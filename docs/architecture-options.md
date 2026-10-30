# Lab 7 — Architecture Options Analysis

## Evaluation criteria

The alternatives are evaluated against the project's current constraints and quality scenarios:

- Phase 1 is delivered by one team and does not require independent service release cycles.
- The system targets 50 concurrent web users and a 1,000-order test dataset.
- Route optimization targets batches of up to 100 orders, completing within 15 minutes for at least 95% of batches.
- Committed order, route, driver, and delivery state must remain consistent.
- External mapping and messaging providers must be replaceable without leaking vendor types into business logic.
- Operational complexity should be proportionate to the demonstrated scale.

## Options

### Option 1 — Capability-Oriented Modular Monolith (selected)

**Structure:** One deployable application divided into Dispatch & Management, Optimization Engine, and Tracking & Notification capability modules. Each capability owns its application services, domain model, and public ports. Internal collaboration uses in-process application-interface calls and local events.

**Advantages**
- Capability responsibilities and change ownership are explicit.
- Internal calls avoid network serialization and broker latency.
- One relational database supports local transactions for consistency-sensitive operations.
- A single deployment artifact is simpler to build, test, operate, and monitor.
- Ports isolate database, mapping, and notification implementations.
- A capability can be extracted later if operational evidence warrants it.

**Trade-offs**
- The whole application is deployed and scaled together.
- Module boundaries require discipline and automated dependency checks.
- Optimization workers share runtime resources with interactive requests and need bounded concurrency.

### Option 2 — Traditional Layered Monolith (not selected)

**Structure:** The system is organized primarily into presentation, business, and data-access layers, with capabilities spread across those horizontal layers.

**Advantages**
- Familiar structure and straightforward initial setup.
- One deployment and one relational database.
- Local transactions are available.

**Trade-offs**
- Capability logic is distributed across technical layers, obscuring business ownership.
- Changes to one workflow can touch many shared layer packages.
- Shared service/repository abstractions can become tightly coupled and encourage cross-capability access.
- It is possible to implement modularity inside a layered monolith, but a purely horizontal decomposition does not make those boundaries first-class.

### Option 3 — Event-Driven Microservices (not selected for Phase 1)

**Structure:** Dispatch, optimization, and tracking/notification run as independently deployed services and communicate over network APIs and/or a message broker. Services own separate data stores.

**Advantages**
- Independent deployment and scaling of capabilities.
- Process isolation can limit some failures.
- Teams can evolve service technology independently.

**Trade-offs**
- Network failures, retries, duplicate messages, schema/version management, and distributed tracing become core concerns.
- Cross-service workflows introduce eventual consistency and potentially sagas or compensating actions.
- Separate data stores complicate atomic route/driver/order assignment.
- Broker, deployment, security, monitoring, and on-call operations increase cost.
- These benefits are not currently required by the Phase 1 scale and team structure.

## Decision

Select **Option 1 — Capability-Oriented Modular Monolith** for Phase 1. The decision and its consequences are recorded in [ADR-001](../decisions/ADR-001-architecture.md).

The design is not a claim that a monolith is universally superior. It is the lowest-complexity option that can address the current quality goals while preserving explicit capability boundaries and a future extraction path.

## Summary comparison

| Criterion | Modular monolith | Layered monolith | Microservices |
|---|---|---|---|
| Deployment | One artifact | One artifact | Multiple services |
| Primary decomposition | Business capabilities | Technical layers | Independently deployable capabilities |
| Internal communication | In-process interfaces/local events | In-process layer calls | Network APIs/events |
| Phase 1 data consistency | Local relational transactions | Local relational transactions | Distributed workflow consistency |
| Independent scaling | No | No | Yes |
| Operational complexity | Low–moderate | Low | High |
| Fit for current Phase 1 | **Best fit** | Acceptable but weaker capability boundaries | Over-engineered at present |
