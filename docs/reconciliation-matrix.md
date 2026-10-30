# Laboratory 11 — Requirement / Model / Code / Test Reconciliation

**Project:** Logistics Route Decider System  
**Review date:** 30 October 2026  
**Branch:** lab-11; baseline lab-09 commit a7ed4134cd5123931bec6cbb156f6c129b147a8f.

| Requirement / source | Expected behaviour | Implementation evidence | Test evidence / result | Difference / remaining work |
|---|---|---|---|---|
| UC-25 / API contract POST /api/routes/optimize | Dispatcher submits batch, vehicle, driver and stop details; receives optimized route | `RouteOptimizationController`, `OptimizeRouteRequest`, `RouteOptimizationWorkflow` | HTTP integration test for success | Slice uses request-supplied vehicle/driver/order data; no authenticated dispatcher or full domain repository |
| BR-01 capacity | Weight and volume within vehicle limits | Workflow checks both limits before routing/persistence | Capacity rejection test asserts HTTP 422, no route, batch Draft | No persisted order/vehicle aggregates; values supplied in request |
| BR-02 driver shift | Estimated route duration within shift | Workflow compares synthetic driving estimate plus service times to request shift | Not yet covered by automated test | Add boundary test; estimate is synthetic |
| BR-03 delivery windows | Each stop ETA within window | Workflow checks cumulative synthetic per-stop estimate against each window | Not yet covered by automated test | Simplified estimate; real travel-time/traffic provider required |
| BR-04 batch lifecycle | Draft and all orders confirmed before Optimized | H2 `order_batch` row locked and checked; update in transaction | Success test verifies Optimized; failure test verifies Draft | Seed batch only; order confirmation is a batch-level demo flag |
| BR-05 reachable stops | No unreachable stop in saved route | Workflow rejects any request stop marked unreachable | Unreachable-stop test asserts no route persisted | Reachability flag is supplied by request, not external geocoder |
| BR-06 atomic failure | Failed optimization leaves no route and batch Draft | Workflow is `@Transactional`; persistence occurs after validation and routing | Capacity and unreachable tests assert no route/batch transition | Add simulated provider/persistence failure rollback test |
| ADR-001 modular monolith | One deployable app; gateway and primary/replica retained | Spring Boot app and capability packages; gateway/replica remain architecture intent | Context-load implicit in Spring Boot integration tests (not run) | H2 demo does not implement deployment topology |
| ADR-002 Spring Boot | Spring at composition/API/infrastructure boundaries; plain Java core | Maven Spring Boot 3.3.5 project; composition bean and routing port | Tests authored; not executed here | Verify clean build on Java 21/Maven |
| Persistence / idempotency | Atomic route/stop/batch writes and replay protection | H2 schema, JDBC transaction, idempotency table and unique constraints | Success/failure side-effect tests; replay not yet tested | H2 is demo-only; concurrency/replay tests remain |

## Honest status
A runnable vertical slice has now been added: HTTP controller → application workflow → routing port / deterministic synthetic adapter → transactional JDBC persistence → HTTP response. The database is H2 in-memory and the routing algorithm is deliberately synthetic. This is not production routing, production persistence, or a complete secured system.

Automated HTTP integration tests have been authored, but **were not run in this editing session**. Compilation and runtime remain unverified until `mvn clean test` and a local launch succeed under Java 21/Maven 3.9+.

Do not revise architecture models to hide these implementation differences. Record later implementation and observed test output here.
