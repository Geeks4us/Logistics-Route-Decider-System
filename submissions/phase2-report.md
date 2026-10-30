# CSI473 Project Design Report — Phase 2
## Logistics Route Decider System — Group 01 (Geeks4us)
**Date:** 30 October 2026  
**Repository:** https://github.com/Geeks4us/Logistics-Route-Decider-System/tree/lab-11  
**Submission tag:** Not yet created; create `phase2-submission` only after final review.

| Field | Complete |
|---|---|
| Assignment/phase | Phase 2 — Project Design / Implementation Report |
| Approved project title | Logistics-Route-Decider-System |
| Approval decision/date | Proposal form has approval boxes and lecturer sign-off/date blank; confirmation required |
| Team number | Group 01 (Geeks4us) |
| Members and IDs | David Ukandu (202306298); Smarts Ngubo (202003253); Weno Phatshwane (202206615); Lindiwe Mamelala (202306277) |
| Repository URL | https://github.com/Geeks4us/Logistics-Route-Decider-System/tree/lab-11 |
| Submission tag | Pending final review |
| Date | 30 October 2026 |

> **Evidence note:** This report distinguishes design targets, committed source, and observed execution. It does not claim lecturer approval, production readiness, or a successful test run on the latest branch head.

## 1. Executive summary
The Logistics Route Decider System is a proposed web-based logistics planning and dispatch support system for a single fleet operator based in Gaborone Central. It addresses dispatch inefficiencies associated with manual/static route planning, vehicle payload constraints, delivery time windows and limited visibility of delivery progress. The proposal artefact records five observed dispatch/driver operational workflows dated 12 August 2026 and bounds the initial problem to one depot, 5–10 synthetic orders and 1–2 vehicles.

The principal architecture decision is a capability-oriented modular monolith, organized around Dispatch & Management, Optimization Engine, and Tracking & Notification. The wider target design retains an API Gateway and a relational database primary/read-replica topology. The Phase 2 implementation is a narrower UC-25 vertical slice: a Spring Boot HTTP endpoint validates a demo batch and request-supplied constraints, invokes a deterministic synthetic routing adapter, and persists a route, stops, batch transition and idempotency record in an H2 in-memory database.

The repository contains integration-test source for successful optimization, capacity rejection and unreachable-stop rejection. GitHub Actions run 41083022114 (run 2) completed successfully for code commit `cdca3d13c402415487782baab6c76fc32aa09cd1`; the job's Maven test step completed successfully. The report-only commit does not change application source. The test result is therefore verified for the current application code, though the team should still perform a clean-checkout local launch and demonstration. The demonstrator is not production-ready: routing is synthetic, H2 is in-memory, request values are not loaded from authoritative repositories, and authentication/authorization is absent.

## 2. Problem evidence, stakeholders and scope
### Problem statement
Logistics operators and fleet dispatchers face operational bottlenecks when delivery routes are planned manually or with static spreadsheet-based processes. Such planning does not reliably account for vehicle weight/volume capacity, customer delivery windows, driver shift limits, traffic variation or delivery exceptions. The project aims to support constraint-aware route sequencing and stateful dispatch workflows.

### Evidence and approval record
The proposal artefact describes five observed dispatch/driver operational workflows dated 12 August 2026. It identifies dispatchers, drivers, logistics managers and recipients as stakeholders. The lecturer approval checkboxes are not selected and the lecturer sign-off/date are blank in the repository copy. This report therefore does not claim a verified lecturer approval decision or date; append the signed approval form before final academic submission.

### Goal and objectives
The stated goal is to design and implement a stateful route design and optimization system that automates delivery sequencing, enforces operational constraints and manages dispatch workflows. Stated objectives are to reduce daily dispatcher planning from two hours to under 15 minutes per batch, reduce route distance/fuel consumption by at least 15% against manual planning, and improve on-time completion from 78% to above 92%. These are project targets, not measured outcomes of the current demonstrator.

### Stakeholders
- Fleet dispatchers/route planners: create batches, generate and review routes, and respond to operational exceptions.
- Delivery drivers: receive assigned routes and concise status/navigation updates with minimal distraction.
- Logistics managers: monitor fleet performance, route variance, on-time delivery, distance and fuel estimates.
- Customers/recipients: receive reliable deliveries and status/ETA information.
- External mapping/geocoding provider: supplies distance and travel-time information through an integration boundary.

