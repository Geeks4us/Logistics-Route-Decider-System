# Lab 9 — Interfaces, Code Skeletons, and Dependency Direction

## Architecture boundary

This design follows the **Capability-Oriented Modular Monolith** selected in ADR-001. The system is one deployable application partitioned by business capability:

- **Dispatch & Management** — order batches, route review/approval, and driver assignment.
- **Optimization Engine** — route generation and optimization.
- **Tracking & Notification** — route progress and outbound notifications.

The interfaces below are Java skeletons, not a complete implementation. They make dependency direction explicit and give each capability a stable contract.

## Dependency direction

```text
             Spring HTTP / persistence / provider adapters
                               |
                               v
                    Capability application services
                       |                  |
                       v                  v
               Domain model       Capability-owned ports
                                          ^
                                          |
                         Infrastructure adapters implement ports

Dispatch application ---> OptimizeRouteUseCase (Optimization public API)
Optimization application ---> RoutingEnginePort (outbound mapping API)
Tracking application ---> NotificationPort (outbound messaging API)

Spring composition root wires application services and adapter implementations.
```

**Rule:** domain and application code are framework-neutral and depend inward on domain-owned interfaces. Infrastructure adapters implement those interfaces and may depend on Spring, persistence libraries, or vendor SDKs. Dispatch calls the Optimization capability through its public use-case interface; it does not import the optimizer's concrete implementation. No core module imports a database implementation, mapping-provider SDK, SMS SDK, or WhatsApp SDK.

## Interface and skeleton catalogue

| Type | Capability / layer | Purpose |
|---|---|---|
| `RouteRepository` | Dispatch outbound port | Persist and retrieve route aggregates |
| `OptimizeRouteUseCase` | Optimization public API | Contract used by Dispatch to request optimization |
| `RouteOptimizationService` | Optimization application | Implements the optimization use case |
| `RoutingEnginePort` | Optimization outbound port | Hides the external routing provider |
| `NotificationPort` | Tracking outbound port | Hides SMS/WhatsApp provider details |
| `DispatchService` | Dispatch application | Coordinates dispatch use cases through ports |

The concrete mapping and messaging adapters are infrastructure implementations of these ports. The provider has not been finalized by D-001 (which permits OpenStreetMap or Mapbox), so provider-specific adapter names should be treated as examples until a provider is selected.

## Dependency checks

1. Domain and application packages must not import `org.springframework..*`, provider SDKs, or persistence implementation packages.
2. Adapters and the composition root may import framework and vendor libraries; adapters must implement a port owned by the relevant capability.
3. Cross-capability calls must use a published application interface or a local event, not another module's concrete class.
4. Unit tests should substitute port implementations with fakes/mocks; adapter integration tests verify external integrations separately.

## Framework decision

The backend framework decision is recorded in [ADR-002](../../decisions/ADR-002-framework.md): Java 21 with Spring Boot 3.x. Spring is used for HTTP/infrastructure adapters and composition-root wiring; domain, ports, and application services remain plain Java.
