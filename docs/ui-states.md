# UI States – Logistics Route Decider System

**Traces to:** `models/use-case-model.md`, D-001 (external routing API), D-002 (State pattern, BR-01, BR-02, AC-02, AC-03), `architecture.md`, `models/logic-data-model.md` (circuit breaker and fallback)

---

## 1. Roles and tasks covered

| Role | Task | Screen |
|---|---|---|
| Dispatcher (primary) | Create a route for pending orders, review and approve it, assign it to a driver | S1 Dispatch Planner |
| Driver (secondary) | View the assigned route and update delivery status | S2 Driver Route View |

Out of scope for these screens: customer notifications, the logistics manager dashboard, and live GPS tracking (simulated/future). "Stale" refers to dispatch data (orders, vehicle and stop status), not live location.

---

## 2. Mapping to the D-002 state machine

| Route state (D-002) | Screen state |
|---|---|
| Draft | S1-01 (orders selected, no route yet) |
| Optimized | S1-11 (route generated, awaiting review) |
| Assigned | After the dispatcher approves and assigns (assumes **Approve Route** is the Optimized → Assigned transition; the team must confirm) |
| In Transit / Completed | S2 (driver updates status) |

---

## 3. Cross-cutting rules (apply to every state)

- **Non-colour cues:** every state pairs colour with an icon and text (⚠ Error, ✓ Delivered, ⟳ Loading).
- **Announcements:** errors and status changes use `role="alert"` or `aria-live="polite"`.
- **Focus:** after a failed submit, focus moves to the error summary; the first invalid field is linked from it. All controls show a visible focus outline.
- **Labels:** persistent visible labels above fields; placeholders are never the only label.
- **Data safety:** no error or reload clears the user's selections.
- **Responsive:** single column below 600 px; messages wrap and are never truncated.

---

## 4. S1 – Dispatch Planner (Dispatcher)

| ID | State | Trigger | What the user sees | Recovery | Traceability |
|---|---|---|---|---|---|
| S1-01 | Normal (Draft) | Pending orders exist, vehicle and driver available | Order list with weights, vehicle selector, load meter (e.g. 640 / 1000 kg), enabled **Generate route** | n/a | Create Delivery Order |
| S1-02 | Loading | Route being computed | ⟳ "Calculating route…"; Generate disabled; order list stays visible | **Cancel** link | Optimize Delivery Route |
| S1-03 | Empty | No pending orders at the depot | "No pending orders." with an **Add order** button | Add order | Create Delivery Order |
| S1-04 | Validation – payload | Selected load exceeds vehicle capacity | ⚠ Inline message beside the load meter: "Load 1,240 kg exceeds Van 1 limit of 1,000 kg." Generate disabled; the reason is also written directly under the button | Remove an order or select another vehicle | BR-01, AC-02 |
| S1-05 | Validation – fields | Missing address or weight, or non-numeric weight | ⚠ Error under the field plus an error summary at the top; focus moves to the summary | Correct the field, then resubmit | Create Delivery Order |
| S1-06 | Degraded | Routing API times out or the circuit breaker is open; fallback (cached or heuristic route) used | ⚠ Banner: "Approximate route. The routing service is unavailable, so distances are estimated." Route shown with an "Approximate" label on every distance and time | Review the route, or **Retry** with the live service later; **Approve** requires ticking "I accept approximate distances" | Optimize Delivery Route, D-001, logic-data-model (fallback) |
| S1-07 | Unavailable | No vehicle available | Vehicle selector disabled with the reason: "No vehicle available." | Wait, or contact the depot admin | Assign Route to Driver |
| S1-08 | Unauthorised | A Driver opens the planner | "You don't have permission to plan routes." | Link to the Driver's own route (S2) | Role access |
| S1-09 | Stale | Order or vehicle data older than the freshness limit | Banner: "Last updated 10:42. Data may be out of date." | **Refresh** button | Manage Delayed/Exception Delivery |
| S1-10 | Error – no route possible | Live service and fallback both fail | ⚠ "A route could not be calculated. Your selections are saved." | **Retry** button | Optimize Delivery Route |
| S1-11 | Review (Optimized) | Route generated | Stop order, total distance and time, load meter, **Approve route** and **Edit** buttons | Approve (then assign a driver) or edit | Review Optimized Route, Approve Route |
| S1-12 | Conflict | Another dispatcher already assigned the same order or vehicle | ⚠ "Order 14 was just assigned by another dispatcher." Selection updates; no duplicate assignment | Choose another order or vehicle | AC-03 (explicit locking) |
| S1-13 | Validation – shift limit | Route duration exceeds the driver's remaining shift | ⚠ "Route 9 h exceeds Driver A's remaining shift of 6 h." Assign disabled, with the reason shown | Choose another driver or remove stops | BR-02 |

