# ADR-002: Backend Framework Decision

- **Status:** Accepted
- **Date:** 2026-10-30
- **Decision owners:** Group 01

## Context

The Logistics Route Decider System needs a Java backend for dispatcher-facing HTTP operations, route-management use cases, validation, persistence integration, and background route-optimization work. The architecture remains the **Capability-Oriented Modular Monolith** selected in ADR-001: Dispatch & Management, Optimization Engine, and Tracking & Notification are capability modules in one deployable application.

The framework must support clear module boundaries and dependency inversion. Framework and infrastructure code must not become dependencies of the domain model or application services.

## Decision

Use **Java 21 with Spring Boot 3.x** as the backend application framework.

- Use Spring Web for REST controllers and HTTP request/response handling.
- Use Spring dependency injection in the composition root to wire application services and outbound adapters.
- Use Jakarta Bean Validation at request boundaries.
- Use Spring Data/JDBC only in infrastructure adapters; domain and application code depend on repository ports.
- Use managed asynchronous execution for long-running optimization jobs where needed.
- Keep capability modules in one deployable application. Cross-module calls go through small, explicit application interfaces; publish local events for secondary effects.

Spring is an outer-layer implementation detail. Domain models, port interfaces, and application services remain plain Java and should be testable without starting the Spring container.

## Dependency direction

```text
HTTP / Spring adapters
        |
        v
Capability application services
        |
        v
Domain model + capability-owned port interfaces
        ^
        |
Infrastructure adapters implement outbound ports

Spring composition root wires application services and adapter implementations.
Core modules do not import adapter classes or provider SDKs.
```

## Why Spring Boot

- Provides an established Java ecosystem for REST APIs, dependency injection, validation, persistence integration, and testing.
- Supports a single deployable modular monolith without requiring a distributed platform.
- Allows infrastructure implementations to be replaced behind interfaces.
- Keeps the team focused on delivery and domain behaviour rather than building framework plumbing.

## Consequences

- Spring dependencies belong in HTTP/infrastructure adapters and the composition root, not in domain entities, port interfaces, or application services.
- Enforce package/module boundaries in code review and tests.
- Keep provider-specific SDK types out of public capability interfaces.
- Use unit tests for domain/application logic and Spring integration tests for wiring and HTTP behaviour.
- Reassess the framework only if measured operational requirements justify a change.

## Explicit rejection

**Reject Quarkus as the backend framework for this release.** Quarkus is a capable Java framework, but its native-image and container-startup advantages are not required by the current Phase 1 constraints. The project has no demonstrated need for those optimizations, while adopting a second framework ecosystem would add evaluation, configuration, and team learning costs without improving the required route-planning workflows. Spring Boot is therefore the lower-risk choice for this release.

This does not reject Quarkus permanently; the decision can be revisited if deployment measurements establish a concrete need for its strengths.
