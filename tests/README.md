# Automated Tests — Laboratory 11

## Implemented test coverage
`src/test/java/com/logistics/optimization/RouteOptimizationApiTest.java` uses Spring Boot MockMvc and an H2 test database.

| Test | Behaviour asserted | Side effect asserted |
|---|---|---|
| `persistsRouteAndTransitionsBatchOnSuccess` | Valid request returns HTTP 201 and Optimized result | Batch status changes to Optimized; one route row is persisted |
| `rejectsCapacityAndDoesNotPersistRoute` | Capacity violation returns HTTP 422 | No route row; batch remains Draft |
| `rejectsUnreachableStopWithoutCreatingRoute` | Unreachable stop returns HTTP 422 | No route row |

The test fixture resets route, stop and idempotency rows and restores the synthetic Draft batch before each test.

## Run
```bash
mvn clean test
```

## Execution record
- Test sources: added on branch lab-11.
- Execution result: **NOT RUN in this editing session**; no Maven/JDK execution environment was available through the repository editing connection.
- Do not claim a passing test count until the command is run from a Java 21/Maven 3.9+ environment and its actual output is recorded.

## Additional tests still required
- Driver shift and delivery-window boundary cases.
- Missing/invalid idempotency header and repeated-key replay.
- Missing batch and non-Draft/unconfirmed batch.
- Simulated routing dependency exception (controlled 503, no writes).
- Authentication/authorization (not implemented in this slice).
- Database rollback on a forced persistence failure.
