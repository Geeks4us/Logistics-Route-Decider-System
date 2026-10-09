# Logistics Route Decider System: Architecture, Failure Recovery & Data Model

**System:** Logistics Route Decider System (CSI473 Group 01 - Geeks4us)  
**Document Ref:** MASTER-001  

---

## 1. Overview & System Context

In a distributed logistics platform, network partitions, upstream API outages, concurrent retries, and data persistence are core architectural challenges. This document consolidates the system's fault-tolerance, circuit breaking, concurrency controls, and logical data model to guarantee reliability and consistency.

---

## 2. Failure, Recovery & Concurrency Architecture

### A. Failure & Recovery Model: Unavailable External Dependency
When external geospatial routing engines or map providers experience latency spikes or complete outages, the system prevents thread exhaustion and service crashes using a **Circuit Breaker** and **Graceful Degradation** pattern.

#### Interaction Flow
    [Client / Dispatcher] 
           │
           ▼ (1. Route Request with X-Request-Id)
    [Route Decider Service] 
           │
           ├─► [Redis Cache] ──(Hit / Near Match)──► Return Cached Route
           │
           ▼ (Cache Miss)
    [Circuit Breaker] ──(OPEN)──► [Fallback Engine] ──► Generate Heuristic / Skeleton Path
           │
           ├─► (CLOSED) 
           ▼
    [External Routing API] ──(Timeout / Failure > Threshold)──► Trip Circuit Breaker

#### Step-by-Step Breakdown
1. **Request Ingestion & Validation:** The client dispatches a route calculation payload containing origin, destination, and waypoints alongside an idempotency key (`X-Request-Id`).
2. **Primary Dependency Timeout:** The service invokes the external geospatial routing dependency. Due to upstream degradation, the network call hangs and breaches the strict 3,000ms timeout budget.
3. **Circuit Breaker Trip:** The internal Circuit Breaker monitors failure rates. Once failures exceed the configured threshold (e.g., 5 consecutive failures within 10 seconds), the circuit state transitions from **Closed** to **Open**, short-circuiting subsequent live calls.
4. **Graceful Degradation & Fallback:** Rather than returning a hard `504 Gateway Timeout` to the dispatcher, the service executes its fallback strategy:
   * Queries the **Redis Cache** for pre-calculated regional skeleton paths.
   * Applies a secondary heuristic routing algorithm (straight-line distance + historical traffic weight) to compute a safe approximate route.
5. **Response Delivery & Auditing:** The route decision is returned with an explicit response header (`X-Route-Status: Degraded-Fallback`), while an error telemetry event is dispatched to monitoring tools.
6. **Self-Healing & Probing:** A background health-check probe periodically pings the primary dependency. Upon receiving successive successful health responses, the Circuit Breaker transitions to **Half-Open**, permits a controlled test request, and fully resets to **Closed**.

---

### B. Concurrency Model: Duplicate Requests & Idempotency Locking
When network timeouts cause mobile apps or dispatch clients to retry requests, duplicate payloads bearing the same `X-Request-Id` can create race conditions, redundant graph calculations, and extra third-party API billing. 

#### Interaction Flow
    [Client Req-A] ──┐
                     ├──► [API Gateway / Nodes] ──► [Redis Distributed Lock (NX EX 30)]
    [Client Req-B] ──┘                                        │
                                                ┌─────────────┴─────────────┐
                                                ▼                           ▼
                                        [Node A: Acquires Lock]     [Node B: Lock Fails]
                                                │                           │
                                                ▼                           ▼
                                        [Ledger: PENDING]           [Poll Ledger / 409 Conflict]
                                                │
                                                ▼
                                        [Route Calculation]
                                                │
                                                ▼
                                        [Ledger: COMPLETED & Release Lock]

