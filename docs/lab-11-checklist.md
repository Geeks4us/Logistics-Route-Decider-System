# Laboratory 11 — Exit and Phase 2 Evidence Checklist

Status on 30 October 2026; distinguish committed source from verified execution.

- [x] Create lab-11 from reviewed lab-09 baseline.
- [ ] Create stable pre-lab tag.
- [x] Implement HTTP → application workflow → routing port → transactional persistence vertical-slice source.
- [x] Add Java 21 / Spring Boot Maven build descriptor and README setup/run instructions.
- [x] Add H2 demo schema and synthetic Draft batch seed.
- [x] Add automated HTTP integration tests for success, capacity rejection and unreachable stop.
- [ ] Run mvn clean test on JDK 21 and record actual output.
- [ ] Run application and complete five-minute demonstration from a clean checkout.
- [x] Create requirement/model/code/test reconciliation and disclose differences.
- [x] Create technical-debt/risk register and conditional release recommendation.
- [ ] Add provider failure, shift/window, idempotency replay and transaction rollback tests.
- [ ] Implement dispatcher authentication/authorization and test denial.
- [ ] Replace demo H2 and synthetic routing before production.
- [ ] Add Phase 2 report PDF at submissions/phase2-report.pdf.
- [ ] Add and verify contribution record.
- [ ] Create phase2-submission tag after final review.
- [ ] Submit Moodle Assignment 3 by 23:59 Friday 30 October 2026 (team/student action).

## Current exit status
The vertical-slice implementation and test sources are committed, but the build and tests have **not been executed here**. Do not claim the system is verified runnable until a Java 21/Maven environment successfully runs the documented commands. Production release remains NO-GO.
