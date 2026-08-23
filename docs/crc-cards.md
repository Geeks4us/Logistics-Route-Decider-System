## Class-Responsibility-Collaborator (CRC) Cards

### 1. Class Name: `DeliveryRoute`
* **Superclass / Subclass:** None / None
* **Description:** Represents an optimized sequence of delivery stops assigned to a vehicle and driver for execution.

| Responsibilities (What it Knows / Does) | Collaborators |
| :--- | :--- |
| **Doing:** Calculates total estimated distance, travel time, and duration. | `DeliveryStop` |
| **Doing:** Sequences delivery stops into an optimal order. | `Vehicle` |
| **Doing:** Transitions route status from `Draft` to `Optimized`. | `Driver` |
| **Knowing:** Knows its assigned stops, total distance, total time, and current state. | `ExternalMappingService` |
| **Knowing:** Enforces invariant BR-01 (Total weight/volume $\le$ Vehicle limits). | |
| **Knowing:** Enforces invariant BR-02 (Total duration $\le$ Driver shift limit). | |

---

### 2. Class Name: `OrderBatch`
* **Superclass / Subclass:** None / None
* **Description:** Manages a grouping of pending delivery orders targeted for route optimization.

| Responsibilities (What it Knows / Does) | Collaborators |
| :--- | :--- |
| **Doing:** Validates that all contained orders are confirmed before optimization. | `DeliveryOrder` |
| **Doing:** Aggregates total payload weight and total package volume across orders. | `DeliveryRoute` |
| **Doing:** Transitions batch state from `Draft` to `Optimized` upon successful routing. | |
| **Knowing:** Knows its batch lifecycle state (`Draft`, `Optimized`). | |
| **Knowing:** Enforces invariant BR-04 (Batch state lock and atomic updates). | |
| **Knowing:** Enforces invariant BR-06 (Rollback batch to `Draft` on optimization failure). | |

---

### 3. Class Name: `DeliveryStop`
* **Superclass / Subclass:** None / None
* **Description:** Represents an individual location on a route where an order must be delivered within a specific time window.

| Responsibilities (What it Knows / Does) | Collaborators |
| :--- | :--- |
| **Doing:** Verifies if its estimated arrival time ($\text{ETA}$) falls within the order window. | `DeliveryOrder` |
| **Doing:** Flags or excludes itself if designated unreachable by mapping services. | `ExternalMappingService` |
| **Knowing:** Knows its sequence index, delivery address, estimated arrival time, and reachability. | |
| **Knowing:** Enforces invariant BR-03 ($\text{WindowStart} \le \text{ETA} \le \text{WindowEnd}$). | |
| **Knowing:** Enforces invariant BR-05 (Reachable stop sequence guarantee). | |

---

### 4. Class Name: `Vehicle`
* **Superclass / Subclass:** None / None
* **Description:** Represents a transport asset used to carry order payloads along a delivery route.

| Responsibilities (What it Knows / Does) | Collaborators |
| :--- | :--- |
| **Doing:** Evaluates whether a proposed weight and volume payload exceeds its capacity. | `Driver` |
| **Doing:** Confirms active status and operational availability for route assignment. | `DeliveryRoute` |
| **Knowing:** Knows its maximum weight capacity, maximum volume capacity, and operational status. | |
| **Knowing:** Enforces invariant BR-07 (Only active vehicles with assigned drivers can be routed). | |
