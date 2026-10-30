# Lab 7 — Quality Attributes to Architecture Mapping

This document explains how the selected modular-monolith components support the quality scenarios in [quality-scenarios.md](quality-scenarios.md). These are architectural responses and verification plans, not claims that the targets have already been achieved.

## Scenario mapping

| Quality attribute / target | Architectural components and mechanisms | Verification evidence required | Risks and limitations |
|---|---|---|---|
| **Scalability / capacity:** 50 concurrent web users and a 1,000-order test dataset; optimization remains within the 15-minute target | Spring HTTP adapters keep request handling separate from bounded optimization workers. Dispatch and Optimization communicate in-process through `OptimizeRouteUseCase`. One relational database avoids distributed consistency overhead. | Load test with 50 concurrent users and representative 1,000-order data; record latency, throughput, worker saturation, DB connection use, and optimization completion time. | A single application is scaled as a whole. CPU, memory, worker, or database saturation can affect all capabilities. The target must be demonstrated by tests. |
| **Planning efficiency:** 95% of batches up to 100 orders optimized within 15 minutes | Optimization application service delegates routing through `RoutingEnginePort`; bounded asynchronous execution prevents long calculations from occupying interactive HTTP request threads. Dispatch supplies validated batch data through the public use-case contract. | Repeatable benchmark over representative batches, including provider latency/failure; report the percentage completed within 15 minutes and route-quality measures. | End-to-end time depends on the external routing provider, data quality, and optimization workload. No performance guarantee is implied by the architecture alone. |
| **Data integrity:** preserve order details, vehicle assignment, route state, and delivery status for committed transactions | Capability-owned repository ports are implemented by relational adapters. Local transactions protect related state changes. Optimistic version checks prevent stale concurrent assignment updates. | Integration tests for rollback, concurrent assignment conflicts, duplicate submissions where relevant, and persistence round trips. | Shared database schema and transaction boundaries need disciplined ownership. Avoid cross-capability writes that bypass application interfaces. |
| **Dashboard accuracy:** at least 99% consistency with underlying operational data | Dashboard queries read authoritative persisted route, delivery, and fleet data through application/query interfaces; calculations are centralized rather than duplicated in UI components. | Compare dashboard metrics against independently calculated values from controlled test fixtures and production-like data snapshots. | Accuracy depends on metric definitions, source-data quality, and correct calculation logic. |
| **Driver responsiveness:** 95% of route views/status updates within 3 seconds under normal network conditions | Lightweight Tracking application use cases and HTTP adapters handle status updates; notification sending is isolated behind `NotificationPort` so external messaging does not need to block the core state update. | API latency tests for route view/status update; separately measure notification delivery latency and failure rates. | Network and database latency remain external factors. Notifications should be retried/handled separately from committed status changes where appropriate. |
| **Dynamic resequencing:** 95% of affected routes recalculated within 60 seconds | Tracking/Dispatch raises a local event or invokes an application use case; Optimization performs recalculation through its public contract and routing port using bounded workers. | Simulated delayed/unreachable stops; measure event-to-revised-route latency and assert prior valid route state remains available until replacement succeeds. | The 60-second target depends on provider availability and workload. If provider failure occurs, retain the last valid route and surface that resequencing is pending/failed rather than inventing a route. |
| **Reliability / external dependency failure** | `RoutingEnginePort` and `NotificationPort` isolate vendor failures. Finite timeouts, explicit error results, and preserving committed state prevent a provider outage from corrupting core data. | Adapter contract tests, timeout/failure injection, and tests proving failed routing does not change the stored valid route. | Provider availability remains a dependency. A secondary provider or cache is not assumed until separately selected and designed. |
| **Security** | HTTP boundary performs authentication/authorization and request validation; application use cases enforce authorization-sensitive business rules; adapters keep credentials/configuration outside domain code. | Authorization matrix tests, negative access tests, input validation tests, and dependency/configuration review. | The architecture diagram is not a complete security design; authentication, authorization, secret storage, and audit requirements need implementation evidence. |

## Cross-cutting design choices

### In-process capability communication

Dispatch calls `OptimizeRouteUseCase` directly within the same process. This avoids network serialization and broker operations while keeping the optimizer implementation hidden behind a stable contract. Local events may be used for secondary effects such as notification requests.

### Persistence and concurrency

One relational database and local transactions support consistency-sensitive route and driver assignment. Optimistic version checks detect stale updates. A distributed lock service is not part of the Phase 1 design.

### External integrations

Mapping/routing and messaging are outbound dependencies. The core calls `RoutingEnginePort` and `NotificationPort`; infrastructure adapters translate those calls to provider APIs. Finite timeouts and explicit failure handling are required. Provider choice and fallback strategy are separate decisions.

### Framework boundary

Spring Boot is used at the HTTP/infrastructure boundary and in the composition root. Domain models, application services, and ports remain framework-neutral plain Java, consistent with [ADR-002](../decisions/ADR-002-framework.md).

## Verification status

The mappings above describe intended architectural support. They do not constitute performance, availability, security, or accuracy test results. Add measured test reports under `evidence/lab-07/` when those tests have been run.
