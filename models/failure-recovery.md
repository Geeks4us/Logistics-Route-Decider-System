# Failure, Recovery & Concurrency Architecture

**System:** Logistics Route Decider System (CSI473 Group 01 - Geeks4us)  
**Document Ref:** FR-001  
**Applies to:** Phase 1 Capability-Oriented Modular Monolith

---

## 1. Scope and architectural assumptions

Phase 1 is a single deployable modular monolith with one relational database. This document therefore does not assume an API gateway, multiple application nodes, Redis, distributed locks, or a message broker. Those technologies may be evaluated later, but require an explicit architecture decision and operational justification.

The system delegates base distance, travel-time, and routing calculations to an external mapping API under D-001. It does not introduce a separate heuristic routing engine as an implicit fallback.

## 2. External mapping API failure

The Optimization application calls the mapping provider through `RoutingEnginePort`. The provider-specific adapter is responsible for applying a finite connection/request timeout and translating provider errors into application-level failures.

Phase 1 behaviour:

1. Validate the optimization request before making an external call.
2. Invoke the mapping provider through `RoutingEnginePort` with a configured timeout.
3. If the provider succeeds, return the calculated route data to the application service.
4. If the provider times out, is unavailable, or rejects the request, return a clear optimization failure to Dispatch. Do not silently invent a route or claim that an approximate route is optimized.
5. Keep the route in its prior valid state; do not advance it to `Optimized` or `Assigned` when calculation has failed.
6. Record a diagnostic event/log with a correlation identifier and provider error category, without logging customer-sensitive address/contact data unnecessarily.

A bounded retry may be added for transient errors only if it respects the provider's rate limits and an overall request deadline. A circuit breaker can be introduced if measured failure rates justify it; it is not a prerequisite for the initial skeleton.

Caching or a secondary mapping provider may be considered under D-001's stated consequences, but must be designed and documented before implementation. A cached result must be clearly marked with its age and suitability; it must not be represented as a fresh live calculation.

## 3. Concurrency and duplicate dispatch attempts

D-002 places constraint validation and state-transition rules in the Java application core and requires explicit concurrency protection. Phase 1 uses the relational database transaction and optimistic versioning/locking for route and driver assignment records.

1. A dispatcher submits an assignment request with the route/driver identifiers and the version last observed by the client.
2. The application validates vehicle capacity, driver availability, and the permitted route-state transition.
3. The application persists the assignment in a database transaction using a version column or equivalent optimistic-lock mechanism.
4. If another dispatcher has changed the route or driver first, the update affects no current version and the transaction fails with a concurrency conflict.
5. The application returns a conflict response and asks the client to refresh the route/driver availability.
6. The transaction ensures that a failed assignment does not leave a partially updated route or driver record.

If API-level idempotency keys are later required for retries, store their status/result in the relational database within the same transactional boundary. A Redis distributed lock is not required for a single-instance Phase 1 deployment.

## 4. Deferred mechanisms

The following are explicitly deferred and must not be treated as current architecture:

- Redis cache or distributed locks
- Multiple application nodes and API gateway routing
- A custom heuristic/fallback route engine
- A message broker or distributed transaction protocol
- Automatic background health-probe orchestration

Revisit these only when deployment topology, measured traffic, provider reliability, or recovery requirements demonstrate a concrete need. Record any adoption in a new ADR and update ADR-001, ADR-002, and this document together.
