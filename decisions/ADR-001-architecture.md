# ADR-001: Capability-Oriented Modular Monolith with Gateway and Replicated Database

- **Status:** Accepted
- **Date:** 2026-10-30

## Context

The Logistics Route Decider System needs clear business-capability boundaries, a controlled public API entry point, and a deployment topology that supports read scaling and database resilience. The deployment includes dispatcher web and driver mobile clients, an API Gateway, the application, a primary relational database and a database replica, plus external mapping/routing and messaging providers.

A modular monolith describes the application code and its business-module boundaries; it does not require every infrastructure component to run in the same process.

## Decision

Adopt a **capability-oriented modular monolith behind an API Gateway, using one logical relational database with a primary and a replica**.

### Application structure

The Logistics Route Decider application is one deployable application unit partitioned into three capability modules:

1. **Dispatch & Management** — order batches, drivers, vehicles, route review and assignment.
2. **Optimization Engine** — route optimization, stop sequencing and ETA calculation.
3. **Tracking & Notification** — delivery progress, status tracking and notifications.

Modules communicate through explicit application interfaces and in-process calls/events. Domain and application logic remain independent of Spring and vendor SDKs. Infrastructure adapters implement ports for persistence, routing/mapping and messaging. The modules are not independently deployed microservices and do not communicate through a message broker in this phase.

### API Gateway

The **API Gateway is a separate edge component** between browser/mobile clients and the application. It handles ingress concerns such as TLS termination, authentication/token verification, rate limiting, request-size limits and request routing. It forwards API requests to the application over a protected connection.

The gateway does not own business rules, route optimization, delivery state transitions or persistence. Gateway validation is limited to transport/schema-level checks; authoritative domain validation is performed by the application. The gateway may be scaled independently without changing the modular-monolith boundary.

### Database topology

Use **one logical relational database with a writable primary and a read replica**:

- All inserts, updates, deletes, transactions and consistency-sensitive reads go to the primary.
- The replica receives changes from the primary and may serve explicitly designated read-only queries that tolerate replication lag, such as selected dashboard/history views.
- Reads that must immediately observe a preceding write—including route assignment, driver availability, batch lifecycle and duplicate-assignment checks—must use the primary or another mechanism that guarantees read-your-writes.
- The replica is not an independent source of truth. Application code must not write to it.
- Replication lag, replica health and failover status must be monitored. If the replica is stale or unavailable, eligible reads fall back to the primary or fail according to operational policy; writes continue to target the primary.
- Automatic promotion/failover is not assumed by this ADR. It must be explicitly configured, tested and documented before being represented as an operational guarantee.

The primary database transaction is the consistency boundary for related order, batch, route, driver and vehicle changes. Use foreign keys, unique constraints, transactions and optimistic version checks where appropriate.

### External services

The Optimization Engine accesses the external mapping/routing provider through a RoutingEnginePort. Tracking & Notification accesses the messaging provider through a NotificationPort. Provider-specific SDKs and network details remain in infrastructure adapters. Timeouts, bounded retries and clear failure responses prevent unavailable providers from creating partial or invalid business state.

## Consequences

### Positive

- Capability boundaries remain explicit while the application is straightforward to develop and deploy.
- The API Gateway provides a single controlled ingress point and can scale independently.
- Read-heavy, stale-tolerant queries can be offloaded to the replica.
- The primary remains authoritative, preserving a clear transaction and integrity boundary.
- External provider failures are isolated behind ports and adapters.

### Trade-offs and controls

- The gateway adds a network hop and becomes an operational component that must be monitored and secured.
- Replica reads may be stale. The application must route consistency-sensitive reads to the primary and document which queries may use the replica.
- Replication does not replace backups, transaction integrity or a tested recovery plan.
- Primary failure may interrupt writes unless a separately configured and tested failover procedure exists.
- A single application deployment still has a shared release and runtime failure domain; the gateway and database topology do not turn the capability modules into microservices.

## Alternatives considered

1. **Single-process application with no gateway or replica:** simpler, but does not meet the selected deployment topology's explicit ingress and read-replication requirements.
2. **Traditional layered monolith:** feasible, but provides weaker business-capability boundaries as the system grows.
3. **Event-driven microservices with decentralized databases:** rejected for this phase because it introduces independent service deployments, network messaging, distributed transactions and cross-service consistency work that are not currently justified.

## Architectural rules

- Do not split Dispatch, Optimization or Tracking into independently deployed services without a new architecture decision.
- Do not bypass the API Gateway for normal client traffic.
- Do not place business rules in the gateway.
- Do not write to the database replica.
- Do not serve read-after-write or other consistency-sensitive operations from a potentially lagging replica.
- Do not claim automatic database failover unless the deployment configuration and recovery tests demonstrate it.
- Keep provider-specific types out of domain and application modules.