### Scope
**Overall design in scope:** order batching, constraint-aware route optimization, dispatcher review/approval, route lifecycle management, capacity checks, driver-facing route/status views, role-based access, mapping integration and a basic operational dashboard.

**Out of scope:** building a custom map/geocoding engine, payment/billing, native mobile apps, predictive/ML demand forecasting, production-grade continuous GPS tracking and multi-tenant support.

**Phase 2 implemented slice:** UC-25 route optimization proposal for a seeded Draft batch; it does not implement the full system scope or later driver assignment.

Assumptions and constraints include synthetic Gaborone-based test data, no production customer data, a semester-sized vertical slice, Java/SQL backend, limited external API cost, and simplified rather than jurisdiction-complete driving-hour rules. The proposal bounds the initial operating context to Gaborone Central, 5–10 synthetic orders and 1–2 vehicles.

## 3. Glossary
| Term | Definition |
|---|---|
| Order Batch | A collection of pending delivery orders grouped by a dispatcher for simultaneous route optimization. |
| Delivery Route | An ordered sequence of delivery stops proposed for execution by a driver and vehicle. |
| Delivery Stop | A destination on a route with a customer delivery time window. |
| Payload Capacity | The maximum vehicle parcel mass (kg) and volume (m³). |
| Distance Matrix | Travel distances and estimated travel times between locations, supplied by an external mapping API. |
| Dispatch Lock | Concurrency control that prevents two dispatchers from assigning the same driver or vehicle simultaneously. |
| Idempotency Key | A client-supplied request identifier used to avoid duplicate processing and replay the original result. |
| Vertical Slice | A small end-to-end implementation crossing API, application, integration and persistence boundaries. |

## 4. Functional requirements and use cases
The repository requirements catalogue defines FR01–FR10. The Phase 2 vertical slice concentrates on route optimization and selected validation/lifecycle behaviours; it does not claim all catalogue requirements are implemented.

| ID | Requirement summary | Phase 2 status |
|---|---|---|
| FR01 | Dispatcher creates a batch with destination, time window and priority. | Not implemented in slice; seeded demo batch. |
| FR02 | Generate optimized route using priority, time windows and capacity. | Partial; synthetic deterministic estimate and request-supplied constraints. |
| FR03 | Reject route if vehicle weight/volume capacity is exceeded. | Implemented in slice; integration test authored. |
| FR04 | Dispatcher approves/rejects proposed route before assignment. | Deferred; optimization proposal remains separate from assignment. |
| FR05 | Enforce route lifecycle states and reject invalid transitions. | Partial; Draft → Optimized only. |
| FR06 | Automatically resequence unreachable/delayed stops. | Not implemented; unreachable stop is rejected rather than silently dropped. |
| FR07 | Notify assigned driver after route modification. | Not implemented. |
| FR08 | Restrict sensitive data by dispatcher/driver roles. | Not implemented; security is a release blocker. |
| FR09 | Retrieve distance/time from external mapping API. | Not implemented in runnable slice; synthetic adapter used. |
| FR10 | Show manager on-time completion rate over date range. | Not implemented. |

**Actors:** Dispatcher, Optimization Engine, routing provider and relational database.

**UC-25 — Optimize delivery route:** the dispatcher submits a batch, vehicle, driver and stop details with an Idempotency-Key. The target contract checks dispatcher authorization, Draft batch status, confirmed orders, capacity, reachability, time windows and shift limits; it then calculates a route and persists the proposal atomically. In the current slice, authentication is not implemented and business data is supplied in the request rather than loaded from authoritative records.

Acceptance evidence includes a successful HTTP 201 route proposal and negative cases for capacity overflow and unreachable stops. The implementation leaves the driver Available and does not assign the route; assignment is a separate review/approval use case.

