---
description: "Task list for refactoring the Reports entry point"
---

# Tasks: Refactor Reports Entry Point

**Input**: Design documents from `/specs/004-refactor-reports-entry-point/`  
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/, quickstart.md

**Tests**: Compose UI instrumentation coverage must confirm the reports icon navigation and three-tab bottom bar behaviour.

**Organization**: Tasks grouped so the single P1 story is independently shippable.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Task can run in parallel (no shared files).
- **[Story]**: Maps to user story (e.g., US1).
- Include exact file paths for traceability.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Validate the workspace before changing navigation code.

- [ ] T001 Run baseline build via `./gradlew assembleDebug` at `./` to ensure a clean starting point.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Simplify the shared navigation contract ahead of the story work.

**⚠️ CRITICAL**: Complete before starting user story implementation.

- [ ] T002 Prune reports from `bottomBarDestinations` in `app/src/main/java/com/barutdev/kora/navigation/KoraDestinations.kt`.
- [ ] T003 Align bottom bar rendering with the three-tab list in `app/src/main/java/com/barutdev/kora/navigation/KoraNavGraph.kt`, removing the reports branch from the bottom nav click handler.

**Checkpoint**: Bottom navigation renders only Dashboard, Calendar, Homework with no dead code for Reports.

---

## Phase 3: User Story 1 - Access reports from home (Priority: P1) 🎯 MVP

**Goal**: Let tutors open the global Reports screen from the Student List top bar while keeping student-scoped context intact.

**Independent Test**: From Student List, tap the reports icon, land on the Reports screen, then navigate back and confirm the previous student destination remains active with the bottom bar still showing three tabs.

### Tests for User Story 1

- [ ] T004 [US1] Update the navigation instrumentation coverage in `app/src/androidTest/java/com/barutdev/kora/ui/student_list/StudentListReportsNavigationTest.kt` to assert the reports icon navigation and bottom bar destinations.

### Implementation for User Story 1

- [ ] T005 [US1] Inject an `onNavigateToReports` callback using `navigateToReports(launchSingleTop = true)` inside `app/src/main/java/com/barutdev/kora/navigation/KoraNavGraph.kt` when composing `StudentListScreen`.
- [ ] T006 [US1] Add the Material 3 bar chart top bar action in `app/src/main/java/com/barutdev/kora/ui/screens/student_list/StudentListScreen.kt`, wiring it to the new callback and `ScreenScaffoldConfig`.
- [ ] T007 [US1] Add `student_list_reports_action_description` to `app/src/main/res/values/strings.xml` for accessibility text.
- [ ] T008 [P] [US1] Localize `student_list_reports_action_description` in `app/src/main/res/values-de/strings.xml`.
- [ ] T009 [P] [US1] Localize `student_list_reports_action_description` in `app/src/main/res/values-tr/strings.xml`.

**Checkpoint**: Reports icon appears on Student List, navigation preserves back stack, and localization is complete.

---

## Phase N: Polish & Cross-Cutting Concerns

**Purpose**: Harden the feature before merge.

- [ ] T010 Run `./gradlew testDebugUnitTest connectedDebugAndroidTest` at `./` to exercise regression suites.
- [ ] T011 Perform manual validation steps from `specs/004-refactor-reports-entry-point/quickstart.md`, documenting results in the feature notes.

---

## Dependencies & Execution Order

- Phase 1 unlocks Phase 2 by proving the workspace builds.
- Phase 2 updates the shared navigation list and must finish before Phase 3 tasks.
- Phase 3 implements and verifies the P1 story; translations (T008, T009) can start after the base string (T007) exists.
- Phase N runs once user story work is complete.

### Task Dependencies

- T003 consumes the destination list defined in T002.
- T004–T009 assume the bottom bar only has three tabs (T002–T003).
- T008 and T009 depend on adding the base string in T007.
- T010 should run after all story tasks to validate tests; T011 follows the automated runs.

### Parallel Opportunities

- After T007, run T008 and T009 in parallel because they touch different locale files.
- While localization updates run, T004 can be authored independently to cover the new navigation flow.

### User Story Completion Order

- Only User Story 1 exists; it depends on the foundational navigation cleanup and gates the polish tasks.

---

## Parallel Example: User Story 1

```bash
# After completing T005–T007:

# Terminal 1 – update localized strings
nvim app/src/main/res/values-de/strings.xml

# Terminal 2 – update instrumentation coverage
nvim app/src/androidTest/java/com/barutdev/kora/ui/student_list/StudentListReportsNavigationTest.kt

# Terminal 3 – translate Turkish strings
nvim app/src/main/res/values-tr/strings.xml
```

---

## Implementation Strategy

### MVP First

1. Finish T002–T003 to stabilize the bottom navigation list.
2. Implement T005–T007 to expose the reports icon path and navigation callback.
3. Localize (T008–T009) and extend instrumentation coverage (T004) to prove behaviour.

### Incremental Delivery

1. Ship the foundational navigation cleanup as a preparatory PR if needed.
2. Follow with the Student List top bar update and tests as the primary increment.
3. Use the polish tasks (T010–T011) before release to guard regressions and capture manual findings.
