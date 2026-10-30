# Laboratory 11 Evidence Index

Project: Logistics Route Decider System  
Branch: lab-11  
Baseline commit: a7ed4134cd5123931bec6cbb156f6c129b147a8f (lab-09)  
Review date: 30 October 2026

## Evidence in this branch
- docs/reconciliation-matrix.md — requirement/model/code/test reconciliation, including gaps.
- docs/technical-debt-and-risks.md — risk register and conditional NO-GO recommendation.
- docs/lab-11-demo-plan.md — UC-25 demonstration script and readiness gates.
- docs/lab-11-checklist.md — exit and submission checklist.
- tests/README.md — required test coverage and explicit NOT RUN record.
- README.md — project identity, repository map and setup/build status.

## Verification performed
Inspected baseline README, Java source skeletons, routing adapter, ports, UC-25, business rules, traceability matrix and ADR-002. Confirmed that repository lacks build descriptor, application entry point, HTTP controller, concrete persistence implementation, schema/migration and executable test suite. Confirmed MapboxAdapter.calculate is a TODO that throws UnsupportedOperationException.

## Verification not performed
No compilation, unit/integration test run, live route-provider call, database migration, end-to-end demonstration, production deployment or Moodle submission is claimed.

## Remaining evidence
Team must implement and run the vertical slice, add real test output, complete Phase 2 PDF and contribution record, and create required tags after reviewing stable commits.
