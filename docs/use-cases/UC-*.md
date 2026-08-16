FULL DRESSED CORE USE CASE WITH PRECONDITIONS, POSTCONDITIONS AND ALTERNATIVE FLOWS

USE CASE NAME: Optimized delivery process.
USE CASE ID: UC 25

Primary actor: Route dispatcher
Secondary actor: External Mapping API
Goal: Generate an effective and functional delivery route that satisfies vehicle capacity, delivery time window and operational constraints.
Trigger: Dispatcher selects an order batch and asks the system to generate an optimized route.

Preconditions
•	An order batch exists in "Draft" state with at least one order waiting for an order confirmation.
•	Each order has a delivery address, delivery time window and payload weight/volume.
•	At least one vehicle is available and marked active, with known payload capacity and driver shift limits on file.
•	The Mapping/Geocoding API is reachable.

Success scenario
1.	Dispatcher selects a batch of delivery orders. 
2.	System validates the order information. 
3.	System checks for constraints such as  the selected vehicle's weight and volume capacity and the driver’s shift limit. 
4.	System sends the required location information to the external mapping/geocoding API. 
5.	The mapping service returns distance and travel-time information. 
6.	System evaluates delivery time windows and order priorities. 
7.	System generates an optimized sequence of delivery stops. 
8.	System checks that the generated route satisfies all applicable constraints. 
9.	System calculates the estimated route distance and travel time. 
10.	System saves the optimized route. 
11.	System changes the route status from Draft to Optimized. 
12.	System displays the optimized route to the dispatcher for review.
 
Alternative Flows
Alternative 1 – Vehicle Over Capacity
1.	During validation, the system determines that the total package weight or volume exceeds the vehicle's capacity. 
2.	The system prevents route finalization. 
3.	The system informs the dispatcher that the vehicle is over capacity. 
4.	Dispatcher changes the vehicle or adjusts the assigned orders. 
5.	The dispatcher requests optimization again.
   
Alternative 2 – Delivery Stop Cannot Be Reached
1.	The system determines that a delivery stop cannot be reached using the available route information. 
2.	The system removes or flags the unreachable stop. 
3.	The system re-sequences the remaining deliveries. 
4.	The system presents the revised route to the dispatcher.
   
Alternative 3 – Mapping API Unavailable
1.	The system cannot obtain the required distance or travel-time information. 
2.	The system does not finalize the optimized route. 
3.	The system informs the dispatcher that route optimization cannot currently be completed. 
4.	The dispatcher can retry when the mapping service becomes available again.
   
Alternative 4 – Driver Working-Hour Constraint Violated
1.	The system determines that the proposed route exceeds the driver's permitted working hours. 
2.	The system rejects the route sequence. 
3.	The system adjusts the route or requires another driver/route configuration. 
4.	The system generates a new route that doesn’t exceed the driver’s shift limit.
   
Postconditions
1.	Success: The batch's state moves from Draft to Optimized.
•	Constraints are checked and validated.
•	Delivery stops are arranged according to the optimization rules.
•	The dispatcher can review the route before finalizing and assigning it to the driver.
2.	Failure: The batch remains in Draft with no partial or invalid route created.
•	The dispatcher is notified about the error preventing route optimization.











