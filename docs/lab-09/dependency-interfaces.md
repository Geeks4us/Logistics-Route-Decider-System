# Lab 9 — Interfaces, Code Skeletons, and Dependency Direction

## Architecture boundary

This design follows the **Capability-Oriented Modular Monolith** selected in ADR-001. The system is one deployable application partitioned by business capability:

- **Dispatch & Management** — order batches, route review/approval, and driver assignment.
- **Optimization Engine** — route generation and optimization strategies.
- **Tracking & Notification** — route progress and outbound notifications.

The interfaces below are Java skeletons, not a complete implementation. They make dependency direction explicit and give each capability a stable contract.

## Dependency direction

```text
                 Spring HTTP / UI adapters
                           |
                           v
                 Dispatch application
                    |            |
                    v            v
             RouteRepository   OptimizeRouteUseCase
                 (port)          (public capability API)
                    ^                    ^
                    |                    |
          Persistence adapter    Optimization application
                                         |
                                         v
                                  RoutingEnginePort
                                         ^
                                         |
                                  MapboxAdapter

          Tracking application ---> NotificationPort
                                           ^
                                           |
                                  SMS / WhatsApp adapters

Spring composition root wires concrete adapters to these interfaces.
```

**Rule:** application and domain code depend inward on domain-owned interfaces. Infrastructure adapters depend on those interfaces by implementing them. Dispatch calls the Optimization capability through its public use-case interface; it does not import the optimizer's concrete implementation. No core module imports a database, mapping-provider, SMS, or WhatsApp SDK.

## Interface and skeleton catalogue

| Type | Capability / layer | Purpose |
|---|---|---|
| `RouteRepository` | Dispatch port | Persist and retrieve route aggregates |
| `OptimizeRouteUseCase` | Optimization public API | Contract used by Dispatch to request optimization |
| `RouteOptimizationService` | Optimization application | Implements the optimization use case |
| `RoutingEnginePort` | Optimization outbound port | Hides the external routing provider |
| `MapboxAdapter` | Optimization infrastructure | Example provider adapter implementing the port |
| `NotificationPort` | Tracking outbound port | Hides SMS/WhatsApp provider details |
| `DispatchService` | Dispatch application | Coordinates dispatch use cases through ports |

See the Java skeletons under `src/main/java/com/logistics/`.

## Dependency checks

1. Domain and application packages must not import `org.springframework..*`, provider SDKs, or persistence implementation packages.
2. Adapters may import framework and vendor libraries, and must implement a port owned by the relevant capability.
3. Cross-capability calls must use a published application interface or a local event, not another module's concrete class.
4. Unit tests should substitute port implementations with fakes/mocks; adapter integration tests verify external integrations separately.

## Framework decision

The backend framework decision is recorded in [ADR-002](../../decisions/ADR-002-framework.md): Java 21 with Spring Boot 3.x. Spring is used at the application and infrastructure edges; the interfaces and domain remain plain Java.
