# Lab 10: User Profile and Task Scenario

## 1. Primary User Profile

### User Role: Fleet Dispatcher

**Description:**  
The Fleet Dispatcher is responsible for coordinating deliveries, assigning drivers, reviewing optimized routes, and monitoring delivery progress through the Logistics-Route-Decider-System.

**Goals:**
- Generate an optimized route for selected pending deliveries.
- Assign the route to an available driver.
- Review waypoint order and estimated arrival times (ETAs).
- Manually review or adjust a route when it exceeds the driver's shift hours.
- Monitor route and delivery status.

**Needs:**
- A clear list of pending deliveries and available drivers.
- Accurate route sequences and ETAs.
- Clear warnings when a route cannot be assigned.
- Feedback when a route is successfully generated or an operation fails.
- The ability to review a proposed route before confirming assignment.

**Challenges:**
- Assigning deliveries to unavailable drivers.
- Selecting delivery nodes that have already been assigned.
- Handling route optimization failures.
- Preventing route assignments that violate driver shift limits.

## 2. Primary Task Scenario

**Scenario:** Generate and Assign an Optimized Delivery Route

**Primary actor:** Fleet Dispatcher

**Goal:** Create an optimized route for three pending delivery nodes and assign it to an available driver.

### Preconditions
1. The Fleet Dispatcher is authenticated.
2. The selected delivery nodes exist and have a Pending status.
3. At least one driver is logged in and available.
4. The system can access the required delivery coordinates.

### Main Success Flow
1. The dispatcher opens the Dispatch Dashboard.
2. The dispatcher views the list of pending delivery nodes.
3. The dispatcher selects three delivery nodes.
4. The dispatcher selects an available driver.
5. The dispatcher requests route optimization.
6. The Optimization Engine calculates the waypoint sequence and ETAs.
7. The system displays the proposed route for review.
8. The dispatcher reviews the route and confirms the assignment.
9. The system assigns the route to the selected driver and marks the route Active.

### Alternative Flow
If the proposed route exceeds the driver's shift hours, the system flags the route for manual review. The dispatcher is prompted to review the route and remove or adjust lower-priority delivery nodes before confirming the assignment.

### Success Criteria
- The dispatcher can select the required delivery nodes and an available driver.
- The system returns a proposed waypoint sequence and ETAs.
- The dispatcher can review the proposed route before confirmation.
- The route is assigned only after successful validation and confirmation.
- The route becomes Active with the correct driver and delivery nodes.
- Invalid assignments are rejected without creating inconsistent route data.

## 3. Secondary User Task Scenario

**Secondary actor:** Delivery Driver

**Scenario name:** Update Delivery Status

**Goal:** Update the status of a delivery node after attempting delivery.

### Main Success Flow
1. The driver opens their active route.
2. The driver selects the next delivery node.
3. The system displays the delivery details.
4. The driver selects Completed or Failed, as appropriate.
5. If the driver selects Failed, the system requests a failure reason.
6. The driver submits the update.
7. The system validates and saves the new delivery status.
8. The system displays confirmation of the saved update.

### Success Criteria
- The driver can find the next delivery node easily.
- The driver can select a valid delivery status.
- A failure reason is captured when required.
- The system confirms the update only after it has been saved successfully.

