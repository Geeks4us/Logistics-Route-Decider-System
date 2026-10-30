# Laboratory 11 — Technical Debt, Residual Risks and Release Review

**Review date:** 30 October 2026  
**Project:** Logistics Route Decider System  
**Release recommendation:** **NO-GO for a runnable Phase 2 demonstration or production release at this review point.**

## Technical-debt and risk register

| ID | Debt / residual risk | Likelihood / impact | Evidence | Required mitigation | Gate |
|---|---|---|---|---|---|
| TD-01 | No build descriptor or reproducible setup | High / High | No pom.xml or build.gradle | Add Java 21 build descriptor, dependency policy and setup; run clean build | Blocker |
| TD-02 | No runnable entry point or HTTP adapter | High / High | No @SpringBootApplication or REST controller | Implement dispatcher API and bootstrap | Blocker |
| TD-03 | Routing adapter is a stub | High / High | MapboxAdapter.calculate throws UnsupportedOperationException | Implement provider or clearly labelled deterministic synthetic adapter; define timeout/error mapping | Blocker |
| TD-04 | No persistence adapter or schema/migration | High / High | Only RouteRepository port exists | Implement schema, repository adapter and transaction rollback | Blocker |
| TD-05 | Business invariants not enforced in Java | High / High | No order, batch, vehicle, driver or lifecycle model | Implement BR-01 to BR-06 and guarded transitions | Blocker |
| TD-06 | No automated tests or test command | High / High | No test sources/build descriptor | Add unit tests for normal/rule/failure and integration tests | Blocker |
| TD-07 | API contract not implemented | High / High | POST /api/routes/optimize described; no controller found | Implement contract or document approved deviation | Blocker |
| TD-08 | Authentication/authorization not demonstrated | Medium / High | No security filter/controller authorization evidence | Enforce dispatcher role; test denied and unauthenticated requests | Blocker |
| TD-09 | Primary/replica deployment is design-only | Medium / High | No DB configuration, replication or monitoring | Verify read-only replica, lag, failover and recovery before production | Production gate |
| TD-10 | Provider credentials/data handling not operationally specified | Medium / High | Adapter placeholder; no secret/config guidance | Externalize secrets, use synthetic data, define retention/log redaction | Production gate |

## Residual risks
1. Route feasibility depends on accurate geocoding, travel-time estimates, traffic and service-time assumptions. Provider success alone does not prove operational feasibility.
2. Provider outage, timeout, rate limiting and malformed responses may block optimization. Fail closed: no partial route persisted and batch remains Draft.
3. Concurrent requests could optimize or assign the same batch/driver/vehicle twice without constraints, transactions, optimistic locking and idempotency.
4. Replica lag can show stale state. Consistency-sensitive reads must use primary; replica reads must be explicitly stale-tolerant.
5. Current skeleton does not establish authentication, privacy controls, audit logging, monitoring, backup/restore, failover or deployment hardening.

## Conditional release recommendation

**Current decision: NO-GO.** The repository does not demonstrate a runnable vertical slice or automated tests. This is an evidence decision, not a claim that the architecture is invalid.

Reconsider a controlled Phase 2 demonstration only after TD-01 through TD-08 pass:
- clean Java 21 build on a fresh checkout;
- documented setup and synthetic seed data;
- end-to-end dispatcher request through application service to persisted route result;
- tests for success, invalid capacity/window/shift, authorization denial, routing dependency failure and rollback/no partial write;
- observed test output committed as evidence;
- reconciliation matrix updated with actual code and test names.

Production additionally requires verified replication/failover/recovery, secret management, security review, observability, backup/restore exercise, provider resilience and operational ownership.

## Evidence integrity
No tests, build, live provider integration, database migration or deployment were run as part of this review. Do not label planned gates as passed without execution evidence.
