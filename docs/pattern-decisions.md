# Design Pattern Decisions

| Design Problem in Our System | Selected Pattern/Technique | Participants in the current skeleton | Justification |
|---|---|---|---|
| Dispatch must request route optimization without depending on the optimizer's concrete class. | Dependency Inversion Principle (DIP) / interface-based capability boundary | `DispatchService`, `OptimizeRouteUseCase`, `RouteOptimizationService` | Dispatch depends on the Optimization capability's public application interface. The implementation can change without changing Dispatch. |
| Route optimization must obtain distance/time and route calculations without coupling business logic to a mapping vendor. | Adapter Pattern (Ports and Adapters) | `RoutingEnginePort`, infrastructure adapter (provider to be selected) | The application calls a stable outbound port. An OpenStreetMap or Mapbox adapter can implement it without exposing vendor SDK types to the core. This matches D-001; the provider choice remains open. |
| Route data must be persisted without coupling dispatch logic to a database implementation. | Repository port / Dependency Inversion Principle | `RouteRepository`, `DispatchService`, persistence adapter (to be implemented) | The Dispatch capability owns the persistence contract. A database adapter can implement it while application tests use an in-memory fake. |
| Notifications must be sent without coupling Tracking & Notification to an SMS or WhatsApp vendor. | Adapter Pattern (Ports and Adapters) | `NotificationPort`, messaging adapter (to be implemented) | Provider-specific details remain outside the capability. The core application calls the stable port. |

## Pattern not yet implemented

The Strategy Pattern was proposed for interchangeable optimization algorithms, but the current Lab 9 skeleton delegates route calculation through one `RoutingEnginePort`. `RouteOptimizationStrategy`, `NearestNeighbourStrategy`, and `AlternativeOptimizationStrategy` are not part of the verified skeleton. Add them only if the project actually implements multiple in-house strategies; do not describe them as existing classes.
