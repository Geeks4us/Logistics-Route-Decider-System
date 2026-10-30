# Laboratory 11 — Five-Minute Demonstration Plan

**Project:** Logistics Route Decider System  
**Use case:** UC-25 — Optimize delivery route  
**Data:** synthetic only; no live customer or provider data.

## Preparation
- Install JDK 21 and Maven 3.9+.
- From repository root run `mvn clean test`, then `mvn spring-boot:run`.
- Confirm application is listening on port 8080.
- H2 starts empty and seeds `BATCH-DEMO-001` as Draft with confirmed orders.
- Use the sample request in README.md. Restart the app to reset the in-memory batch after a successful run.

## Five-minute sequence
| Time | Action | Evidence |
|---|---|---|
| 0:00–0:45 | Explain UC-25 and synthetic Draft batch | Seed row in `data.sql`; H2 `order_batch` |
| 0:45–1:45 | POST valid optimization request | `POST /api/routes/optimize`, controller and workflow |
| 1:45–2:45 | Show HTTP 201 response, ordered stops and synthetic estimate | DeterministicRoutingAdapter and response JSON |
| 2:45–3:45 | Query `delivery_route`, `delivery_stop` and `order_batch` | Persisted route, stops and Draft → Optimized |
| 3:45–5:00 | Run capacity/unreachable failure tests | HTTP 422 and no partial route/batch mutation assertions |

## Honest limitations to disclose
- DeterministicRoutingAdapter is synthetic and not live Mapbox.
- H2 is in-memory demonstration persistence, not the primary/read-replica deployment design.
- Authentication/authorization and full order/vehicle/driver repositories are not implemented.
- Automated tests are authored but must be run locally; do not claim pass until output is observed.
