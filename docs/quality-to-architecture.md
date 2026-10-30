# Quality Scenarios Mapped Across Architecture Alternatives

## 1. Scalability

**Scenario:** Support at least 50 concurrent web users and 1,000 delivery orders while maintaining route optimization within 15 minutes.

- **Selected architecture:** The API Gateway provides a controlled ingress point and can be scaled independently. The application uses asynchronous workers for optimization. The database replica may serve designated read-heavy dashboard/history queries that tolerate replication lag. Optimization writes and consistency-sensitive reads remain on the primary.
- **Traditional layered monolith:** Can meet the scenario with sufficient capacity, but capability boundaries may become harder to maintain.
- **Event-driven microservices:** Offers independent service scaling, but introduces broker, deployment and distributed-state complexity not currently justified.

## 2. Planning efficiency

**Scenario:** Generate an optimized route within 15 minutes for at least 95% of batches containing up to 100 orders.

- **Selected architecture:** Dispatch invokes the Optimization Engine through an in-process application interface. The gateway routes the client request but does not participate in optimization logic. The optimization worker uses the external routing provider through a port and persists the result transactionally to the primary database.
- **Traditional layered monolith:** Can support the use case, but horizontal-layer dependencies may make capability ownership less explicit.
- **Event-driven microservices:** Adds network and broker boundaries to the optimization workflow.

## 3. Data integrity

**Scenario:** Preserve order details, vehicle/driver assignment, route state and delivery status without unintended duplication or loss.

- **Selected architecture:** The primary relational database is authoritative. Related state changes are committed in a local transaction, protected by foreign keys, uniqueness constraints and optimistic version checks. The replica is read-only and is not used for decisions that require read-after-write consistency.
- **Traditional layered monolith:** A shared relational database can provide strong transactional integrity, but unclear layer ownership can permit accidental cross-layer mutations.
- **Event-driven microservices:** Decentralized stores require eventual-consistency handling and distributed workflow coordination.

## 4. Availability and recovery

**Scenario:** An unavailable mapping or messaging provider must not create a partially persisted route or corrupt delivery state.

- **Selected architecture:** Infrastructure adapters enforce timeouts and bounded retries. The application returns a clear failure response and leaves the batch/route in its prior valid state when optimization cannot complete. The gateway propagates the application error response. Primary/replica health and replication lag are monitored separately.
- **Traditional layered monolith:** Can isolate provider calls, but boundaries may be less explicit.
- **Event-driven microservices:** Can isolate failures by service, but recovery requires coordination across distributed services and data stores.

**Verification note:** These are architectural responses to the target scenarios, not evidence that performance, availability or recovery targets have already passed tests. Record test results separately.
