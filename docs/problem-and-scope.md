Candidate Problem Evaluation
The candidate problem is the operational inefficiencies, high costs, and delays caused by reliance on manual, static, and unoptimized route planning in logistics and fleet delivery operations. The overall need can be summarised as the need for an automated, constraint-aware route optimization and dynamic dispatch system that replaces manual route planning, improves fleet resource utilization, and provides real-time visibility across logistics operations.
For a Logistics Route Design System, stakeholder access evaluates where and how you will obtain real-world data and feedback for each role:
	Fleet Dispatchers / Route Planners: Accessible through interviews or observations with administrative personnel at local courier services, university campus transport offices, or retail distribution hubs. 
	Delivery Drivers: Accessible via short surveys or informal field interviews with local delivery drivers, campus shuttle operators, or regional freight transporters. 
	Logistics Managers: Accessible by consulting with transport operational managers or reviewing published operational case studies, transport policy documents, and fleet management industry reports. 
	End Customers / Recipients: Accessible directly within your student team or campus community by collecting feedback on delivery experience, tracking expectations, and common delivery failure points.
1. Complex Domain Constraints
The system must balance multiple competing dynamic variables simultaneously:
	Vehicle Constraints: Maximum payload weight, volumetric capacity, and driver shift limits.
	Customer Constraints: Specific delivery time windows (e.g., 09:00–11:00 AM) and order priorities.
	Spatial/Temporal Constraints: Distance matrices, dynamic speed limits, and travel time variability.
2. Stateful Workflow Lifecycle
Routes and orders do not exist in a static state; they progress through an event-driven lifecycle:
"Draft" ⟶ "Optimized" ⟶ "Assigned" ⟶ "In-Transit" ⟶ {("Completed" @"Delayed / Exception")}
Each state transition enforces strict business rules (e.g., a driver cannot mark a route as In-Transit without dispatcher approval and capacity validation).
3. Rule Enforcement & Business Logic
	Dynamic Re-sequencing: Rules for automatically dropping an unreachable stop or shifting delayed deliveries to an available secondary vehicle.
	Over-capacity Prevention: Algorithmic checks preventing an order from being assigned if it violates the assigned vehicle's total weight/volume capacity.
4. Non-Trivial Quality Trade-Offs
	Execution Time vs. Route Precision: Choosing between instant heuristic route generation versus compute-intensive optimal sequencing during peak load times.
	Real-time Synchronization vs. Battery/Data Usage: Deciding driver location ping frequencies to avoid overwhelming network throughput while maintaining accurate ETA estimates.
5. Architectural Separation & External Integration
	System Boundaries: Clear separation between business rules (route sequencing), persistent data (dispatch logs), and external subsystems (third-party mapping and geocoding services like OpenStreetMap).

1. Technical & Operational Feasibility
	Semester Scope Isolation: Core route-matrix computations are delegated to external Map APIs (OpenStreetMap/Map box) via Decision D-001. This keeps the development scope focused on orchestration, state management, and business logic. 
	Stack Compatibility: Built on standard web/desktop stacks (Java/SQL backend, web frontend) to manage stateful route workflows, spatial distance matrices, and interactive UI map displays without special hardware. 
	Mock Data Viability: Simple to validate using Gaborone street addresses, synthetic customer order batches, and vehicle payload configurations.
2. Data Constraints
	PII & Data Protection: Strict Role-Based Access Control (RBAC) ensures delivery addresses, contact numbers, and order contents are visible only to dispatchers and the assigned driver.
	Concurrency & Integrity: Database locks prevent race conditions, such as dual dispatching a vehicle or driver status overrides on cancelled orders.
	Payload Optimization: GPS pings from drivers are throttled/batched to minimize system network overhead and external API costs.
3. Safety & Regulatory Constraints
	Driver Safety (Minimizing Distraction): Driver UI enforces high-contrast, hands-free readable route views that restrict heavy user input while the vehicle status is marked In-Transit.
	Regulatory Workload Hours: Business rules strictly enforce maximum continuous driving hours and mandatory rest breaks prior to route generation.
	Vehicle Load Enforcement: Hard systemic checks block route finalization if total assigned parcel weights or volumes exceed maximum legal vehicle payload limits.


	Problem Statement