## 5. Quality requirements
| Attribute | Scenario / target | Design obligation and evidence |
|---|---|---|
| Performance | Optimize at least 95% of batches up to 100 orders within 15 minutes; planning objective under 15 minutes per batch. | Bound provider calls and measure end-to-end latency; synthetic adapter does not validate target. |
| Scalability | At least 50 concurrent web users and 1,000 orders within stated target. | Load-test API, DB and routing integration; not demonstrated by single-process H2 slice. |
| Reliability | Mapping dependency failure must not create invalid Optimized/Assigned route. | Port/adapter boundary, finite timeout and controlled error; provider-failure test remains. |
| Integrity | Failed optimization must not leave partial route, stop or batch updates. | Local transaction, FK/unique constraints, batch row lock and idempotency table; negative tests assert no route for capacity/unreachable failure. |
| Security/privacy | Only dispatchers and assigned driver may access delivery address/contact data. | RBAC and authenticated gateway/API; not implemented and blocks operational release. |
| Usability/safety | Driver views high-contrast, readable and low-distraction while in transit. | Responsive web flows and restricted input; UI/accessibility evidence absent from this backend slice. |

These are design targets from repository artefacts. No performance, load, security, accessibility or operational field measurements are claimed.

## 6. Analysis/domain model and responsibilities
The domain model centres on Customer, DeliveryOrder, OrderBatch, BatchOrder, Driver, Vehicle, DeliveryRoute, DeliveryStop and UserAccount. An order batch groups orders; an optimized route is associated with a vehicle and driver proposal and contains ordered stops. The logical data model defines primary/foreign keys and cardinalities. The Phase 2 H2 schema is intentionally reduced to `order_batch`, `delivery_route`, `delivery_stop` and `idempotency_record`.

| Component | Responsibility |
|---|---|
| RouteOptimizationController | HTTP boundary; accepts validated request and returns HTTP response. |
| OptimizeRouteRequest | Request data and bean-validation constraints. |
| RouteOptimizationWorkflow | UC-25 orchestration, business checks, transaction and persistence. |
| OptimizeRouteUseCase / RouteOptimizationService | Application capability contract and core optimization service. |
| RoutingEnginePort | Outbound port for route calculation. |
| DeterministicRoutingAdapter | Offline synthetic implementation of the routing port. |
| MapboxAdapter | Provider adapter stub; not registered as active Spring bean. |
| ApplicationConfiguration | Spring composition root / bean wiring. |

**Business rules:** only a Draft batch with confirmed orders can be optimized; total weight and volume must not exceed vehicle maximums; unreachable stops are rejected; simplified estimated duration must fit driver shift and stop windows; success transitions Draft to Optimized and persists route/stops/idempotency in one transaction; failure must not create partial route; optimization is not assignment.

## 7. Interaction and lifecycle
### UC-25 interaction
1. Dispatcher/client sends POST `/api/routes/optimize` with JSON request and Idempotency-Key.
2. Spring MVC controller binds and validates the request.
3. Workflow checks the idempotency record; an existing key replays its stored route result.
4. Workflow locks the batch row and verifies Draft status and confirmed-order state.
5. Workflow checks weight/volume capacity and stop reachability.
6. Optimization use case invokes RoutingEnginePort; current bean is DeterministicRoutingAdapter and returns synthetic distance/duration.
7. Workflow checks simplified shift and delivery-window constraints.
8. Within a JDBC transaction, workflow inserts route/stops, updates batch to Optimized and records idempotency response.
9. Controller returns HTTP 201. Capacity/unreachable validation errors return HTTP 422; routing dependency errors map to HTTP 503.

The target lifecycle is Draft → Optimized → Assigned → In Transit → Completed, with Delayed/Exception outcomes. The Phase 2 slice implements only Draft → Optimized. It intentionally does not mark a driver assigned or In Transit.

The target failure policy uses RoutingEnginePort to isolate a third-party map service. Provider timeout/unavailability should leave the batch in its prior state and return a controlled error; no invented route should be represented as a live provider result. The demonstrator uses a deterministic adapter, so real provider timeout/retry/circuit-breaker behaviour is not proven.

## 8. Architecture and decisions
Selected architecture: capability-oriented modular monolith, one deployable application with Dispatch & Management, Optimization Engine, and Tracking & Notification modules. Module boundaries are expressed through explicit application interfaces and ports. This avoids premature independently deployed microservices and a message broker during the semester.

The target design retains a separate API Gateway for TLS termination, authentication/token verification, rate limiting, request-size limits and routing. The gateway must not own domain business rules, state transitions or persistence. The logical relational database has a primary and asynchronous read replica: writes and consistency-sensitive reads go to primary; only explicitly stale-tolerant reads may use the read-only replica. These remain architecture targets, not features of the H2 demo.

