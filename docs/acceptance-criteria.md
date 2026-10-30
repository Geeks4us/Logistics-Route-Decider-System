# Acceptance Criteria: Delivery Route Review and Assignment

This document details the Given-When-Then acceptance criteria for route approval, capacity validation, and assignment. Use-case IDs follow `docs/traceability-matrix.md`: UC-02 is Optimize Delivery Route and UC-03 is Review & Approve Route.

---

## UC-03: Review, Approve & Assign Route

### AC-01: Successful Route Assignment (Happy Path)
* **Given** a logged-in Dispatcher viewing a pending delivery route with status `Draft`
* **And** at least one driver with status `Available` and matching vehicle capacity is registered in the system
* **When** the Dispatcher approves the proposed route, selects an available driver, and confirms the assignment
* **Then** the route status updates to `Assigned`
* **And** the assigned driver's system status updates to `On Duty / Assigned`
* **And** the system records the assignment for subsequent driver notification.

---

## UC-02: Optimize Delivery Route

### AC-02: Validation Failure – Vehicle Capacity Exceeded
* **Given** a Dispatcher attempting to optimize a route with a total payload weight of **1,200 kg**
* **When** the system evaluates a vehicle with a maximum capacity of **1,000 kg**
* **Then** the system rejects the optimization/assignment and halts the state transition
* **And** displays an inline validation error: *"Vehicle capacity exceeded by 200 kg. Select a suitable vehicle."*
* **And** the route remains in its prior valid state.

---

## UC-03: Review, Approve & Assign Route

### AC-03: Concurrent Assignment Conflict
* **Given** two Dispatchers (`Dispatcher A` and `Dispatcher B`) reviewing the same unassigned route simultaneously
* **And** `Driver John` is currently listed as `Available`
* **When** `Dispatcher A` assigns `Driver John` to the route and saves successfully
* **And** `Dispatcher B` attempts to assign `Driver John` to a separate route using stale availability/version data
* **Then** the system rejects `Dispatcher B`'s submission via optimistic concurrency control
* **And** displays a conflict message: *"Driver John is no longer available. Refresh the available drivers and retry."*
* **And** leaves the failed transaction without partial route/driver updates.

---

## Design Rationale

* **Choice Made:** Perform capacity validation and route-state checks synchronously in the Java application core before persisting state changes. Use relational-database optimistic locking/version checks for concurrent assignments.
* **Alternative Considered:** Asynchronous validation after accepting the assignment, and distributed locking with an external cache.
* **Consequence Accepted:** Synchronous validation adds a small amount of request latency, accepted to prevent invalid or conflicting assignments.

---

## Traceability Summary

| Acceptance Criteria | Target Requirement | Target Use Case | Verification Method |
|---|---|---|---|
| **AC-01** | **FR-04**, **FR-05** | **UC-03: Review, Approve & Assign Route** | Automated End-to-End Test |
| **AC-02** | **FR-03** | **UC-02: Optimize Delivery Route** | Automated Unit/Integration Test |
| **AC-03** | **FR-05** plus concurrency invariant | **UC-03: Review, Approve & Assign Route** | Integration Test / Concurrent Assignment Test |
