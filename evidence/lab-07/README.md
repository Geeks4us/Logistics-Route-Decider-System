# Lab 7 — Architecture Evidence

This folder records the evidence used to review the component architecture and its quality-attribute rationale.

## Architecture artifacts

- [Component architecture (Markdown)](../../models/component-architecture.md) — component responsibilities, dependency direction, deployment view, and Mermaid diagram.
- [Component architecture (PlantUML)](../../models/component-architecture.puml) — editable source for the component diagram.
- [Architecture options](../../docs/architecture-options.md) — alternatives and selection rationale.
- [Quality-to-architecture mapping](../../docs/quality-to-architecture.md) — quality scenarios, mechanisms, and verification plan.
- [ADR-001](../../decisions/ADR-001-architecture.md) — accepted architecture decision.

## Evidence checklist

- [x] Architecture alternatives and selected option documented.
- [x] Component boundaries and cross-capability contracts documented.
- [x] Dependency direction and framework boundary documented.
- [x] Deployment and data consistency assumptions documented.
- [x] Quality scenarios mapped to architectural mechanisms and verification methods.
- [ ] Attach a rendered component diagram or export if the course submission requires a standalone image.
- [ ] Attach load/performance test reports after running the stated scenarios.
- [ ] Attach integration/failure-injection test reports after implementing the adapters.

## Scope and honesty note

The documents describe the intended Phase 1 architecture. The checked items indicate documentation is present; they do not claim that the system has passed the listed performance, security, reliability, or integration tests. No test report is included until a test has actually been executed and its results recorded.
