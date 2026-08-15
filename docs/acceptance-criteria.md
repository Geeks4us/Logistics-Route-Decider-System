# Acceptance Criteria: Delivery Route Assignment

This document details the Given-When-Then acceptance criteria for functional requirements `FR-01` through `FR-03`, linked to **UC-01: Assign Delivery Route**.

---

## UC-01: Assign Delivery Route

### AC-01: Successful Route Assignment (Happy Path)
* **Given** a logged-in Dispatcher viewing a pending delivery route with status `Draft`
* **And** at least one driver with status `Available` and matching vehicle capacity is registered in the system
* **When** the Dispatcher selects an available driver and confirms the assignment
* **Then** the route status updates from `Draft` to `Dispatched`
* **And** the assigned driver's system status updates to `On Duty / Assigned`
* **And** a real-time dispatch alert containing route details is delivered to the driver's mobile device.

---

### AC-02: Validation Failure – Vehicle Capacity Exceeded (Alternative Flow)
* **Given** a Dispatcher attempting to assign a route with a total payload weight of **1,200 kg**
* **When** the Dispatcher selects a driver operating a vehicle with a maximum capacity of **1,000 kg** and submits the request
* **Then** the system rejects the assignment and halts the status transition
* **And** displays an inline validation error: *"Vehicle capacity exceeded by 200 kg. Select a suitable vehicle."*
* **And** the route status remains strictly as `Draft`.

---

### AC-03: Concurrent Assignment Conflict (Alternative Flow)
* **Given** two Dispatchers (`Dispatcher A` and `Dispatcher B`) reviewing the same unassigned route simultaneously
* **And** `Driver John` is currently listed as `Available`
* **When** `Dispatcher A` assigns `Driver John` to the route and saves successfully
* **And** `Dispatcher B` attempts to assign `Driver John` to a separate route seconds later
* **Then** the system rejects `Dispatcher B`'s submission via optimistic concurrency locking
* **And** displays a conflict message: *"Driver John is no longer available (Assigned to Route #104)"*
* **And** automatically refreshes the available drivers drop-down menu for `Dispatcher B`.

---

## Design Rationale

* **Choice Made:** Implemented synchronous capacity validation and optimistic entity locking on the route assignment process before changing state to `Dispatched`.
* **Realistic Alternative Considered:** Asynchronous background validation where the assignment is saved immediately and flagged later if weight limits are violated.
* **Consequence Accepted:** Small increase in request latency (~100–150ms) during validation checks, accepted in favor of preventing overweight delivery vehicles from leaving the depot.

---

## Traceability Summary

| Acceptance Criteria | Target Requirement | Target Actor | Verification Method |
| :--- | :--- | :--- | :--- |
| **`AC-01`** | `FR-01` (Assign Route) | Dispatcher, Driver | Automated End-to-End Test |
| **`AC-02`** | `FR-02` (Capacity Validation) | Dispatcher | Automated Unit/Integration Test |
| **`AC-03`** | `FR-03` (Concurrency Control) | Dispatcher | Integration Test / Load Simulation |
