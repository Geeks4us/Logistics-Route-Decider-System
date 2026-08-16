ACTORS AND THEIR GOALS
1.	Route dispatcher
•	The main goal of a dispatcher is to create delivery orders.
•	Generate optimized routes, review and approve routes and assign those routes to drivers.

2.	Delivery driver
•	View assigned route, follow delivery instructions while receiving and updating delivery status.

3.	Logistics manager
•	The primary goal of a logistics manager is fleet management,
•	Overseeing that deliveries are on time, hence improving supplier-customer relationships.
•	Calculating the distance and fuel estimates while also minimizing the costs.

4.	Customers
•	Customers should receive their orders at the correct time and place
•	Receive and send accurate delivery status updates.

5.	API
•	Provide the route and geographical information including distance matrix.
•	The time taken to travel for route optimization.

                         LOGISTICS ROUTE DESIGN SYSTEM
        ┌──────────────────────────────────────────────────────────┐
        │                                                          │
        │  (Create Delivery Order)                                 │
        │  (Optimize Delivery Route)                               │
        │  (Review Optimized Route)                                │------> Route dispatcher
        │  (Approve Route)                                         │
        │  (Assign Route to Driver)                                │                                     │
        │  (Manage Delayed/Exception Delivery)                     │
        │                                                          │
        │  (View Assigned Route)                                   │
        │  (View ETA)                                              │------> Delivery driver
        │  (Update Delivery Status)                                │
        │  (Complete Delivery)                                     │
        │                                                          │
        │  (View Operational Dashboard)                            │
        │  (Monitor Route and Delivery Performance)                │------> Logistics manager
        │                                                          │
        │  (View and update Delivery Status/ETA)                   │------> Customer
        │                                                          │
        │  (Get Distance/Travel-Time Data)                         │------> API
        │                                                          │
        └──────────────────────────────────────────────────────────┘
                                      

                         External Mapping API
                                  │
                                  ↓
                    (Get Distance/Travel-Time Data)