---

## 5. S2 – Driver Route View (Driver)

| ID | State | Trigger | What the user sees | Recovery | Traceability |
|---|---|---|---|---|---|
| S2-01 | Normal | Route assigned | Ordered stop list with status icon and text per stop; ETA; **Mark delivered** button on the next stop | n/a | View Assigned Route, View ETA |
| S2-02 | Loading | Fetching the route | Skeleton stop list with the text "Loading your route…" | n/a | View Assigned Route |
| S2-03 | Empty | No route assigned | "No route assigned yet." | **Refresh** | View Assigned Route |
| S2-04 | Error – update | Status update fails | ⚠ "Couldn't mark Stop 2 delivered. Your update was not saved." with a full-width **Retry** button directly under the message, inside the same alert box. The stop keeps its previous status, labelled "Pending" | **Retry** (primary action in the alert) | Update Delivery Status |
| S2-05 | Stale/offline | Connection lost or data not refreshed | Banner "Showing route last synced 10:42." Status buttons disabled | Reconnect, then **Refresh** | Update Delivery Status |
| S2-06 | Unauthorised | Driver requests another driver's route | "You can only view your own route." | Link to own route | Role access |

---

## 6. Design rationale

**Choice 1 (validation):** show validation inline, next to the load meter and fields, with an error summary at the top.
**Alternative:** a blocking modal on submit.
**Why inline:** the dispatcher needs the order list visible to decide what to remove or change. A modal hides it and adds a dismissal step.
**Consequence accepted:** more layout work (summary, per-field messages, focus handling) and a longer page on small screens.

**Choice 2 (fallback):** show an approximate route with a clear label and require explicit acceptance before approval (S1-06).
**Alternative:** show a hard error when the routing API fails.
**Why:** it follows the failure model in `logic-data-model.md`, where the system degrades instead of failing, and keeps the dispatcher working.
**Consequence accepted:** approximate distances could affect BR-02 shift checks, so the dispatcher must consciously confirm.

---

## 7. Usability-driven revision

> Fill in after the three-person test (see `evidence/usability-test.md`).

| Item | Entry |
|---|---|
| Most important problem observed | P2 paused and appeared confused when the driver status update failed, before finding the Retry button (S2-04). Secondary: P1 hesitated at the disabled Generate button (S1-04). |
| Interface change made | S2-04: Retry moved inside the error message, full width, primary style; message says the update was not saved. S1-04: reason also written under the disabled Generate button. |
| Verification after implementation | Repeat the failed-update task and the overweight-load task with 3 new participants; each completes recovery without help or visible searching. |
| Commit | (add the commit message after committing) |

---

## 8. Consistency checklist before commit

- [ ] Every state ID appears in the wireframe annotations.
- [ ] Role names match `docs/user-profile-and-tasks.md` and `models/navigation.*`.
- [ ] The team has confirmed that Approve Route is the Optimized → Assigned transition.
- [x] Section 7 is filled in after the usability test.