#### Step-by-Step Breakdown
1. **Concurrent Arrival:** Identical requests (`Req-A` and `Req-B`) arrive simultaneously at different API gateway nodes with the same `X-Request-Id`.
2. **Atomic Distributed Lock Acquisition:** Both nodes attempt an atomic Redis lock acquisition:
   `SET lock:idempotency:<uuid> node_id NX EX 30`
   * **Node A** succeeds.
   * **Node B** fails because the lock key already exists.
3. **Ledger Initialization:** Node A queries the Idempotency Ledger table/cache, finding no existing entry, and inserts a `status = PENDING` record.
4. **Alternate Path Handling:** Because Node B failed to acquire the distributed lock, it avoids duplicate computation. It enters a short-poll retry loop or queries the Idempotency Ledger, observing that the request is currently `PENDING`.
5. **Route Computation & Ledger Finalization:** Node A computes the optimal route, serializes the response, updates the ledger record to `status = COMPLETED` with the payload result, and releases the distributed lock (`DEL`).
6. **Cache Hit & Replay:** Subsequent duplicate requests bypass the route engine entirely, instantly returning the cached payload from the Idempotency Ledger.

---

## 3. Logical Data Model & Entity Relationship Specification

### A. Entity-Relationship Overview & Cardinalities
    [Client / Dispatcher] 1 ──── N [Route Request] 
                                      │
                                      ├──── N [Waypoint]
                                      │
                                      ├──── 1 [Route Decision]
                                      │
                                      └──── 1 [Idempotency Ledger]
                                      
    [Vehicle / Fleet] 1 ──────── N [Route Request]

#### Relationship Summary Matrix
* **Client to Route Request:** One-to-Many ($1:N$) — A single client/dispatcher can initiate multiple route requests.
* **Vehicle to Route Request:** One-to-Many ($1:N$) — A specific logistics vehicle can be assigned to multiple route calculation jobs over time.
* **Route Request to Waypoint:** One-to-Many ($1:N$) — A route request contains an ordered sequence of at least 2 waypoints (origin and destination) plus optional stops.
* **Route Request to Route Decision:** One-to-One ($1:0..1$) — A request yields precisely one calculated route decision (or none if failed/degraded).
* **Route Request to Idempotency Ledger:** One-to-One ($1:1$) — Every request maps strictly to a unique idempotency tracking ledger entry.

---

### B. Core Entities, Attributes & Constraints

#### 1. `clients` (Dispatchers & API Consumers)
Stores registered accounts or API clients authorized to submit logistics routing jobs.

| Column Name | Data Type | Key Type | Constraints | Description |
| :--- | :--- | :--- | :--- | :--- |
| `client_id` | UUID | PK | `NOT NULL` | Unique identifier for the client entity. |
| `company_name` | VARCHAR(120) | - | `NOT NULL` | Organization or logistics partner name. |
| `api_key_hash` | VARCHAR(255) | - | `NOT NULL, UNIQUE` | Hashed credential for API authentication. |
| `status` | VARCHAR(20) | - | `CHECK (status IN ('ACTIVE', 'SUSPENDED'))` | Operational status of the client account. |
| `created_at` | TIMESTAMP | - | `DEFAULT CURRENT_TIMESTAMP` | Account provisioning timestamp. |

#### 2. `vehicles` (Fleet Management)
Represents physical delivery units and trucks carrying cargo payloads.

| Column Name | Data Type | Key Type | Constraints | Description |
| :--- | :--- | :--- | :--- | :--- |
| `vehicle_id` | UUID | PK | `NOT NULL` | Unique identifier for the vehicle. |
| `license_plate` | VARCHAR(20) | - | `NOT NULL, UNIQUE` | Official vehicle registration number. |
| `max_weight_kg` | DECIMAL(8,2)| - | `CHECK (max_weight_kg > 0)` | Maximum cargo weight load capacity. |
| `vehicle_type` | VARCHAR(30) | - | `NOT NULL` | Classification (e.g., `HEAVY_TRUCK`, `VAN`). |
| `created_at` | TIMESTAMP | - | `DEFAULT CURRENT_TIMESTAMP` | Record creation timestamp. |

