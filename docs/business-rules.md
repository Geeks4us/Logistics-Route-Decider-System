## Business Rules and System Invariants

### 1. Vehicle Mass and Volume Capacity Limit (BR-01)
* **Invariant Condition:** The combined payload weight and total package volume of all orders assigned to a route must be less than or equal to the assigned vehicle’s maximum limits.
  $$\sum \text{Order.weight} \le \text{Vehicle.maxWeight} \quad \text{AND} \quad \sum \text{Order.volume} \le \text{Vehicle.maxVolume}$$
* **Enforcement Point:** Evaluated prior to route finalization.
* **Rationale:** Prevents overloading vehicles, avoiding mechanical damage, legal violations, and safety hazards (*UC 25 Alternative Flow 1*).

---

### 2. Driver Working-Hour Boundary (BR-02)
* **Invariant Condition:** The estimated duration of a route—including driving time, stop service times, and required driver breaks—must not exceed the maximum shift duration permitted for the driver.
  $$\text{Route.totalEstimatedTime} \le \text{Driver.maxShiftLimit}$$
* **Enforcement Point:** Evaluated during sequence generation and before saving the route.
* **Rationale:** Maintains compliance with labor laws and driver safety standards (*UC 25 Alternative Flow 4*).

---

### 3. Delivery Window Fulfillment (BR-03)
* **Invariant Condition:** For every delivery stop on an optimized route, the estimated time of arrival ($\text{ETA}$) must fall within the customer's specified time window.
  $$\text{Order.windowStart} \le \text{Stop.ETA} \le \text{Order.windowEnd}$$
* **Enforcement Point:** Evaluated during sequence optimization and re-sequencing.
* **Rationale:** Guarantees customer satisfaction and prevents missed deliveries.

---

### 4. Batch Lifecycle Transition Integrity (BR-04)
* **Invariant Condition:** Route optimization can only be triggered on an `OrderBatch` in the `Draft` state, and an `OrderBatch` can only transition to `Optimized` if all contained orders are validated and confirmed.
  $$\text{Batch.status} = \text{Draft} \implies \forall o \in \text{Batch.orders}, \text{o.status} = \text{Confirmed}$$
* **Enforcement Point:** Validated at the start and completion of the optimization process.
* **Rationale:** Prevents routing unconfirmed, canceled, or already-scheduled orders.

---

### 5. Reachable Stop Sequence Guarantee (BR-05)
* **Invariant Condition:** An optimized route must never contain a delivery stop marked as unreachable by the geocoding or mapping service.
  $$\forall s \in \text{Route.stops}, \text{s.isReachable} = \text{True}$$
* **Enforcement Point:** Checked immediately after receiving distance/time data from the external API.
* **Rationale:** Ensures drivers are not assigned impossible or dangerous routes (*UC 25 Alternative Flow 2*).

---

### 6. Atomic Rollback on Failure (BR-06)
* **Invariant Condition:** If route optimization fails due to system error, API downtime, or constraint violation, no new route record may be persisted, and the `OrderBatch` must remain unchanged in the `Draft` state.
  $$\text{OptimizationFailed} \implies \text{Batch.status} = \text{Draft} \land \text{Route.created} = \text{False}$$
* **Enforcement Point:** Transaction management / exception handling level.
* **Rationale:** Preserves data integrity by preventing partial, orphaned, or corrupted route records in the system (*UC 25 Postconditions - Failure*).
