# Laboratory 11 — Requirement / Model / Code / Test Reconciliation

**Project:** Logistics Route Decider System  
**Review date:** 30 October 2026  
**Baseline:** lab-09, commit a7ed4134cd5123931bec6cbb156f6c129b147a8f; review branch lab-11.

This records repository evidence actually present at review time. Planned tests are not passing tests. “Not implemented” and “not run” are deliberate findings.

| Requirement / source | Expected behaviour | Code evidence at baseline | Test evidence / result | Difference and action |
|---|---|---|---|---|
| UC-25, optimize batch | Dispatcher requests optimized route for Draft batch; result persisted and shown for review | DispatchService only has findRoute; optimization TODO remains. RouteOptimizationService delegates to a port | No automated tests; not run | Core vertical slice incomplete. Implement HTTP boundary, workflow, persistence adapter and integration test |
| BR-01, capacity | Total order weight and volume within vehicle limits | No vehicle/order aggregate or capacity validator | No test; not run | Add validation and boundary tests |
| BR-02, driver shift | Driving, service and break duration within shift | No driver/shift model or validator | No test; not run | Add shift calculation and tests |
| BR-03, delivery windows | Every stop ETA within delivery window | Request model only contains stop address strings | No test; not run | Extend model with windows and ETA calculation |
| BR-04, lifecycle | Only Draft batch with confirmed orders becomes Optimized | Route is minimal record with free-form status; no batch state | No test; not run | Add explicit lifecycle state and guarded transitions |
| BR-05, reachability | Optimized route contains no unreachable stop | Routing port returns ordered stops/distance; no reachability field/check | No test; not run | Model reachability and reject incomplete results |
| BR-06, atomic failure | Failure creates no route and leaves batch Draft | No transaction/persistence. MapboxAdapter throws UnsupportedOperationException | No test; not run | Implement transaction and rollback test |
| ADR-001, modular monolith | One deployable app with three capability modules; gateway and primary/replica retained | Java packages show dispatch, optimization and tracking ports; no runnable app | Not tested | Module skeleton aligned in intent; runtime/topology not evidenced |
| ADR-002, Spring Boot | Spring at adapters/composition root; core services plain Java | ApplicationConfiguration uses Spring beans; MapboxAdapter imports Spring component annotation | No build descriptor/tests | Decision documented but executable Spring app not demonstrated |
| API contract, POST /api/routes/optimize | Batch/vehicle/driver request, validation, idempotency, controlled errors | No HTTP controller or request DTO | No HTTP tests; not run | Contract is design evidence only; implement controller/tests |
| Persistence | Route/stops persist atomically to primary; replica read-only and may lag | RouteRepository is only interface; no concrete repository/schema/migrations | No DB integration tests; not run | Add migration, repository adapter and rollback test |

## Overall reconciliation
- **Aligned:** capability package separation and OptimizeRouteUseCase / RoutingEnginePort match modular-monolith and ports-and-adapters decisions.
- **Partially implemented:** Spring composition root and service skeletons exist.
- **Not implemented/evidenced:** HTTP interface, application entry point, real routing adapter, batch/order/vehicle/driver validation, durable persistence, schema/migrations, executable tests and end-to-end vertical slice.
- **No successful execution claim:** no build file was present, so compilation, tests and runtime were not verified.

Update this matrix with exact file paths, test names, command, date and observed result as implementation changes. Do not edit design documents merely to hide code gaps.
