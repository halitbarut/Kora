---

description: "Task list template for feature implementation"
---

# Tasks: [FEATURE NAME]

**Input**: Design documents from `/specs/[###-feature-name]/`  
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: Add explicit test tasks only when the specification or constitution mandates them. Compose UI tests and coroutine unit tests must fail first, then be fixed.

**Organization**: Group tasks by user story so each slice can ship, test, and roll back independently.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Task can run in parallel (no shared files).
- **[Story]**: Maps to user story (e.g., US1, US2, US3).
- Include exact file paths (`app/src/main/java/com/barutdev/kora/...`) for traceability.

## Path Conventions

- Kotlin sources: `app/src/main/java/com/barutdev/kora/<layer>/<feature>/...`
- Compose UI: `app/src/main/java/com/barutdev/kora/ui/<feature>/...`
- Navigation: `app/src/main/java/com/barutdev/kora/navigation/...`
- DI modules: `app/src/main/java/com/barutdev/kora/di/...`
- Repositories & data sources: `app/src/main/java/com/barutdev/kora/data/...`
- Room entities/DAO: `app/src/main/java/com/barutdev/kora/data/datasource/local/...`
- Resources & localization: `app/src/main/res/values*/strings.xml`
- Unit tests: `app/src/test/java/com/barutdev/kora/...`
- Instrumented UI tests: `app/src/androidTest/java/com/barutdev/kora/...`

<!-- 
  ============================================================================
  IMPORTANT: The tasks below are SAMPLE TASKS. `/speckit.tasks` MUST replace
  them with real tasks derived from the plan/spec/data-model/contracts.
  ============================================================================
-->

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Align scaffolding with the constitution before feature work.

- [ ] T001 Audit `gradle/libs.versions.toml` and declare any new dependencies.
- [ ] T002 Update `app/src/main/java/com/barutdev/kora/navigation/Routes.kt` with placeholder destinations.
- [ ] T003 [P] Create/adjust Hilt module skeleton in `app/src/main/java/com/barutdev/kora/di/[feature]Module.kt`.
- [ ] T004 [P] Add localization placeholders to `app/src/main/res/values/strings.xml`.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Establish Clean Architecture plumbing required by every story.

**⚠️ CRITICAL**: All user stories are blocked until this phase is complete.

- [ ] T005 Define immutable domain models in `app/src/main/java/com/barutdev/kora/domain/model/`.
- [ ] T006 Create Room entities + DAO interfaces with KSP annotations.
- [ ] T007 Implement repositories mapping DAO entities to domain models.
- [ ] T008 Add use cases exposing suspend/Flow APIs for the ViewModel.
- [ ] T009 Wire Hilt bindings for repository/use case stack.

**Checkpoint**: Domain and data layers expose tested Flows/suspend functions.

---

## Phase 3: User Story 1 - [Title] (Priority: P1) 🎯 MVP

**Goal**: [Describe the value delivered by this story]

**Independent Test**: [How to verify this story in isolation]

### Tests for User Story 1 (optional unless mandated)

- [ ] T010 [P] [US1] `ViewModel` coroutine test in `app/src/test/.../[Feature]ViewModelTest.kt`.
- [ ] T011 [P] [US1] Compose UI screenshot/semantics test in `app/src/androidTest/.../[Feature]ScreenTest.kt`.

### Implementation for User Story 1

- [ ] T012 [P] [US1] Build Compose screen components in `ui/[feature]/`.
- [ ] T013 [P] [US1] Implement ViewModel exposing `StateFlow` state + events.
- [ ] T014 [US1] Integrate use cases and repository calls in the ViewModel.
- [ ] T015 [US1] Register navigation destination + arguments.
- [ ] T016 [US1] Add localized strings and preview data.

**Checkpoint**: User Story 1 is shippable and passes UI + ViewModel tests.

---

## Phase 4: User Story 2 - [Title] (Priority: P2)

**Goal**: [Describe P2 scope]

**Independent Test**: [How to validate P2 alone]

### Tests for User Story 2

- [ ] T017 [P] [US2] Repository integration test using in-memory Room database.
- [ ] T018 [US2] Compose UI test covering new states/edge cases.

### Implementation for User Story 2

- [ ] T019 [P] [US2] Extend domain models/use cases for new behavior.
- [ ] T020 [US2] Update ViewModel reducers and expose additional signals.
- [ ] T021 [US2] Adjust Compose UI to render new states.
- [ ] T022 [US2] Update navigation graph if new routes/arguments introduced.

**Checkpoint**: User Stories 1 and 2 function independently with coverage.

---

## Phase 5: User Story 3 - [Title] (Priority: P3)

**Goal**: [Describe P3 scope]

**Independent Test**: [How to validate P3 alone]

### Tests for User Story 3

- [ ] T023 [P] [US3] Domain use case test covering new branches.
- [ ] T024 [US3] Compose interaction test for corner cases.

### Implementation for User Story 3

- [ ] T025 [P] [US3] Add/modify Room queries + migrations if needed.
- [ ] T026 [US3] Update repositories and ViewModel event handlers.
- [ ] T027 [US3] Polish UI, accessibility labels, and localization entries.

**Checkpoint**: All priority stories pass tests and remain independently deployable.

---

[Add more user story phases following this pattern when required]

---

## Phase N: Polish & Cross-Cutting Concerns

**Purpose**: Hardening work that spans multiple stories.

- [ ] TXXX [P] Compose theming/typography refinements.
- [ ] TXXX Audit `strings.xml` for unused keys and update translations.
- [ ] TXXX Performance profiling + snapshot tests.
- [ ] TXXX [P] Additional unit/UI tests requested post-review.
- [ ] TXXX Documentation updates (`docs/`, `README.md`, feature quickstarts).
- [ ] TXXX Run full `./gradlew assembleDebug` + lint before release.

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: Starts immediately.
- **Foundational (Phase 2)**: Depends on Phase 1 and gates all user stories.
- **User Stories (Phase 3+)**: Unlocked after foundational plumbing; deliver in priority order unless parallel teams own separate slices.
- **Polish**: Begins once desired stories are feature-complete.

### User Story Dependencies

- **User Story 1 (P1)**: Baseline experience; no dependencies on other stories.
- **User Story 2 (P2)**: May reuse US1 data but must stay independently testable.
- **User Story 3 (P3)**: Can extend prior stories but must not break their tests.

### Within Each User Story

- Write failing tests before implementation when tests are included.
- Define immutable models before repositories; repositories before use cases; use cases before ViewModels; ViewModels before UI.
- Keep localization updates and navigation wiring in sync with ViewModel work.

### Parallel Opportunities

- Tasks flagged `[P]` touch distinct files/modules and can run simultaneously.
- After foundational work, different engineers may implement separate user stories in parallel.
- Compose UI work and localization updates often parallelize once ViewModel contracts are stable.

---

## Parallel Example: User Story 1

```bash
# Run failing tests first
./gradlew testDebugUnitTest --tests "*[Feature]ViewModelTest"
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.barutdev.kora.[Feature]ScreenTest

# Parallel implementation tasks
Task: "Build Compose screen components in ui/[feature]/"
Task: "Implement ViewModel exposing StateFlow state + events"
Task: "Register navigation destination + arguments"
```

---
