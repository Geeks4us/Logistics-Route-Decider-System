# Logistics Route Decider System

CSI473 team project. This repository implements the **Logistics Route Decider System**; the University Service Hub in the Laboratory 11 handout is only a lecturer example and is not this team's project.

## Current project architecture
- Java 21 / Spring Boot 3.x target (ADR-002).
- Capability-oriented modular monolith (ADR-001): Dispatch & Management, Optimization Engine, Tracking & Notification.
- API Gateway remains the edge component; the logical relational database has a primary and read replica.
- Routing and notification integrations are represented by outbound ports.

## Laboratory 11 status (30 October 2026)
The lab-11 branch is an evidence and reconciliation branch based on lab-09 commit a7ed4134cd5123931bec6cbb156f6c129b147a8f.

**Important:** the current repository snapshot is not yet a runnable vertical slice. The source contains application-service and port skeletons, but the routing adapter throws UnsupportedOperationException; no HTTP controller, application entry point, persistence adapter, database schema/migration, Maven/Gradle build descriptor, or automated test suite was present at the start of Lab 11. Therefore no successful application run or test result is claimed.

## Repository guide
- src/main/java — current Java capability skeletons.
- tests/ — Lab 11 test evidence and implementation gap notes; automated executable tests remain to be added.
- docs/reconciliation-matrix.md — requirement/model to code to test reconciliation.
- docs/technical-debt-and-risks.md — technical debt, residual risks and conditional release recommendation.
- docs/lab-11-demo-plan.md — five-minute demo plan and readiness gates.
- docs/lab-11-checklist.md — evidence and submission checklist.
- decisions/ADR-001-architecture.md, decisions/ADR-002-framework.md — architecture decisions.

## Build and test
No supported build command can currently be run from this repository snapshot because no pom.xml or build.gradle is present. Do not report mvn test, gradle test, or an application launch as successful until a build descriptor, adapters, schema, and tests have been committed and run in a configured Java 21 environment.

## Synthetic data and security
Use synthetic order/customer/driver data only. Do not commit real customer addresses, contact details, access tokens, Mapbox keys, or production credentials.