| Decision | Choice | Rationale / consequence |
|---|---|---|
| ADR-001 | Capability-oriented modular monolith; retain API Gateway and primary/read replica. | Clear capability boundaries with one deployable unit; gateway and replica remain edge/data topology components. Local transaction is consistency boundary. |
| ADR-002 | Java 21 + Spring Boot 3.x. | Spring MVC/DI/validation/JDBC at adapters and composition root; keep domain/application core as plain Java. |
| D-001 | External mapping/geocoding API behind RoutingEnginePort. | Do not build custom map engine; isolate provider-specific behaviour behind adapter. |
| D-002 | Synchronous core validation and relational transaction/optimistic concurrency. | Reject invalid state/capacity before persistence and prevent partial updates; concurrency must be tested. |

Alternatives considered include a traditional layered monolith and event-driven microservices. The modular monolith was selected for manageable deployment and lower operational complexity. Broker, Redis distributed lock and independently deployed services are deferred unless measured needs justify them.

## 9. Data, API/service and deployment design
### Logical data model
| Entity | Purpose |
|---|---|
| Customer | Recipient/customer information; parent of delivery orders. |
| DeliveryOrder | Delivery request, address, priority, weight/volume and delivery window. |
| OrderBatch | Dispatcher-created group of orders; lifecycle includes Draft and Optimized. |
| BatchOrder | Associative entity linking orders to a batch. |
| Driver | Driver profile, availability and shift constraints. |
| Vehicle | Vehicle profile and payload weight/volume capacity. |
| DeliveryRoute | Optimized route proposal associated with batch, driver and vehicle. |
| DeliveryStop | Ordered stop on a route, with destination and time window. |
| UserAccount | Authenticated user and role information. |

The demo schema is reduced to order_batch, delivery_route, delivery_stop and idempotency_record. H2 is in-memory and seeded with BATCH-DEMO-001. It is not the production logical relational database or primary/read-replica deployment.

### API contract
`POST /api/routes/optimize` accepts batchId, vehicleId, driverId, vehicle capacity, driver shift limit, total order weight/volume and stops with address, service duration, reachability and delivery-window bounds. The Idempotency-Key header is required. Success returns HTTP 201 with an Optimized route proposal. The documented contract includes 400, 401, 403, 404, 409, 422, 429 and 503; the runnable slice implements only a subset and has no authentication/rate-limit gateway.

### Deployment target and recovery
- Dispatcher web UI, driver web UI and manager view connect over HTTPS to API Gateway.
- Gateway forwards authenticated/rate-limited requests to the Java 21 / Spring Boot modular monolith.
- Application modules use in-process calls and explicit ports; routing/geocoding and notification providers are external adapters.
- Writes and consistency-sensitive reads use the primary relational database; read-only replica receives asynchronous replication and serves only stale-tolerant reads.
- Current demo is one Spring Boot process with in-memory H2 and a synthetic routing adapter; it does not instantiate gateway, production DB, replica, notification provider or live map service.

## 10. Detailed design, patterns and framework
- **Ports and adapters:** RoutingEnginePort isolates external route engine; DeterministicRoutingAdapter supplies offline repeatable output; MapboxAdapter remains a stub.
- **Application service/use-case orchestration:** OptimizeRouteUseCase and RouteOptimizationWorkflow coordinate validation, routing and persistence.
- **Dependency injection:** Spring composition configuration supplies the routing port implementation.
- **Persistence boundary:** JDBC SQL in workflow for narrow demo schema; production repositories and domain aggregates remain incomplete.
- **Transaction script/unit of work:** route, stops, batch transition and idempotency record are committed in one local transaction.
- **Idempotency:** persisted key/result supports replay and duplicate protection; concurrency/replay tests remain outstanding.

Spring Boot 3.3.5 and Java 21 are specified in pom.xml. Spring is used at HTTP, validation, composition and infrastructure boundaries. Domain/application logic is intended to remain framework-independent. Quarkus and microservices/broker deployment were not selected because the repository does not establish a need for native-image/startup optimization or distributed service operations.

