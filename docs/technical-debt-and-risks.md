# Laboratory 11 — Technical Debt, Residual Risks and Release Review

**Review date:** 30 October 2026  
**Project:** Logistics Route Decider System  
**Recommendation:** **CONDITIONAL NO-GO pending local build/test verification and remaining controls.** The branch now contains a demonstrator vertical slice, but it has not been compiled or executed in this editing session.

## Updated debt and risk register

| ID | Debt / residual risk | Current status | Evidence | Required mitigation / gate |
|---|---|---|---|---|
| TD-01 | Build descriptor and reproducible setup | Implemented, unverified | pom.xml targets Java 21 / Spring Boot 3.3.5; README has commands | Run clean Maven build on JDK 21 |
| TD-02 | Runnable entry point and HTTP adapter | Implemented, unverified | LogisticsRouteDeciderApplication and RouteOptimizationController | Launch app and exercise endpoint |
| TD-03 | Real routing provider | Open | DeterministicRoutingAdapter is synthetic; MapboxAdapter remains a stub | Integrate provider, configure timeouts/retries and validate provider response |
| TD-04 | Production persistence and schema | Partially implemented | H2 schema and JDBC transactional persistence | Replace H2 with approved relational DB; add migrations and replica/read consistency |
| TD-05 | Full business invariants | Partially implemented | Workflow checks request capacity, shift, delivery windows, batch status and reachability | Load authoritative order/vehicle/driver data from persistence; add boundary tests |
| TD-06 | Automated tests | Authored, not run | RouteOptimizationApiTest covers success, capacity and unreachable cases | Run mvn clean test; add provider failure, idempotency, shift/window and rollback cases |
| TD-07 | API contract implementation | Partially implemented | POST /api/routes/optimize exists | Reconcile exact request/response/error schema and add contract tests |
| TD-08 | Authentication/authorization | Open / blocker | No authentication or dispatcher-role enforcement in slice | Add gateway/API security and tests before shared or production use |
| TD-09 | Primary/replica deployment | Design-only | Architecture docs retain primary and read replica; demo uses in-memory H2 | Verify production DB topology, lag, failover and recovery |
| TD-10 | Secrets, privacy and operations | Open | Demo uses synthetic data and no provider credentials | Externalize secrets; add redaction, audit, monitoring, backup and operational ownership |

## Residual risks
1. Synthetic distance and duration are not route optimization estimates suitable for operational dispatch. Do not use this adapter for real deliveries.
2. Vehicle capacity, shift and order totals arrive in the HTTP request in this slice; without authoritative persisted data and authentication, a caller could submit false values.
3. Delivery-window calculation is simplified and uses an equal synthetic leg-time allocation, not road-network ETA.
4. In-memory H2 loses data on restart and does not implement the primary/read-replica architecture.
5. Idempotency key is persisted and unique, but replay/conflict/concurrency behaviour needs tests and review.
6. The current slice does not assign a driver or send tracking/notification events; optimization remains separate from later approval/assignment as specified in the design.
7. No compile/test/runtime result has been observed in this editing session.

## Release recommendation
**Do not approve production or real operational use.** For the Phase 2 demonstration, first run `mvn clean test` and `mvn spring-boot:run` on Java 21, execute the README curl example, verify H2 route/stop/batch rows, and record the actual output. Resolve any build/runtime/test defects before presenting it as runnable.

Before a controlled non-production team demo, also enforce dispatcher authentication and use authoritative persisted batch/order/vehicle/driver values. Before production, replace H2 and synthetic routing, implement provider resilience, secure the API, test concurrency/idempotency/rollback, and verify replication, monitoring, backup and recovery.

## Evidence integrity
Implementation source and tests have been committed. Test execution, live routing, authentication, production database integration, deployment and Moodle submission have **not** been verified or performed here.
