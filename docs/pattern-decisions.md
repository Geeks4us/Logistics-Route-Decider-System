# Pattern Decisions & Identified Design Problems

**System:** Logistics Route Decider System  
**Group:** CSI473 Group 01 (Geeks4us)  
**Document Ref:** LAB09-PATTERN-001  

---

## 1. Overview & Architectural Context

This document outlines the concrete design problems identified within the core workflow of the **Logistics Route Decider System** and establishes the design patterns chosen to solve them while maintaining a clean, capability-oriented modular monolith architecture.

---

## 2. Identified Project-Specific Design Problems

### 2.1 Persistence Coupling (Domain Entities vs. SQL Schema)
* **Design Problem:** Direct coupling between core domain models (e.g., `RouteRequest`, `RouteDecision`, `Waypoint`) and relational database tables (SQL schema definitions) causes persistence logic to leak into domain business rules. This hinders unit testing without an active database and complicates schema migrations.
* **Impact:** High coupling between domain entities and data access structures.
* **Target Solution:** Decouple domain aggregates from database entities using a dedicated Repository Abstraction and Data Mapper pattern.

### 2.2 Variable Behaviour (Dynamic Vehicle Constraints & Route Scoring)
* **Design Problem:** Route calculation must dynamically adjust depending on vehicle classification (e.g., `HEAVY_TRUCK` weight/height restrictions vs. `VAN` constraints) and routing optimization objectives (e.g., shortest distance vs. lowest toll cost vs. fastest ETA). Using complex nested `if/else` or `switch` statements inside the core routing service violates the Open/Closed Principle.
* **Impact:** Fragile code where adding a new vehicle type or scoring rule risks breaking existing logic.
* **Target Solution:** Encapsulate dynamic calculation logic using the **Strategy Pattern** (`RouteScoringStrategy`).

### 2.3 External Formats (Third-Party Geospatial API Integration)
* **Design Problem:** External map/routing engines (Mapbox, OpenStreetMap) return distinct API payloads, coordinate formats, and error codes. Exposing core domain logic directly to external JSON payloads binds system logic to third-party provider specifications.
* **Impact:** Vendor lock-in and potential cascading failures when external API contracts change.
* **Target Solution:** Implement an Anti-Corruption Layer (ACL) using an outbound Port interface (`RoutingEnginePort`) and Adapter (`MapboxAdapter`).

### 2.4 Secondary Effects (Side Effects & Notification Dispatches)
* **Design Problem:** Upon generating an optimized route decision, secondary operations—such as updating dispatch telemetry, emitting audit logs, and dispatching notification events—executed synchronously inside the main thread introduce execution latency and thread exhaustion during upstream outages.
* **Impact:** Slow response times and increased failure probability for primary route requests.
* **Target Solution:** Decouple secondary side effects from primary workflow execution via Domain Events and asynchronous listeners.

### 2.5 Object Creation & Concurrency Control (Idempotent Route Assembly)
* **Design Problem:** Assembling a valid `RouteRequest` aggregate while handling duplicate incoming client payloads, Redis distributed locks, and idempotency ledger validation requires multi-stage state verification. Doing this via raw constructors leads to inconsistent object states.
* **Impact:** Duplicate route computations, extra third-party API costs, and race conditions during network retries.
* **Target Solution:** Encapsulate creation logic and idempotency checking via an `IdempotencyLedgerManager` and Factory pattern.

---

## 3. Pattern Decision Summary Matrix

| Design Problem | Applied Pattern / Technique | Architectural Boundary / Interface | Target Outcome |
| :--- | :--- | :--- | :--- |
| **Persistence Coupling** | Repository & Mapper Pattern | `RouteRepository` | Isolates SQL schemas from domain logic; enables in-memory mocking for unit tests. |
| **Variable Behaviour** | Strategy Pattern | `RouteScoringStrategy` | Allows runtime selection of scoring algorithms without modifying core services. |
| **External Formats** | Outbound Port & Adapter (ACL) | `RoutingEnginePort` | Shields domain logic from Mapbox/OSM payload structures and vendor changes. |
| **Secondary Effects** | Domain Event / Asynchronous Observer | `RouteCalculatedEvent` | Moves non-critical operations out of the synchronous request-response path. |
| **Object Creation Complexity** | Factory / Guarded Initialization | `IdempotencyLedgerManager` | Guarantees idempotent request processing and prevents duplicate execution threads. |
