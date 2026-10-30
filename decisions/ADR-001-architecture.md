# ADR-001: Capability-Oriented Modular Monolith

- **Status:** Accepted
- **Date:** 2026-10-30
- **Decision owners:** Group 01
- **Scope:** Phase 1 architecture for the Logistics Route Decider System

## Context

The system supports dispatchers creating delivery orders and batches, requesting route optimization, reviewing and assigning routes, drivers viewing assigned routes and updating delivery status, and managers monitoring fleet and delivery performance.

The architecture must support the quality goals in `docs/quality-scenarios.md`, including 50 concurrent web users, a test dataset of 1,000 orders, optimization of batches of up to 100 orders within 15 minutes for at least 95% of batches, common driver operations within 3 seconds for at least 95% of requests under normal network conditions, and preservation of committed order and route state.

Phase 1 is a small system with a single team and no demonstrated requirement for independently deployed services, multi-region operation, or independently scaled capability modules. The design should keep business capabilities cohesive while avoiding distributed-system overhead.

## Decision

Adopt a **Capability-Oriented Modular Monolith**: one deployable application, internally partitioned into explicit business-capability modules with controlled interfaces and inward dependency direction.

The initial capability modules are:

1. **Dispatch & Management** — order and batch management, route review/approval, driver and vehicle assignment, and operational dashboard queries.
2. **Optimization Engine** — route optimization use cases and the boundary to an external mapping/routing provider.
3. **Tracking & Notification** — driver progress/status updates and outbound customer/dispatcher notifications.

The application is deployed as one artifact. Modules communicate through in-process calls to published application interfaces and local events for secondary effects. Phase 1 uses one relational database and local database transactions for consistency-sensitive updates.

## Component and dependency structure

- HTTP controllers and other framework adapters translate external requests into application use cases.
- Application services coordinate use cases and depend on domain models and capability-owned ports.
- Domain models contain business rules and do not depend on Spring, persistence implementations, or vendor SDKs.
- Outbound adapters implement ports for relational persistence, routing/geospatial services, and messaging.
- The Spring composition root wires application services to adapter implementations.
- Dispatch requests optimization through the Optimization capability's public `OptimizeRouteUseCase`; it does not depend on the optimizer's concrete implementation.
- The Optimization capability calls an external routing provider through `RoutingEnginePort`. The provider is not fixed by this ADR; the provider decision remains subject to D-001.
- Tracking & Notification uses `NotificationPort` so vendor-specific SMS/WhatsApp details remain outside the core.

See [component architecture](../models/component-architecture.md) and [quality-to-architecture mapping](../docs/quality-to-architecture.md).

## Deployment and data constraints

- One deployable application artifact; no independently deployed Phase 1 services.
- One relational database; use local transactions for route/driver/order state changes.
- No message broker is required in Phase 1. Local events or managed in-process asynchronous execution may be used where useful.
- Long-running optimization work may use managed worker threads, but this does not create a separately deployed service.
- External provider calls must use finite timeouts. If routing is unavailable, do not persist a fabricated or partially calculated route; retain the last valid route state and report the failure.
- Protect concurrent assignment updates with database transactions and optimistic version checks. Do not introduce distributed locks for a single-deployment design without a new evidence-based decision.

## Alternatives considered

### Option 1 — Capability-Oriented Modular Monolith (selected)

**Benefits:** clear business ownership, cohesive capabilities, in-process calls without network serialization, local transactions, simple deployment and operations, and the ability to extract a capability later if evidence justifies it.

**Costs:** modules share a deployment lifecycle and runtime resources; module boundaries must be enforced through code review, package structure, and tests rather than separate processes.

### Option 2 — Traditional Layered Monolith (rejected)

A purely horizontal presentation/business/data-access organization is simple initially, but it scatters capability behaviour across layers and makes ownership and change impact harder to reason about as dispatch, optimization, and tracking evolve. The selected design may still use technical layers inside each capability; it rejects making horizontal layers the primary system decomposition.

### Option 3 — Event-Driven Microservices (rejected for Phase 1)

Independent services could scale and deploy separately, but would require network contracts, service discovery/configuration, distributed observability, broker operations, data ownership boundaries, and eventual-consistency or distributed-transaction handling. No current workload or team constraint demonstrates that these costs are justified for the stated Phase 1 scale.

## Consequences

**Positive**
- Capability boundaries are explicit and map to the system's principal user workflows.
- In-process collaboration avoids network overhead between internal capabilities.
- Local relational transactions support strong consistency for order, route, and assignment changes.
- Ports isolate persistence, mapping, and notification vendors from business logic.
- A future extraction path exists if a capability develops a measured need for independent scaling or deployment.

**Negative / constraints**
- The application is scaled and released as a whole in Phase 1.
- A slow optimization task can consume local resources; worker limits, queue bounds, and load testing are needed.
- A single database remains a shared operational dependency.
- Module boundaries can erode unless dependency rules are tested and reviewed.

## Revisit triggers

Reconsider this ADR only when evidence shows one or more of the following: sustained load that cannot meet the documented quality scenarios with vertical/resource tuning; a capability requiring an independent release cadence; a distinct availability/security boundary; or a team/ownership structure that warrants independent service operation. Any extraction must include an explicit data ownership and consistency plan.

## Explicit rejection

**Do not adopt microservices, a network message broker, Kubernetes, Redis-based distributed locks, or separate databases for Phase 1 merely in anticipation of future scale.** These mechanisms solve distributed deployment problems that the selected single-deployment architecture does not currently have. Introduce them only through a new ADR supported by measured requirements.
