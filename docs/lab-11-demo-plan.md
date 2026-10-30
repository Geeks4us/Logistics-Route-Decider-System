# Laboratory 11 — Five-Minute Demonstration Plan

**Project:** Logistics Route Decider System  
**Core use case:** UC-25 — Optimized delivery process  
**Data policy:** synthetic orders, addresses, vehicle, driver and batch only.

## Demonstration flow (target once implementation is runnable)

| Time | Presenter action | Evidence to show |
|---|---|---|
| 0:00–0:45 | Sign in as dispatcher and select Draft batch with confirmed synthetic orders | Dispatcher interface/API request and batch state |
| 0:45–1:45 | Submit optimization request with selected vehicle and driver | HTTP request, validation and application-service call |
| 1:45–2:45 | Show route result and stop sequence/ETA | Routing port/adapter result and BR-01–BR-05 validation |
| 2:45–3:45 | Show route/stops persisted and batch Draft → Optimized | Database query/repository evidence and transaction boundary |
| 3:45–5:00 | Submit invalid or failing request; show controlled error/no partial route | Test output and unchanged Draft batch/no route record |

## Current readiness

**Not runnable at review time.** Repository has no build descriptor, application entry point, HTTP controller, concrete persistence adapter, schema/migration or automated tests. Routing adapter is an unsupported-operation stub. Do not present this plan as a completed demonstration.

## Minimum implementation and rehearsal gates
1. Add reproducible Java 21 build and setup instructions.
2. Implement dispatcher HTTP boundary and UC-25 workflow.
3. Add synthetic seed batch/orders/vehicle/driver and persistence schema.
4. Implement deterministic synthetic routing adapter for offline demo; label it clearly, not live Mapbox routing.
5. Enforce business rules and atomic persistence/failure behaviour.
6. Add and run unit and integration tests.
7. Rehearse from clean checkout using README instructions; record command and observed output.
