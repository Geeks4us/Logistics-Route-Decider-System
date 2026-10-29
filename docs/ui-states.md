# UI States – Logistics Route Decider System

**Team:** Geeks4us (Group 01) | **Course:** CSI473 Lab 10 | **Branch:** lab-10
**Scope:** Single Gaborone Central depot, 5–10 synthetic orders, 1–2 vehicles.
**Related artefacts:** `docs/user-profile-and-tasks.md`, `models/navigation.*`, `prototype/wireframes.*`, `docs/accessibility-checklist.md`

> Replace every `REQ-xx` / `UC-xx` placeholder with the real IDs from the team's requirements document before committing.

---

## 1. Roles and tasks covered

| Role | Task | Screen |
|---|---|---|
| Dispatcher (primary) | Assign orders to a vehicle and generate a route | S1 Dispatch Planner |
| Driver (secondary) | View assigned route and update stop status | S2 Driver Route View |

Live GPS tracking and mid-transit re-sequencing are out of scope (simulated/future). "Stale" below refers to dispatch data (orders, vehicle and stop status), not live location.

---

## 2. Cross-cutting rules (apply to every state)

- **Non-colour cues:** every state pairs colour with an icon and text (e.g. ⚠ Error, ✓ Delivered, ⟳ Loading).
- **Announcements:** errors and status changes use `role="alert"` or `aria-live="polite"`.
- **Focus:** after a failed submit, focus moves to the error summary; the first invalid field is linked from it. All controls, including Retry and Refresh, show a visible focus outline.
- **Labels:** persistent visible labels above fields; placeholders are never the only label.
- **Data safety:** no error or reload clears the user's selections.
- **Responsive:** single-column layout below 600 px; messages wrap and are never truncated.

---

## 3. S1 – Dispatch Planner (Dispatcher)

| ID | State | Trigger | What the user sees | Recovery | Traceability |
|---|---|---|---|---|---|
| S1-01 | Normal | Pending orders exist, vehicle available | Order list with weights, vehicle selector, load meter (e.g. 640 / 1000 kg), enabled **Generate route** | n/a | REQ-xx |
| S1-02 | Loading | Route being computed using the distance matrix | ⟳ "Calculating route…"; Generate button disabled; order list stays visible | **Cancel** link | REQ-xx |
| S1-03 | Empty | No pending orders at the depot | "No pending orders." with an **Add order** button | Add order | REQ-xx |
| S1-04 | Validation – payload | Selected load exceeds vehicle capacity | ⚠ Inline message beside the load meter: "Load 1,240 kg exceeds Van 1 limit of 1,000 kg." Generate disabled | Remove an order or select another vehicle | REQ-xx (payload violation prevention) |
| S1-05 | Validation – fields | Missing address or weight, or non-numeric weight | ⚠ Error under the field plus an error summary at the top; focus moves to the summary | Correct the field, then resubmit | REQ-xx |
| S1-06 | Error – service | Spatial/distance service fails or times out | ⚠ "Route could not be calculated. Your selections are saved." | **Retry** button | REQ-xx |
| S1-07 | Unavailable | No vehicle available | Vehicle selector disabled with the reason: "No vehicle available." | Wait, or contact the depot admin | REQ-xx |
| S1-08 | Unauthorised | A Driver opens the planner | "You don't have permission to plan routes." | Link to the Driver's own route (S2) | REQ-xx |
| S1-09 | Stale | Order or vehicle data older than the freshness limit | Banner: "Last updated 10:42. Data may be out of date." | **Refresh** button | REQ-xx (dispatch visibility) |

---

## 4. S2 – Driver Route View (Driver)

| ID | State | Trigger | What the user sees | Recovery | Traceability |
|---|---|---|---|---|---|
| S2-01 | Normal | Route assigned | Ordered stop list with status icon and text per stop; **Mark delivered** button on the next stop | n/a | REQ-xx |
| S2-02 | Loading | Fetching the route | Skeleton stop list with the text "Loading your route…" | n/a | REQ-xx |
| S2-03 | Empty | No route assigned | "No route assigned yet." | Refresh | REQ-xx |
| S2-04 | Error – update | Status update fails | ⚠ "Couldn't mark Stop 2 delivered. Try again." The stop keeps its previous status | **Retry** button | REQ-xx |
| S2-05 | Stale/offline | Connection lost or data not refreshed | Banner "Showing route last synced 10:42." Status buttons disabled | Reconnect, then **Refresh** | REQ-xx |
| S2-06 | Unauthorised | Driver requests another driver's route | "You can only view your own route." | Link to own route | REQ-xx |

---

## 5. Design rationale

**Choice:** payload and field validation is shown inline, next to the load meter and fields, with an error summary at the top.
**Realistic alternative:** a blocking modal dialog on submit.
**Why inline:** the dispatcher needs the order list visible to decide which order to remove or which vehicle to switch to. A modal hides it and adds a dismissal step.
**Consequence accepted:** more layout work (summary, per-field messages, focus handling) and a longer page on small screens.

---

## 6. Usability-driven revision

> Fill in after the three-person test (see `evidence/usability-test.md`).

| Item | Entry |
|---|---|
| Most important problem observed | *(e.g. testers did not notice why Generate was disabled)* |
| Interface change made | *(e.g. added a visible reason text next to the disabled button, state S1-04)* |
| Verification after implementation | *(e.g. repeat task with 3 users; all identify the cause within 10 s without help)* |
| Commit | *(commit hash or message)* |

---

## 7. Consistency checklist before commit

- [ ] Every state ID appears in the wireframe annotations.
- [ ] Role names match `docs/user-profile-and-tasks.md` and `models/navigation.*`.
- [ ] Every row has a real requirement ID.
- [ ] Accessibility rules in section 2 match `docs/accessibility-checklist.md`.
- [ ] Exit record in section 6 is filled in.