## 11. User-interface, accessibility and usability
The overall requirements describe web-based dispatcher, driver and manager experiences. Dispatcher flow: create/select batch → request optimization → inspect ordered stops and estimates → approve/reject → assign available driver/vehicle. Driver flow: view assigned route and next stop → update delivery status → complete delivery or raise exception. Manager flow: select date range → view on-time completion, route variance, distance and fuel estimates.

Driver usability constraints include high contrast, readable route presentation, low interaction while moving and restricting heavy input during In-Transit status. Dispatcher validation should be inline and actionable. Alternative states include empty batch, invalid/over-capacity request, unavailable mapping provider, route conflict, delayed delivery and completed route.

No UI source, wireframes, accessibility audit or user test results were present in the reviewed Phase 2 vertical-slice evidence. This section records design obligations, not a claim that a UI has been delivered or evaluated.

## 12. Implementation vertical slice
The Phase 2 vertical slice is UC-25 route optimization. It crosses REST API, application workflow, routing port/adapter, transactional persistence and response boundary.

| Path | Role |
|---|---|
| `pom.xml` | Java 21 / Spring Boot 3.3.5 Maven build. |
| `src/main/java/com/logistics/LogisticsRouteDeciderApplication.java` | Application entry point. |
| `src/main/java/com/logistics/optimization/api` | Request DTO and REST controller. |
| `src/main/java/com/logistics/optimization/application/RouteOptimizationWorkflow.java` | Orchestration and JDBC transaction. |
| `src/main/java/com/logistics/config/ApplicationConfiguration.java` | Composition root and use-case bean. |
| `src/main/java/com/logistics/optimization/adapters/DeterministicRoutingAdapter.java` | Synthetic routing adapter. |
| `src/main/resources/schema.sql`, `data.sql` | Reduced H2 schema and demo seed. |
| `src/test/java/com/logistics/optimization/RouteOptimizationApiTest.java` | HTTP integration tests. |
| `.github/workflows/lab-11-ci.yml` | Java 21 Maven CI workflow. |

**Run instructions:** JDK 21, Maven 3.9+ and internet access on first build. Run `mvn clean test`, then `mvn spring-boot:run`. Application URL: `http://localhost:8080`; endpoint: `POST /api/routes/optimize`; demo batch: `BATCH-DEMO-001`. The README contains a sample curl request. A successful request consumes the seeded batch by changing it to Optimized. Restarting the application resets the in-memory H2 database and reloads the seed. Use synthetic data only.

## 13. Testing and results
| Test case | Expected/asserted behaviour | Evidence status |
|---|---|---|
| Valid optimization | HTTP 201; route persisted; batch becomes Optimized. | Test source authored; prior CI run passed on earlier commit. |
| Capacity violation | HTTP 422; no route; batch remains Draft. | Test source authored; prior CI run passed on earlier commit. |
| Unreachable stop | HTTP 422; no route persisted. | Test source authored; prior CI run passed on earlier commit. |
| Shift/window boundary | Reject when estimated duration/window violated. | Not covered by current test suite. |
| Provider failure | Controlled 503 and no partial writes. | Not covered. |
| Idempotency replay/concurrency | Same key returns original result; concurrent requests handled safely. | Not covered. |
| Authentication/authorization | Unauthenticated/non-dispatcher request denied. | Not implemented; security blocker. |

CI run 41083022114 (“Lab 11 Java CI”, run 2) completed successfully for application-code commit `cdca3d13c402415487782baab6c76fc32aa09cd1`; its Maven “Run tests” step completed successfully. The subsequent report-only commit does not change application source. A clean-checkout local launch and five-minute demonstration are still required.

No performance/load, security, accessibility, provider-resilience, database replication, production deployment or user-acceptance tests are claimed. A local manual launch and five-minute demonstration remain necessary evidence.

## 14. Reconciliation and change history
| Design expectation | Implementation reality | Reconciliation action |
|---|---|---|
| Three capability modules | Executable slice concentrated in optimization packages; other capabilities incomplete. | Keep architecture model as target; identify incomplete modules as future work. |
| API Gateway and primary/read replica | No gateway or replica in H2 demo. | Do not represent design as deployed; preserve ADR topology. |
| Live map provider via RoutingEnginePort | Synthetic adapter active; Mapbox adapter is a stub. | Disclose synthetic estimates; implement provider adapter and failure tests. |
| Authoritative order/vehicle/driver data | Capacity, shift and order totals request-supplied. | Add repositories and load trusted values from persistence. |
| Lifecycle through assignment and delivery | Only Draft → Optimized implemented. | Implement approval, assignment and later transitions separately. |
| Authentication and role-based privacy | Not implemented. | Security is a gate before shared/operational use. |