Logistics operators and fleet dispatchers currently face critical operational bottlenecks due to reliance on manual, static, and unoptimized route planning processes. Route planners depend on outdated personal experience or disconnected spreadsheet models, which fail to dynamically accommodate real-time delivery constraints, vehicle payload limits, driver shift regulations, fluctuating fuel costs, and unexpected traffic delays. This reliance on manual routing leads to excessive fuel consumption, long route distances, frequent delivery delays, and inflated operational expenses. Furthermore, delivery drivers in the field lack real-time updates when route adjustments or customer cancellations occur during active dispatch runs. Management lacks centralized visibility into real-time fleet performance metrics, route variance, and operational costs, preventing data-driven decision-making. Without an automated, constraint-aware route design system that optimizes delivery sequences and enforces load capacities while managing stateful workflows, logistics organizations will continue to suffer from high operational overhead, driver burnout, and inconsistent customer service quality.

2. Project Goal
	Goal: To design and implement a stateful route design and optimization system that automates vehicle delivery sequencing, enforces operational constraints, and dynamically manages dispatch workflows to reduce logistics operational costs. 
Measurable Objectives
	Planning Efficiency: Reduce the average daily planning time for dispatchers from 2 hours to under 15 minutes per batch of delivery orders.
	Fuel & Distance Optimization: Decrease total distance driven and fleet fuel consumption by at least 15% across active delivery routes compared to baseline manual planning. 
	On-Time Delivery Performance: Increase the overall on-time delivery completion rate from 78% to over 92% within the initial operational deployment phase.
In Scope
•	Automated route optimization engine that sequences delivery stops based on vehicle capacity, time windows, and priority orders.
•	Dispatcher interface for creating, reviewing, and approving optimized routes before assignment.
•	Stateful order and route lifecycle management (Draft, Optimized, Assigned, In Transit, Completed, Delayed, Exception) with enforced transition rules.
•	Dynamic resequencing logic for unreachable stops or delayed deliveries.
•	Overcapacity prevention checks (weight and volume validation before route finalization).
•	Driver facing view showing assigned route, ETA, and status updates (hands free, low distraction design).
•	Role based access control separating dispatcher, driver, and manager permissions.
•	Integration with an external mapping and geocoding API (for example OpenStreetMap) for distance and time matrices.
•	Basic operational dashboard for managers (route variance, on time performance, distance and fuel estimates).
Out of Scope
•	Building a custom mapping and geocoding engine. The team relies on an external API (see Decision D001) rather than developing routing algorithms from raw map data.
•	Payment processing or billing integration.
•	Native mobile apps. Driver and dispatcher interfaces will be web based only this semester.
•	Predictive analytics and machine learning based demand forecasting.
•	Full production grade GPS live tracking. The system uses throttled or simulated pings, not continuous live tracking infrastructure.
•	Multi company or multi tenant support. The system is scoped to a single fleet operator.
Assumptions
•	Test data (addresses, orders, vehicle profiles) can be adequately simulated using Gaborone based mock data without needing live production data.
•	The external mapping API (OpenStreetMap or Mapbox) will remain available and free or low cost for the semester's usage volume.
•	Drivers and dispatchers have basic smartphone or web access sufficient to use the interfaces.
•	Regulatory driving hour rules can be approximated with a simplified rule set rather than full legal jurisdictional compliance.
Constraints
•	Semester timeline restricts scope to a vertical slice. Core focus is orchestration, state management, and business logic, not custom optimization algorithms.
•	Team's technical stack is fixed to a Java and SQL backend with a web frontend.
•	No access to real operator or customer data. The team relies on mock and synthetic datasets, and PII must still be protected via RBAC even in test data.
•	Network and API cost constraints require GPS ping frequency to be throttled rather than continuous.
•	No specialized hardware (for example dedicated GPS trackers). The system is reliant on standard device sensors.



