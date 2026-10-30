# Lab 8 Cross-Artifact Consistency Matrix

This matrix links the core optimization workflow to the API contract, use case, domain model, deployment and integrity controls. It distinguishes optimization from the later route-dispatch operation.

| Concern | Use case / operation | API / sequence responsibility | Domain entity and rule | Persistence / deployment rule |
|---|---|---|---|---|
| Batch eligibility | UC-25, precondition and step 4 | POST /api/routes/optimize validates batchId | OrderBatch; BR-04: only Draft batch with confirmed eligible orders | Read authoritative state from primary |
| Payload capacity | UC-25, step 5 / A1 | Return 422 CAPACITY_EXCEEDED | Vehicle, DeliveryOrder; BR-01 | Recheck selected vehicle version before commit |
| Driver shift | UC-25, step 5 / A4 | Return 422 SHIFT_LIMIT_EXCEEDED | Driver, DeliveryRoute; BR-02 | Driver state/version checked on primary |
| ETA and reachability | UC-25, steps 6–8 / A2 | Return 422 DELIVERY_WINDOW_UNMET or UNREACHABLE_STOP | DeliveryOrder, DeliveryStop; BR-03 and BR-05 | No invalid route is committed |
| Provider failure | UC-25 / A3 | Return 503 OPTIMIZATION_UNAVAILABLE | Optimization capability via RoutingEnginePort | Bounded timeout/retry; batch remains Draft |
| Route proposal persistence | UC-25, steps 9–11 | 201 Created, status Optimized | DeliveryRoute and DeliveryStop; BR-06 | Atomic primary transaction; idempotency result stored with route |
| Dispatcher assignment | Separate approval/assignment operation after UC-25 | Not performed by POST /api/routes/optimize | Driver, Vehicle, DeliveryRoute | Recheck concurrency and update all assignment states in one primary transaction |
| Client ingress | All client requests | Dispatcher browser / driver mobile -> API Gateway | Authorization and transport validation | Gateway forwards to single application deployment |
| Read scaling | Dashboard/history views only when stale data is acceptable | Application selects read-only replica path | Read models / historical views | Primary is authoritative; replica may lag and is never written |

## Important lifecycle distinction

Optimization creates a proposed route and moves the batch from Draft to Optimized. It does **not** dispatch the route or change the driver's status to On Duty. Dispatcher approval/assignment is a separate operation that moves the route to Dispatched and updates driver/vehicle assignment atomically.

## Architecture boundary

The API Gateway is a separate ingress component. Dispatch, Optimization and Tracking remain capability modules inside one deployable application. The primary database and read replica form one logical relational database topology; the replica is not an independent source of truth.
