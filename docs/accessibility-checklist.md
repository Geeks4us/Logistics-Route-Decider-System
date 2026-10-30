# Accessibility Checklist – Logistics Route Decider System

Each requirement states how the design meets it. Tick "Verified" only after it has been checked on the wireframe or the built screen.

> Replace `REQ-xx` with the real quality requirement IDs.

---

## 1. Required checks (Lab 10 minimum standard)

| ID | Requirement | How the design meets it | Screens/states | How to verify | Verified |
|---|---|---|---|---|---|
| A-01 | **Keyboard access** | Every control (order list, vehicle selector, Generate, Retry, Refresh, Mark delivered) is reachable with Tab and activated with Enter/Space; logical tab order top to bottom | S1, S2, all states | Complete T1 and T2 using only the keyboard | [ ] |
| A-02 | **Visible focus** | Every focusable element shows a clear outline, including inside banners and dialogs | S1-06, S1-09, S2-04, S2-05 | Tab through each screen and confirm the outline is never hidden | [ ] |
| A-03 | **Persistent labels** | Labels sit above fields and never disappear; placeholders are examples only | S1-05 (address, weight fields) | Check that every field keeps its label when filled or in error | [ ] |
| A-04 | **Non-colour cues** | State is shown by icon and text as well as colour (⚠ Error, ✓ Delivered, ⟳ Loading) | S1-04, S1-06, S2-01, S2-04 | View the wireframes in greyscale and confirm every state is still clear | [ ] |
| A-05 | **Clear errors** | Messages say what went wrong and how to fix it (e.g. "Load 1,240 kg exceeds Van 1 limit of 1,000 kg"); error summary at the top of the form; focus moves to it | S1-04, S1-05, S1-06, S2-04 | Trigger each error and read the message without help | [ ] |
| A-06 | **Readable responsive layout** | Single column below 600 px; text wraps; no horizontal scrolling; tap targets at least 44 px | S1, S2 | View at phone width and at 200% text size | [ ] |

---

## 2. Additional checks

| ID | Requirement | How the design meets it | How to verify | Verified |
|---|---|---|---|---|
| A-07 | Text contrast of at least 4.5:1 against the background | Dark text on light surfaces; no grey-on-grey for warnings | Check pairs in a contrast checker | [ ] |
| A-08 | Status changes are announced to screen readers | Errors use `role="alert"`; loading and stale banners use `aria-live="polite"` | Test with a screen reader on the built page | [ ] |
| A-09 | Loading is not shown by a spinner alone | Spinner is always paired with text ("Calculating route…") | Review S1-02, S2-02 | [ ] |
| A-10 | Disabled buttons explain why | Reason text beside Generate when it is disabled (S1-04, S1-07) | Review the wireframes | [ ] |

---

## 3. Traceability

| Check | Quality requirement | Related states |
|---|---|---|
| A-01 to A-06 | REQ-xx (usability/accessibility) | S1-01 to S1-09, S2-01 to S2-06 |
| A-05, A-10 | REQ-xx (payload violation prevention) | S1-04 |
| A-08, A-09 | REQ-xx (dispatch visibility) | S1-02, S1-09, S2-05 |

---

## 4. Design rationale

**Choice:** meet accessibility through visible text, icons and structure built into every state, not as a separate "accessible mode".
**Realistic alternative:** a high-contrast theme toggle.
**Why this choice:** a toggle leaves the default screens inaccessible, and dispatchers and drivers use the same default screens.
**Consequence accepted:** every state needs an icon and text, which means more layout work on small screens.

---

## 5. Revision after the usability test

| Check changed | Reason (link to `evidence/usability-test.md` row) | Change made |
|---|---|---|
| A-05, A-10 | `evidence/usability-test.md` rows P2/T2 and P1/T1 | S2-04: Retry placed inside the error message and the message says the update was not saved. S1-04: reason written directly under the disabled Generate button. |

