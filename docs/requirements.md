Functional Requirements
FR01: The system shall allow a dispatcher to create a batch of delivery orders with destination address, time window, and priority level.
FR02: The system shall generate an optimized delivery route for a given vehicle based on order priority, time windows, and vehicle capacity.
FR03: The system shall reject any route assignment that exceeds a vehicle's maximum weight or volume capacity.
FR04: The system shall allow a dispatcher to approve or reject a proposed route before it is assigned to a driver.
FR05: The system shall update a route's status through defined states (Draft, Optimized, Assigned, In Transit, Completed, Delayed, Exception) and reject invalid state transitions.
FR06: The system shall automatically resequence a route when a stop becomes unreachable or a delivery is delayed.
FR07: The system shall notify the assigned driver when a dispatcher modifies an active route.
FR08: The system shall restrict access to delivery addresses and customer contact details to only the assigned driver and dispatcher, based on role.
FR09: The system shall retrieve distance and travel time data from an external mapping API for route calculation.
FR10: The system shall display, for each manager query, the on time delivery completion rate over a selected date range.
