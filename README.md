# Logistics Route Decider System

CSI473 team project — the University Service Hub in the Laboratory 11 handout is only a lecturer example and is not this team's project.

## Architecture
- Java 21 / Spring Boot 3.x target.
- Capability-oriented modular monolith: Dispatch & Management, Optimization Engine, Tracking & Notification.
- API Gateway remains the intended edge component; the logical relational database retains primary/read-replica roles.
- Routing and notification integrations are represented by outbound ports.

## Laboratory 11 vertical slice
This branch adds an executable Spring Boot HTTP slice for UC-25 route optimization:
1. POST request reaches the REST controller.
2. Workflow checks the seeded Draft batch and confirmation state, capacity, reachability, delivery windows and driver shift.
3. A deterministic synthetic routing adapter supplies a repeatable offline route estimate.
4. Route, ordered stops, batch lifecycle and idempotency key are committed in one JDBC transaction to H2.
5. Validation/provider errors return controlled HTTP responses; failed transactions do not persist a route.

**Scope limitations:** H2 is an in-memory demonstration database, not the approved production relational database/replica topology. The routing adapter is synthetic and is not live Mapbox. Vehicle/driver/order values are supplied in the demo request rather than loaded from full domain repositories. Authentication/authorization, real mapping, notifications, primary/replica deployment and operational controls are not implemented in this slice.

## Requirements
- JDK 21
- Maven 3.9+
- Internet access on first build to resolve Maven dependencies

## Run
```bash
mvn clean test
mvn spring-boot:run
```
The app starts at `http://localhost:8080`. H2 is in-memory; the synthetic batch `BATCH-DEMO-001` is seeded as Draft with confirmed orders.

## Example request
```bash
curl -i -X POST http://localhost:8080/api/routes/optimize \
  -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: demo-run-001' \
  -d '{
    "batchId":"BATCH-DEMO-001",
    "vehicleId":"VAN-01",
    "driverId":"DRIVER-01",
    "vehicleMaxWeightKg":1000,
    "vehicleMaxVolumeM3":10,
    "driverShiftLimitMinutes":240,
    "totalWeightKg":250,
    "totalVolumeM3":2,
    "stops":[
      {"address":"Gaborone CBD","serviceMinutes":10,"reachable":true,"windowStartMinute":0,"windowEndMinute":180}
    ]
  }'
```
Expected first response: HTTP 201 with an Optimized route, ordered stop, synthetic distance/duration. Each successful request consumes the demo batch; restart the app to reset the in-memory database and seed data. Reuse of the same idempotency key returns the original route.

## Tests
`src/test/java/com/logistics/optimization/RouteOptimizationApiTest.java` contains HTTP integration tests for success/persistence, capacity rejection/no write, and unreachable-stop rejection/no write. Run `mvn test`; record the actual output before claiming the tests pass.

## Repository guide
- `src/main/java` — application, API, workflow and adapters.
- `src/main/resources/schema.sql`, `data.sql` — demo schema and synthetic seed.
- `src/test/java` — automated tests.
- `docs/reconciliation-matrix.md` — requirement/model/code/test reconciliation.
- `docs/technical-debt-and-risks.md` — residual risks and release recommendation.
- `docs/lab-11-demo-plan.md` and `docs/lab-11-checklist.md` — lab evidence.

Use synthetic data only. Never commit customer personal data, access tokens or provider credentials.
