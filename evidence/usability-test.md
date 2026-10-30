# Usability Test Record – Logistics Route Decider System

---

## 1. Purpose and traceability

Check whether a first-time user can complete the two critical tasks using the wireframes, and whether the alternative states are understood.

| Task | Use case(s) | Screen/states tested |
|---|---|---|
| T1 (Dispatcher): assign orders to a vehicle and generate a route | Create Delivery Order, Optimize Delivery Route, Assign Route to Driver | S1, states S1-01, S1-04, S1-06 |
| T2 (Driver): find the next stop and mark it delivered | View Assigned Route, Update Delivery Status | S2, states S2-01, S2-04 |

---

## 2. Method

- **Participants:** 3 people who are not on the team, no prior view of the design.
- **Prototype:** wireframes, shown one screen at a time.
- **Procedure:** read the scenario aloud, ask the participant to think aloud, give no hints, record what they say and do.
- **Measures per task:** completed without help (yes/no), wrong turns or hesitations, comments.
- **Severity scale:** 1 = cosmetic, 2 = slows the user, 3 = blocks the task.

**Participants**

| ID | Background |
|---|---|
| P1 | Final-year Computer Science student, moderate familiarity with delivery tracking apps (e.g. courier apps) |
| P2 | Business Management student, no prior experience with logistics or dispatch software |
| P3 | Information Technology student, frequent user of map and navigation tools |

---

## 3. Task scenarios and success criteria

**T1 (Dispatcher).** "You have 4 pending orders at the Gaborone Central depot. Assign them to Van 1 (limit 1,000 kg) and generate a route." Orders are set so the first selection is 1,240 kg.
Success: the user notices why Generate is disabled, removes an order or changes the vehicle, and generates the route without help.

**T2 (Driver).** "Your route is assigned. Find your next stop and mark it delivered." Mid-task, the update fails once.
Success: the user finds the next stop, marks it delivered, and recovers from the failed update using Retry.

---

## 4. Observations

| Participant | Task | Result | Observation | Severity |
|---|---|---|---|---|
| P1 | T1 | Pass with difficulty | Hesitated at the disabled Generate button after selecting an overweight load (over Van 1's limit). Worked out the cause independently and removed an order to get under the limit. | 2 |
| P1 | T2 | Pass | Completed the task without help. | n/a |
| P2 | T1 | Pass | Completed the task without help. | n/a |
| P2 | T2 | Pass with difficulty | Paused and appeared confused when the status update failed mid-task. Eventually located the Retry button and used it to recover. | 2 |
| P3 | T1 | Pass | Completed the task without help. | n/a |
| P3 | T2 | Pass | Completed the task without help. | n/a |

---

## 5. Findings and revision

| Item | Entry |
|---|---|
| Most important usability problem observed | When the driver's status update failed (T2, S2-04), P2 paused and appeared confused before finding the Retry button. A driver who cannot find Retry may leave a delivery unrecorded. Secondary: P1 hesitated at the disabled Generate button (T1, S1-04). |
| Evidence (participants and rows above) | Section 4 rows P2/T2 (main) and P1/T1 (secondary) |
| Interface change made | S2-04: the Retry button now sits inside the error message, directly under the text, full width and styled as the primary action; the message states the update was not saved. S1-04: the reason is also written directly under the disabled Generate button, not only beside the load meter. |
| Screens/states updated | S2-04 and S1-04 in `docs/ui-states.md`; matching wireframes S2 and S1; accessibility checks A-05 and A-10 |
| How it will be verified after implementation | Repeat T2 (with the failed update) and T1 (with the overweight load) with 3 new participants. Pass: each finds and taps Retry, and explains why Generate is disabled, without help or visible searching. |
| Commit | (add the commit message after committing) |

---

## 6. Design rationale

**Choice:** a small task-based test with think-aloud on wireframes, before any code exists.
**Realistic alternative:** a questionnaire after the sessions.
**Why this choice:** a questionnaire records opinions, while think-aloud shows where users hesitate and why.
**Consequence accepted:** only 3 participants, so the findings show likely problems, not statistics, and the test happens on wireframes rather than the real system.

