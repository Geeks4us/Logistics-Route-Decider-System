# Architecture Options, Constraints, and Quality Scenarios Analysis

This document records the architectural evaluation, constraints, selected capability structure, and trade-offs mapped against our project's core requirements for the Logistics-Route-Decider-System[span_1](start_span)[span_1](end_span)[span_2](start_span)[span_2](end_span).

## 1. Project Constraints & Operational Boundaries

To ensure our architecture remains practical for an early-stage release, the system design is governed by the following constraints:
* **Phase 1 Deployment Scope Constraint:** The system must be deployable as a unified, pragmatic application artifact without introducing premature cloud orchestration overhead (e.g., Kubernetes) or distributed deployment complexity[span_3](start_span)[span_3](end_span).
* **Operational Resource Constraint:** The architecture must operate efficiently on standard development and hosting environments without requiring dedicated cluster management or specialized hardware infrastructure.
* **Integration Boundary Constraint:** Third-party integrations (such as messaging/SMS vendors) must be strictly isolated via port interfaces to prevent external vendor failures from crashing core system logic[span_4](start_span)[span_4](end_span).

---

## 2. Architectural Options Overview

* **Option 1: Capability-Oriented Modular Monolith (Selected)**
  The application is structured as a single deployment unit, but the codebase is strictly partitioned by domain capabilities (such as the Dispatch & Management, Optimization Engine, and Tracking & Notification Subsystems) rather than horizontal technical layers. Communication across domain boundaries is handled via in-memory procedure calls and local events[span_5](start_span)[span_5](end_span).
* **Option 2: Traditional Layered Monolith**
  The system is organized strictly by horizontal technical layers: a Presentation Layer (UI), a Business Logic Layer, and a Data Access Layer, where each layer depends entirely on the layer immediately beneath it.
* **Option 3: Event-Driven Microservices Architecture**
  The system is split into completely independent, separately deployable services (such as a Dispatch Service, Optimization Engine Service, and Notification Service) that communicate asynchronously over a network message broker (e.g., RabbitMQ or Kafka) with decentralized data stores.

---

## 3. Quality Scenarios Mapped Across Architecture Alternatives

| Quality Scenario & Definition | Selected Architecture (Option 1) | Alternative 2: Traditional Layered Monolith | Alternative 3: Event-Driven Microservices |
| :--- | :--- | :--- | :--- |
| **1. Scalability Scenario**<br>*(Support at least 50 concurrent web users and 1,000 delivery orders while keeping route optimization under 15 minutes)* | **How it addresses:** A single deployment instance handles 50 concurrent users comfortably, while the Optimization Engine uses asynchronous worker threads to process large datasets without freezing the dispatcher web UI. | **How it addresses:** Struggles under high concurrency because business logic and data access are tightly coupled. Heavy dataset processing creates database connection bottlenecks, risking the 15-minute optimization target. | **How it addresses:** Excellent at scaling specific components (like the optimization service) independently via cloud containers, but adds unnecessary complexity for supporting just 50 concurrent users[span_6](start_span)[span_6](end_span). |
| **2. Planning Efficiency Scenario**<br>*(Automatically generate an optimized route within 15 minutes for 95% of batches up to 100 orders, reducing manual planning from ~2 hours)* | **How it addresses:** In-memory data passing between the Dispatch subsystem and the Optimization Engine ensures rapid batch processing well within the 15-minute window without network serialization overhead. | **How it addresses:** Batch processing triggers cascading calls through horizontal tiers (UI -> Logic -> Data), increasing execution time and making it difficult to guarantee the 95% success rate under load. | **How it addresses:** Batch payloads must be serialized, sent across a message broker, and processed externally. While isolated, network transport overhead can occasionally breach strict timing SLAs. |
| **3. Data Integrity Scenario**<br>*(100% preservation of order details, vehicle assignment, route state, and delivery status without unintended duplication or loss)* | **How it addresses:** Relies on robust local relational database transactions within clear domain boundaries. Ensures atomic updates to route states and driver assignments with zero risk of distributed state sync errors. | **How it addresses:** Centralized database access maintains strong consistency initially, but as the codebase grows and layers blur, accidental cross-layer data mutations increase the risk of race conditions and orphaned records. | **How it addresses:** Because services have decentralized data stores, maintaining data integrity across a completed route transaction requires complex distributed transactions (Sagas/two-phase commits), introducing potential points of failure and data drift. |
