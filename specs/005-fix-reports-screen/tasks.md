---
description: "Task list for fixing the Reports screen UI"
---

# Tasks: Fix Reports Screen UI

**Input**: Design documents from `/specs/005-fix-reports-screen/`  
**Prerequisites**: plan.md (required), spec.md (required), research.md, data-model.md, contracts/, quickstart.md

**Tests**: Compose UI instrumentation must validate the Reports screen without a bottom bar, with consistent charts, and with currency-aware summaries across locales.

**Organization**: Single P1 user story; tasks structured to keep the slice independently shippable.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Task can run in parallel (no shared files).
- **[Story]**: Maps to user story (e.g., US1).
- Include exact file paths for traceability.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Confirm the workspace builds cleanly before UI refactors.

- [ ] T001 Run baseline build via `./gradlew assembleDebug` at `./` to verify the current state compiles.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Establish shared formatting primitives needed by the Reports UI fixes.

**⚠️ CRITICAL**: Complete before starting user story implementation.

- [X] T002 Introduce a currency formatting helper using `LocalUserPreferences` inside `app/src/main/java/com/barutdev/kora/ui/screens/reports/ReportsScreen.kt` to expose a user-selected `NumberFormat`.
- [X] T003 Define shared bar chart style constants and a locale-aware month label formatter in `app/src/main/java/com/barutdev/kora/ui/screens/reports/ReportsScreen.kt` for reuse across chart rendering.

**Checkpoint**: Reports utilities expose currency and chart formatting ready for UI consumption.

---

## Phase 3: User Story 1 - Polished cross-locale reports (Priority: P1) 🎯 MVP

**Goal**: Deliver a Reports screen that hides the bottom nav, shows consistent charts, formats totals with the tutor’s currency, and keeps summary cards readable in long-text locales.

**Independent Test**: From Student List, open Reports in English and German; ensure no bottom navigation appears, month labels render uniformly, totals show the correct currency symbol, and summary cards wrap without clipping.

### Tests for User Story 1

- [X] T004 [US1] Author instrumentation coverage in `app/src/androidTest/java/com/barutdev/kora/ui/screens/reports/ReportsScreenUiTest.kt` asserting bottom navigation semantics are absent, chart bars share style with localized labels, and earnings show the user currency in English and German locales.

### Implementation for User Story 1

- [X] T005 [US1] Apply the user-selected currency formatter to `SummaryCards` in `app/src/main/java/com/barutdev/kora/ui/screens/reports/ReportsScreen.kt`, wiring through `LocalUserPreferences`.
- [X] T006 [US1] Refactor the summary cards layout in `app/src/main/java/com/barutdev/kora/ui/screens/reports/ReportsScreen.kt` to a wrap-friendly arrangement (e.g., FlowRow/Column) that preserves alignment for long translations.
- [X] T007 [US1] Update the `BarChart` composable in `app/src/main/java/com/barutdev/kora/ui/screens/reports/ReportsScreen.kt` to use the shared style constants, remove special-case styling, and render uppercase locale abbreviations under each bar.
- [X] T008 [US1] Add a German locale preview in `app/src/main/java/com/barutdev/kora/ui/screens/reports/ReportsScreen.kt` to visually sanity-check summary card wrapping and chart labels.

**Checkpoint**: Reports screen renders without a bottom bar, honors currency settings, and adapts layout across locales with tests guarding regressions.

---

## Phase N: Polish & Cross-Cutting Concerns

**Purpose**: Validate the end-to-end experience before merge.

- [ ] T009 Run `./gradlew testDebugUnitTest connectedDebugAndroidTest` at `./` to cover regression and new UI tests.
- [ ] T010 Execute the manual QA flow from `specs/005-fix-reports-screen/quickstart.md`, logging results in feature notes.

---

## Dependencies & Execution Order

- Phase 1 ensures a clean build baseline.  
- Phase 2 establishes shared formatting utilities required for the story.  
- Phase 3 (US1) depends on Phase 2 and delivers the MVP slice.  
- Phase N runs after US1 implementation and testing.

### Task Dependencies

- T005 consumes the currency formatter introduced in T002.  
- T007 relies on the chart defaults from T003.  
- T004 should be updated after T005–T007 so tests reflect the new behaviour.  
- T008 benefits from the responsive layout produced in T006.

### Parallel Opportunities

- After T006, T008 can proceed while T004 finalizes instrumentation logic.  
- Localization-friendly chart styling (T007) and summary layout refactor (T006) touch adjacent sections of the same file; keep them sequential to avoid merge conflicts.

### User Story Completion Order

- Only US1 exists; it depends on foundational formatting helpers and unlocks Polish tasks upon completion.

---

## Parallel Example: User Story 1

```bash
# After completing T005–T007 sequentially:

# Terminal 1 – refine instrumentation assertions
nvim app/src/androidTest/java/com/barutdev/kora/ui/reports/ReportsScreenUiTest.kt

# Terminal 2 – adjust German preview content
nvim app/src/main/java/com/barutdev/kora/ui/screens/reports/ReportsScreen.kt
```

---

## Implementation Strategy

### MVP First

1. Land formatting helpers (T002–T003).  
2. Wire currency-aware summaries and responsive layouts (T005–T007).  
3. Backfill instrumentation coverage (T004) and add the German preview (T008).

### Incremental Delivery

1. Ship foundational helpers and currency-aware summaries as the first increment.  
2. Follow with chart styling, layout refactor, and tests in a focused PR.  
3. Finish with polish tasks (T009–T010) before requesting review.