The branch history includes Lab 11 documentation, build descriptor, application entry point, API/workflow, synthetic adapter, H2 schema/seed, integration tests and CI workflow. Append a verified contribution log and lecturer approval form.

## 15. Risks, technical debt and release recommendation
| Priority | Risk/debt | Mitigation / gate |
|---|---|---|
| Critical | No dispatcher authentication/authorization; request can supply untrusted capacity/shift/order data. | Implement gateway/API security and authoritative repositories before shared operational use. |
| Critical | Synthetic route distance/duration are not operationally valid. | Integrate external mapping provider with timeout, rate-limit handling, validation and resilience tests. |
| High | H2 in-memory data is lost on restart and does not implement primary/replica. | Use approved relational DB, migrations, backup/restore and replica lag/failover verification. |
| High | Shift/window, provider failure, rollback, idempotency replay and concurrency tests incomplete. | Add boundary, dependency-failure, rollback and concurrency integration tests. |
| High | Simplified equal synthetic leg-time model may misstate ETA. | Use provider-derived per-leg travel time and test delivery-window calculations. |
| Medium | No dispatcher/driver/manager UI or accessibility evidence. | Deliver task flows/wireframes and conduct accessibility/usability checks. |
| Medium | No production monitoring, audit, secret management or operational runbook. | Add correlation IDs, safe logs, metrics, secrets management, backup and incident ownership. |

**Release recommendation: NO-GO for production or real delivery operations.** The branch can be used as a controlled academic demonstration only after the final branch head passes CI and the team performs a clean-checkout local launch and five-minute demo. Do not use the synthetic adapter for dispatch decisions. Before non-production shared use, implement authentication and trusted persisted business data. Before production, replace H2 and synthetic routing, complete resilience/concurrency/security tests, and verify deployment, replication, monitoring, backup and recovery.

## 16. Academic integrity and AI-use record
AI assistance was used to help organize and draft this report from the team's existing repository artefacts, summarize design/implementation evidence, and prepare report files. Repository edits and report wording should be reviewed by all team members. The team remains responsible for accuracy, originality, citation correctness and compliance with University of Botswana/CSI473 rules.

Verification disclosure: the report distinguishes repository source from executed evidence. A successful GitHub Actions run is recorded for application-code commit `cdca3d13c402415487782baab6c76fc32aa09cd1` (run 41083022114). No lecturer approval signature, local manual demo, performance/security testing, production deployment or Moodle submission is claimed. Add any additional tools/assistance used and follow the lecturer's required AI-use disclosure format.

## References
1. University of Botswana, Department of Computer Science. *CSI473 Project Design Report Template*, Semester 1, 2026/27.
2. Geeks4us, *Logistics Route Decider System*, branch `lab-11`: https://github.com/Geeks4us/Logistics-Route-Decider-System/tree/lab-11
3. Repository artefacts: `docs/problem-and-scope.md`; `docs/project-proposal-and-approval.md`; `docs/requirements.md`; `docs/acceptance-criteria.md`; `docs/quality-scenarios.md`; `docs/business-rules.md`; `docs/stakeholders.md`; `docs/crc-cards.md`; `docs/reconciliation-matrix.md`; `docs/technical-debt-and-risks.md`; `decisions/ADR-001-architecture.md`; `decisions/ADR-002-framework.md`; `glossary.md`; `models/logic-data-model.md`; `models/failure-recovery.md`.
4. GitHub Actions, *Lab 11 Java CI*, run 41086170458: https://github.com/Geeks4us/Logistics-Route-Decider-System/actions/runs/41086170458 (successful for commit 2b5c8f71096ee083b81179c1645e960b9c6e354b).

## Appendices
- **Appendix A — Supporting evidence:** signed project approval form; traceability matrix; ADR-001 and ADR-002; detailed UC-25; API contract; logical data model; deployment model; failure/recovery model; test logs; team contribution record.
- **Appendix B — Metadata still to confirm:** lecturer approval decision/date; final submission tag; and any required individual contribution declaration. Do not replace these with guessed values.
