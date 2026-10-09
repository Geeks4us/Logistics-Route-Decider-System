# Failure, Recovery & Concurrency Architecture

**System:** Logistics Route Decider System (CSI473 Group 01 - Geeks4us)  
**Document Ref:** FR-001  

---

## 1. Overview

In a distributed logistics platform, network partitions, upstream API outages, and client retries are inevitable. This document formally models the system's fault-tolerance, circuit breaking, graceful degradation, and idempotency-locking mechanisms to guarantee system reliability and prevent cascading failures.

---

## 2. Failure & Recovery Model: Unavailable External Dependency

When external geospatial routing engines or map providers experience latency spikes or complete outages, the system prevents thread exhaustion and service crashes using a **Circuit Breaker** and **Graceful Degradation** pattern.

### Interaction Flow

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

### Step-by-Step Breakdown

1. **Request Ingestion & Validation:** The client dispatches a route calculation payload containing origin, destination, and waypoints alongside an idempotency key (`X-Request-Id`).
2. **Primary Dependency Timeout:** The service invokes the external geospatial routing dependency. Due to upstream degradation, the network call hangs and breaches the strict 3,000ms timeout budget.
3. **Circuit Breaker Trip:** The internal Circuit Breaker monitors failure rates. Once failures exceed the configured threshold (e.g., 5 consecutive failures within 10 seconds), the circuit state transitions from **Closed** to **Open**, short-circuiting subsequent live calls.
4. **Graceful Degradation & Fallback:** Rather than returning a hard `504 Gateway Timeout` to the dispatcher, the service executes its fallback strategy:
   * Queries the **Redis Cache** for pre-calculated regional skeleton paths.
   * Applies a secondary heuristic routing algorithm (straight-line distance + historical traffic weight) to compute a safe approximate route.
5. **Response Delivery & Auditing:** The route decision is returned with an explicit response header (`X-Route-Status: Degraded-Fallback`), while an error telemetry event is dispatched to monitoring tools.
6. **Self-Healing & Probing:** A background health-check probe periodically pings the primary dependency. Upon receiving successive successful health responses, the Circuit Breaker transitions to **Half-Open**, permits a controlled test request, and fully resets to **Closed**.

---

## 3. Concurrency Model: Duplicate Requests & Idempotency Locking

When network timeouts cause mobile apps or dispatch clients to retry requests, duplicate payloads bearing the same `X-Request-Id` can create race conditions, redundant graph calculations, and extra third-party API billing. 

### Interaction Flow

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

### Step-by-Step Breakdown

1. **Concurrent Arrival:** Identical requests (`Req-A` and `Req-B`) arrive simultaneously at different API gateway nodes with the same `X-Request-Id`.
2. **Atomic Distributed Lock Acquisition:** Both nodes attempt an atomic Redis lock acquisition:
   `SET lock:idempotency:<uuid> node_id NX EX 30`
   * **Node A** succeeds.
   * **Node B** fails because the lock key already exists.
3. **Ledger Initialization:** Node A queries the Idempotency Ledger table/cache, finding no existing entry, and inserts a `status = PENDING` record.
4. **Alternate Path Handling:** Because Node B failed to acquire the distributed lock, it avoids duplicate computation. It enters a short-poll retry loop or queries the Idempotency Ledger, observing that the request is currently `PENDING`.
5. **Route Computation & Ledger Finalization:** Node A computes the optimal route, serializes the response, updates the ledger record to `status = COMPLETED` with the payload result, and releases the distributed lock (`DEL`).
6. **Cache Hit & Replay:** Subsequent duplicate requests bypass the route engine entirely, instantly returning the cached payload from the Idempotency Ledger.