#### 3. `route_requests` (Incoming Dispatch Inquiries)
Captures raw operational payloads submitted by dispatchers requesting route computation.

| Column Name | Data Type | Key Type | Constraints | Description |
| :--- | :--- | :--- | :--- | :--- |
| `request_id` | UUID | PK | `NOT NULL` | Unique internal tracking UUID. |
| `client_id` | UUID | FK | `REFERENCES clients(client_id)` | The client submitting the request. |
| `vehicle_id` | UUID | FK | `REFERENCES vehicles(vehicle_id)` | The vehicle assigned to this route. |
| `idempotency_key` | VARCHAR(64)| - | `NOT NULL, UNIQUE` | Client-provided hash (`X-Request-Id`) to prevent duplicates. |
| `priority_level` | INT | - | `CHECK (priority_level BETWEEN 1 AND 5)` | Routing urgency score. |
| `created_at` | TIMESTAMP | - | `DEFAULT CURRENT_TIMESTAMP` | Time request entered the ingestion queue. |

#### 4. `waypoints` (Geospatial Location Points)
Normalized breakdown of spatial points comprising a route request (origins, stops, destinations).

| Column Name | Data Type | Key Type | Constraints | Description |
| :--- | :--- | :--- | :--- | :--- |
| `waypoint_id` | UUID | PK | `NOT NULL` | Unique waypoint record identifier. |
| `request_id` | UUID | FK | `REFERENCES route_requests(request_id) ON DELETE CASCADE` | Associated parent route request. |
| `sequence_order`| INT | - | `CHECK (sequence_order >= 0)` | Ordered traversal position (0 = origin). |
| `latitude` | DECIMAL(9,6)| - | `CHECK (latitude BETWEEN -90 AND 90)` | GPS coordinate latitude. |
| `longitude` | DECIMAL(10,6)| - | `CHECK (longitude BETWEEN -180 AND 180)` | GPS coordinate longitude. |
| `location_name` | VARCHAR(150)| - | `NULL` | Human-readable depot or drop-off name. |

#### 5. `route_decisions` (Computed Outputs & Fallbacks)
Stores the final calculated path, total distance, travel duration, and execution metadata.

| Column Name | Data Type | Key Type | Constraints | Description |
| :--- | :--- | :--- | :--- | :--- |
| `decision_id` | UUID | PK | `NOT NULL` | Unique decision record identifier. |
| `request_id` | UUID | FK, UQ | `REFERENCES route_requests(request_id) ON DELETE CASCADE` | Associated route request. |
| `total_distance_km`| DECIMAL(8,2)| - | `CHECK (total_distance_km >= 0)` | Total calculated transit distance. |
| `estimated_duration_min`| INT | - | `CHECK (estimated_duration_min >= 0)` | Estimated time of arrival (ETA) in minutes. |
| `execution_status`| VARCHAR(30) | - | `CHECK (execution_status IN ('OPTIMAL', 'DEGRADED_FALLBACK', 'CACHED'))` | Indicates whether live API or cache/heuristic was used. |
| `computed_at` | TIMESTAMP | - | `DEFAULT CURRENT_TIMESTAMP` | Timestamp of route finalization. |

#### 6. `idempotency_ledger` (Concurrency & State Control)
Tracks request execution state to coordinate distributed locks and prevent race conditions.

| Column Name | Data Type | Key Type | Constraints | Description |
| :--- | :--- | :--- | :--- | :--- |
| `ledger_id` | UUID | PK | `NOT NULL` | Unique entry identifier. |
| `request_id` | UUID | FK, UQ | `REFERENCES route_requests(request_id) ON DELETE CASCADE` | Associated route request. |
| `status` | VARCHAR(20) | - | `CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED'))` | Current lifecycle state of the request. |
| `response_payload`| TEXT | - | `NULL` | Serialized JSON response returned to the client. |
| `expires_at` | TIMESTAMP | - | `NOT NULL` | Time-to-live expiration for cache invalidation. |
