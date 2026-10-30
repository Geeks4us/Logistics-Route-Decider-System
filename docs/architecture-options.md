# Architecture Options Analysis

## Selected: Capability-Oriented Modular Monolith with API Gateway and Replicated Database

The Logistics Route Decider application is one deployable application unit partitioned by business capability:

- **Dispatch & Management** — order batches, drivers, vehicles, route review and assignment.
- **Optimization Engine** — route optimization, stop sequencing and ETA calculation.
- **Tracking & Notification** — delivery progress, status tracking and notifications.

The modules communicate through explicit application interfaces and in-process calls/events. They are not independently deployed microservices.

The deployment includes a **separate API Gateway** between dispatcher/driver clients and the application. The gateway handles ingress concerns such as TLS termination, authentication, rate limiting and request routing. Business rules and authoritative validation remain inside the application.

The application uses **one logical relational database with a writable primary and a read replica**. Writes and consistency-sensitive reads use the primary. The replica may serve explicitly selected read-only queries that tolerate replication lag; it is not an independent source of truth. Failover is not assumed unless separately configured and tested.

External mapping/routing and messaging providers are accessed through application ports and infrastructure adapters.

## Alternative 2: Traditional Layered Monolith

The system is organized by horizontal technical layers (presentation, business logic and data access). This is viable, but capability ownership and boundaries may become less clear as the system grows.

## Alternative 3: Event-Driven Microservices Architecture

The system is split into independently deployed services communicating over a broker or network APIs, often with decentralized data stores. This is not selected for the current phase because independent deployments, broker infrastructure, distributed transactions and cross-service consistency add complexity without a demonstrated need.

## Decision reference

See [ADR-001: Capability-Oriented Modular Monolith with Gateway and Replicated Database](../decisions/ADR-001-architecture.md) for the binding decision, constraints and operational rules.
