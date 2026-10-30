# Failure and Recovery Model — Mapping Provider Unavailable

## Scope

This scenario covers a mapping/routing provider timeout during route optimization. The API Gateway and database primary/replica remain part of the deployment. The application is a modular monolith; Dispatch calls Optimization in-process.

## Sequence

```mermaid
sequenceDiagram
    actor Dispatcher
    participant Gateway as API Gateway
    participant App as Logistics Route Decider
    participant Dispatch as Dispatch Module
    participant Optimizer as Optimization Module
    participant Map as External Mapping API
    participant Primary as Primary Database
    participant Replica as Read Replica

    Dispatcher->>Gateway: POST /api/routes/optimize + Idempotency-Key
    Gateway->>App: Forward authenticated request
    App->>Dispatch: Optimize batch request
    Dispatch->>Primary: Read batch, orders, driver, vehicle
    Primary-->>Dispatch: Current state and versions
    Dispatch->>Optimizer: OptimizeRouteUseCase (in-process)
    Optimizer->>Map: Request distance/time matrix
    Note over Optimizer,Map: Finite timeout; at most one bounded retry
    Map--xOptimizer: Timeout / unavailable
    Optimizer-->>Dispatch: DependencyUnavailable
    Dispatch->>Primary: Persist failed idempotency outcome only
    Dispatch-->>App: Optimization unavailable; no route committed
    App-->>Gateway: 503 OPTIMIZATION_UNAVAILABLE
    Gateway-->>Dispatcher: 503 + retry guidance

    Note over Dispatcher,Map: Dispatcher retries later with same key/request
    Dispatcher->>Gateway: Retry POST with same Idempotency-Key
    Gateway->>App: Forward request
    App->>Dispatch: Retry operation
    Dispatch->>Primary: Check idempotency and current state
    Primary-->>Dispatch: No successful route exists; batch still Draft
    Dispatch->>Optimizer: OptimizeRouteUseCase
    Optimizer->>Map: Request distance/time matrix
    Map-->>Optimizer: Distance/time matrix
    Optimizer-->>Dispatch: Valid optimized stop sequence
    Dispatch->>Primary: Atomic commit route + stops + batch Optimized + idempotency result
    Primary-->>Dispatch: Commit successful
    Primary-->>Replica: Asynchronous replication
    Dispatch-->>App: Saved Optimized route
    App-->>Gateway: 201 Created
    Gateway-->>Dispatcher: Route proposal for review
```

## Failure and recovery rules

1. Use a finite connection/request timeout for the mapping provider. Do not wait indefinitely.
2. Permit at most one automatic retry with bounded backoff for a transient timeout. Do not retry indefinitely.
3. The client supplies an Idempotency-Key. The primary database stores its scope, request fingerprint and outcome under a unique constraint.
4. A timeout before a successful commit must not create a route, stops or batch-state transition. The batch remains Draft.
5. A repeated request with the same key and same body must not create a second route. If the original operation completed but the response was lost, return the stored successful result. A different body with the same key returns 409.
6. Return 503 OPTIMIZATION_UNAVAILABLE with a safe retry instruction when the provider remains unavailable. Do not expose provider credentials or internal exception details.
7. Once mapping recovers, a retry may proceed after rechecking batch/resource versions and availability on the primary.
8. The replica is not consulted to decide whether a route was created or whether a driver/resource is available. Its state may lag.
9. Notification is not part of the optimization transaction. Any later dispatch notification is sent after dispatch commit and retried independently.

## Recovery outcomes

| Situation | Expected outcome |
|---|---|
| Provider times out and bounded retry also fails | 503; batch remains Draft; no partial route. |
| Client retries after failure with same key | Reuse recorded outcome or safely re-run if no successful operation was committed, according to idempotency policy. |
| Provider recovers and optimization succeeds | One route and its stops commit atomically; batch becomes Optimized. |
| Response is lost after successful commit | Same key returns the original route result; no duplicate route. |
| Replica is behind | Operation result comes from primary; no replica-based assignment decision. |
