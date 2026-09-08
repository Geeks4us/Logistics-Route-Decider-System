# Requirements Traceability Matrix

## Overview
This matrix establishes end-to-end traceability between Functional Requirements (FRs), Use Cases, Analysis Domain Entities, Business Rules/Invariants, and planned Verification Methods for the Logistics Route Decider System.

---

| Requirement ID | Requirement Description | Target Actor | Target Use Case | Domain Entity / Service | Business Rule / Invariant | Verification Method |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **FR01** | Create batch of delivery orders with address, time window, and priority level. | Dispatcher | UC-01: Create Batch / Delivery Orders | `OrderBatch`, `DeliveryOrder` | N/A | Automated Unit Test / UI Integration Test |
| **FR02** | Generate optimized delivery route based on priority, time windows, and vehicle capacity. | Dispatcher, System | UC-02: Optimize Delivery Route | `DeliveryRoute`, `DeliveryStop`, `RouteOptimizationService` | `BR-03` (Delivery Window Fulfillment) | Algorithm Simulation / Integration Test |
| **FR03** | Reject route assignments exceeding vehicle maximum weight or volume capacity. | Dispatcher, System | UC-02: Optimize Delivery Route | `Vehicle`, `OrderBatch` | `BR-01` (Vehicle Mass & Volume Capacity Limit) | Automated Unit Test (`validatePayload()`) |
| **FR04** | Dispatcher approval or rejection of proposed route prior to driver assignment. | Dispatcher | UC-03: Review & Approve Route | `DeliveryRoute` | `BR-04` (Batch Lifecycle Transition Integrity) | UI Workflow / End-to-End Integration Test |
| **FR05** | Enforce route status progression through defined lifecycle states and reject invalid transitions. | Dispatcher, Driver | UC-04: Manage Route Status | `DeliveryRoute` | `BR-04` (Batch Lifecycle), `BR-06` (Atomic Rollback) | State Machine Unit Test |
| **FR06** | Automatically resequence route when a stop becomes unreachable or delivery is delayed. | System, Driver | UC-05: Resequence Route | `DeliveryStop`, `DeliveryRoute` | `BR-05` (Reachable Stop Sequence Guarantee) | Scenario Simulation / Integration Test |
| **FR07** | Notify assigned driver when dispatcher modifies active route. | System, Driver | UC-06: Driver Notification | `Driver`, `NotificationService` | N/A | End-to-End Dispatch Alert Test |
| **FR08** | Restrict access to delivery addresses and customer contacts via Role-Based Access Control (RBAC). | All Roles | All Use Cases | `UserSession`, `RoleController` | Security Invariant (Data Protection) | Security Penetration / RBAC Audit Test |
| **FR09** | Retrieve distance and travel time data from external mapping API for route calculations. | System, API | UC-02: Optimize Delivery Route | `ExternalMappingService` | Decision `D-001` | Mock API Integration Test |
| **FR10** | Display on-time delivery completion rate over selected date range for manager query. | Logistics Manager | UC-07: View Operational Dashboard | `OperationalDashboard` | Quality Scenario #4 | Dashboard Data Accuracy Test |

---

## Traceability Rationale & Coverage
* **100% Requirement Coverage:** Every functional requirement (`FR01` through `FR10`) maps directly to at least one use case, domain entity, and verification strategy.
* **Invariant Linkage:** Critical constraints (`BR-01` through `BR-06`) are strictly tied to the domain entities responsible for enforcing them (e.g., `Vehicle.validatePayload()`).
* **Architectural Alignment:** External integration requirements (`FR09`) map directly to Architectural Decision `D-001`.
