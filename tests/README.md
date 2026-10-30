# Automated Tests — Laboratory 11

No executable automated tests were present in the reviewed lab-09 source tree, and no Maven/Gradle build descriptor was present. Consequently, no test command or passing test count is claimed.

## Required test set before release review

| Test category | Required scenario | Expected assertion |
|---|---|---|
| Unit — normal | Valid Draft batch with confirmed orders and feasible vehicle/driver | Optimized stop sequence returned; route saved; batch transitions to Optimized |
| Unit — business rule | Aggregate order weight or volume exceeds vehicle capacity | Validation error; no route save |
| Unit — business rule | ETA outside delivery window or route exceeds shift | Validation error; no route save |
| Integration — authorization | Caller is not an authorized dispatcher | HTTP 401/403; no state change |
| Integration — dependency failure | Routing provider times out or returns unreachable stop | Controlled 503/422; no route persisted; batch remains Draft |
| Integration — transaction | Failure while saving route/stops | Transaction rolls back all writes |

When implemented, put test classes under src/test/java (or documented test root), run from a clean checkout, and append exact command, date, test count and result. Assert important side effects and database state, not merely method completion.

## Execution record
- Build tool: not configured.
- Test command: unavailable until build descriptor is added.
- Result: **NOT RUN**.
- Reason: no build descriptor or automated test sources in baseline.
