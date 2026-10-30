# Domain Model

## Core entities

- `Order`: Represents a single delivery obligation with destination, time window, and priority.
- `Vehicle`: Represents a transport unit with maximum weight, maximum volume, and assigned driver constraints.
- `DeliveryRoute`: Represents a route candidate or active route, including its lifecycle state.
- `Stop`: Represents a location or stop along a route, with sequencing information and completion status.
- `Driver`: Represents the assigned driver and their operational constraints.
- `Dispatcher`: Represents the user responsible for creating and approving route plans.

## Relationships
- A dispatcher creates one or more delivery batches.
- A delivery batch contains multiple orders.
- Each route is associated with one vehicle and one or more stops.
- Each stop belongs to a route and maps to one order.
- A route can transition through states such as Draft, Optimized, Assigned, In Transit, Completed, Delayed, and Exception.

## Architectural intent
The domain model separates operational workflow concerns from external mapping and routing infrastructure. This keeps the route planning process testable while allowing mapping data to be integrated through an external service boundary.
