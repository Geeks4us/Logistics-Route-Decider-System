# UC-25: Optimized Delivery Process

**Primary actor:** Route Dispatcher  
**Supporting system:** External Mapping / Routing API  
**Goal:** Generate a feasible optimized route for a selected order batch while satisfying vehicle capacity, driver shift and customer delivery-window constraints.

## Trigger

The dispatcher selects an order batch, vehicle and driver, then requests route optimization through the client application.

## Preconditions

- The order batch exists and is in Draft state.
- The batch contains at least one order, and every order is confirmed and eligible for delivery.
- Every order has a valid delivery address/coordinates, delivery time window and non-negative payload weight/volume.
- The selected vehicle exists, is active and available, with recorded weight and volume capacity.
- The selected driver exists and is available, with a recorded maximum shift duration.
- The dispatcher is authenticated and authorized.
- The mapping provider is expected to be available; if it is unavailable at execution time, the failure flow applies.

## Success scenario

1. Dispatcher selects a Draft order batch, an available vehicle and an available driver.
2. Client sends POST /api/routes/optimize through the API Gateway with an Idempotency-Key.
3. Gateway authenticates/rate-limits the request and forwards it to the Logistics Route Decider application.
4. Dispatch capability validates batch, orders, vehicle and driver using authoritative reads from the primary database.
5. System checks aggregate vehicle weight and volume capacity and the driver's shift limit.
6. Optimization capability requests distance and travel-time data from the external mapping/routing API.
7. Mapping provider returns road distance and travel-time information.
8. System calculates a stop sequence and ETAs, checking delivery windows and reachability.
9. If all constraints pass, system prepares an Optimized route proposal.
10. In one primary-database transaction, system saves the route and ordered stops, changes the batch from Draft to Optimized, and records the idempotency result.
11. System returns the route proposal through the gateway for dispatcher review.
12. The driver remains Available until a separate dispatcher approval/assignment operation successfully dispatches the route.

## Alternative flows

### A1 — Vehicle over capacity
1. Validation determines that total order weight or volume exceeds the selected vehicle's limit.
2. Optimization is rejected with CAPACITY_EXCEEDED.
3. No route or stop records are committed; batch remains Draft.
4. Dispatcher selects a suitable vehicle or changes the batch and retries.

### A2 — Unreachable delivery stop
1. Mapping data identifies one or more stops as unreachable.
2. System does not silently discard an order or finalize a partial route.
3. System identifies the affected order(s) and returns UNREACHABLE_STOP for dispatcher action.
4. Batch remains Draft until the order/address or routing issue is resolved.

### A3 — Mapping API unavailable or times out
1. Adapter applies a finite timeout and at most one bounded automatic retry.
2. If the provider remains unavailable, system returns 503 OPTIMIZATION_UNAVAILABLE.
3. No route or stop records are committed; batch remains Draft.
4. Dispatcher may retry later using the same Idempotency-Key for the same request.

### A4 — Driver shift limit exceeded
1. Estimated driving, service and break time exceeds the driver's maximum shift.
2. System rejects the proposal with SHIFT_LIMIT_EXCEEDED.
3. No route is committed; batch remains Draft.
4. Dispatcher selects another driver or adjusts the batch.

### A5 — Concurrent update or resource assignment
1. Another dispatcher changes the batch, driver or vehicle after initial validation.
2. The application detects a stale version or active-assignment conflict during the final primary transaction.
3. The transaction is rejected with a 409 conflict; no partial route is committed.
4. Dispatcher refreshes current state and retries with a new idempotency key if the request content changes.

## Postconditions

**Success:** Exactly one Optimized route proposal and its ordered stops are committed; batch state is Optimized; driver and vehicle are not yet marked dispatched/assigned.

**Failure:** Batch remains Draft; no partial route or stop records are persisted; the dispatcher receives a specific error outcome.

## Consistency and deployment notes

- API Gateway is the client ingress point; domain validation and state transitions remain in the application.
- Writes and consistency-sensitive reads use the primary database.
- The database replica is read-only and may serve only explicitly stale-tolerant views.
- Route persistence and batch lifecycle transition are atomic on the primary.
