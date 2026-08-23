# Domain Glossary & Ubiquitous Language


## 1. Core Domain Entities & Concepts

### Order Batch `[NEW - Lab 3]`
* **Definition:** A grouped collection of pending delivery orders in `Draft` state selected together by a dispatcher to be routed in a single optimization cycle.
* **Key Attributes:** `batchId`, `status`, `creationDate`, `totalOrderCount`.
* **Traceability:** UC 25 (Preconditions, Step 1), BR-04, BR-06.

### Delivery Order `[Refined - Lab 3]`
* **Definition:** An individual request from a customer requiring transport of goods to a specific geographic destination within an explicit delivery time window.
* **Key Attributes:** `orderId`, `deliveryAddress`, `payloadWeight`, `payloadVolume`, `timeWindowStart`, `timeWindowEnd`, `status`.
* **Traceability:** UC 25 (Preconditions), BR-03.

### Delivery Route `[NEW - Lab 3]`
* **Definition:** An ordered, time-sequenced path of delivery stops assigned to a specific vehicle and driver to fulfill an Order Batch.
* **Key Attributes:** `routeId`, `totalDistance`, `totalEstimatedTime`, `status`, `createdTimestamp`.
* **Traceability:** UC 25 (Steps 7–11), BR-01, BR-02.

### Delivery Stop `[NEW - Lab 3]`
* **Definition:** A specific location checkpoint along a Delivery Route where a driver stops to fulfill one or more Delivery Orders.
* **Key Attributes:** `stopSequenceIndex`, `estimatedArrivalTime (ETA)`, `isReachable`, `serviceDuration`.
* **Traceability:** UC 25 (Steps 7–8, Alt Flow 2), BR-03, BR-05.

### Vehicle `[Carried Over / Refined - Lab 3]`
* **Definition:** A physical transport asset utilized to carry order payloads along a Delivery Route, constrained by physical capacity limits.
* **Key Attributes:** `vehicleId`, `maxWeightCapacity`, `maxVolumeCapacity`, `status` (`Active`, `Maintenance`, `Inactive`).
* **Traceability:** UC 25 (Preconditions, Step 3), BR-01, BR-07.

### Driver `[Carried Over / Refined - Lab 3]`
* **Definition:** The operational personnel assigned to navigate a Vehicle along a Delivery Route, subject to labor hour regulations.
* **Key Attributes:** `driverId`, `shiftLimitHours`, `currentShiftHoursWorked`, `status`.
* **Traceability:** UC 25 (Preconditions, Step 3), BR-02, BR-07.

### Payload `[NEW - Lab 3]`
* **Definition:** The combined physical mass and bulk volume of goods contained within one or more Delivery Orders.
* **Units:** Weight in kilograms ($\text{kg}$), Volume in cubic meters ($\text{m}^3$).
* **Traceability:** UC 25 (Preconditions, Step 3), BR-01.

---

## 2. Business Roles & External System Abstractions

### Route Dispatcher `[Primary Actor]`
* **Definition:** The domain user responsible for forming order batches, triggering route optimization, reviewing generated routes, and finalizing vehicle assignments.
* **Traceability:** UC 25 (Primary Actor).

### External Mapping Service `[Domain Service Role - Aligned with D001]`
* **Definition:** A domain service boundary representing external geospatial mapping providers (e.g., OpenStreetMap / Mapbox). Responsible for providing distance matrix calculations, geocoding, and base travel time data.
* **Architecture Decision Link:** Aligned directly with **Decision Record D001** (External Routing Engine Delegation).
* **Traceability:** UC 25 (Secondary Actor, Steps 4–5), D001, BR-05.

---

## 3. Lifecycle States & Status Transitions

| State / Status | Applicable Entity | Definition | Traceability |
| :--- | :--- | :--- | :--- |
| **Draft** | `OrderBatch`, `DeliveryRoute` | Initial state of a batch or route prior to optimization or final confirmation. | UC 25, BR-04, BR-06 |
| **Optimized** | `OrderBatch`, `DeliveryRoute` | State achieved when a route sequence successfully satisfies all capacity, time window, and driver shift constraints. | UC 25, BR-04 |
| **Confirmed** | `DeliveryOrder` | Status indicating an order has passed validation and is eligible to be included in an Order Batch. | UC 25 |
| **Active** | `Vehicle`, `Driver` | Status indicating a transport asset or driver is operational, available, and permitted to be assigned. | BR-07 |
| **Unreachable** | `DeliveryStop` | Flag indicating a delivery location cannot be routed due to spatial data gaps or road closures. | UC 25 Alt Flow 2, BR-05 |

---

## 4. Key Performance Metrics & Constraints

* **Delivery Time Window (ETA):** The strict customer-specified time interval ($\text{WindowStart} \le \text{ETA} \le \text{WindowEnd}$) within which a stop must be served (BR-03).
* **Driver Shift Limit:** The maximum permitted working duration ($\text{Hours}$) a driver can operate within a single shift under labor safety regulations (BR-02).
* **Payload Capacity:** The maximum allowable mass ($\text{kg}$) and volume ($\text{m}^3$) a vehicle can safely transport (BR-01).
* **Distance Matrix:** A spatial data structure calculated by the `External Mapping Service` (D001) containing pairwise distances and travel durations between all delivery stops.
