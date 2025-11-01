# Implementation Plan: Refactor Reports Entry Point

**Branch**: `004-refactor-reports-entry-point` | **Date**: 2025-10-31 | **Spec**: `specs/004-refactor-reports-entry-point/spec.md`
**Input**: Feature specification from `/specs/004-refactor-reports-entry-point/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/scripts/bash/setup-plan.sh` for the execution workflow.

## Summary

Restore the bottom navigation bar to its original three-tab layout (Dashboard, Calendar, Homework) and surface the global Reports screen from the Student List top app bar. We will remove the Reports bottom tab wiring inside `KoraNavGraph.kt`, introduce a dedicated reports `TopBarAction` on the Student List via `TopBarConfig`, and reuse the existing `navigateToReports()` helper with singleTop semantics so tutors can open Reports and return without losing their current student-scoped context.

## Technical Context

**Language/Version**: Kotlin (K2) targeting JVM 17  
**Primary Dependencies**: Jetpack Compose Material 3, Hilt, Kotlin Coroutines & Flow, Room (KSP), Navigation Compose  
**Storage**: Room database with KSP processors (no KAPT)  
**Testing**: JUnit, kotlinx-coroutines-test, Compose UI tests, Room in-memory tests  
**Target Platform**: Android (per app `minSdk`) with Compose-only UI layer  
**Project Type**: Android mobile app using Clean Architecture (UI → ViewModel → UseCase → Repository → DataSource)  
**Performance Goals**: 60fps UI, <150ms state propagation, resilient offline persistence  
**Constraints**: No XML layouts, no alternative DI/navigation stacks, immutable data models, strings in `strings.xml`  
**Scale/Scope**: Multi-screen tutor management workflows within `com.barutdev.kora`

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- UI layer change (Compose `TopBarAction`) dispatches navigation intents to `NavController`; no business logic bypass.
- Compose Material 3 icon button added to Student List top bar; no XML introduced.
- No new asynchronous work; navigation remains on UI dispatcher, so coroutine guardrail satisfied.
- Room persistence untouched; immutable state objects remain unchanged.
- Hilt setup unchanged; Navigation Compose route list updated and `strings.xml` extended with reports action description.

## Project Structure

### Documentation (this feature)

```text
specs/004-refactor-reports-entry-point/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
app/
├── src/main/java/com/barutdev/kora/
│   ├── data/
│   │   ├── datasource/
│   │   └── repository/
│   ├── di/
│   ├── domain/
│   │   ├── model/
│   │   └── usecase/
│   ├── navigation/
│   └── ui/
│       ├── components/
│       └── screens/
└── src/main/res/
    ├── values/strings.xml
    └── values-*/strings.xml

app/src/androidTest/...
app/src/test/...
```

**Structure Decision**: Reference affected packages/files within this structure and describe any new feature subpackages added for the work.

- `app/src/main/java/com/barutdev/kora/navigation/KoraNavGraph.kt`
- `app/src/main/java/com/barutdev/kora/navigation/KoraDestinations.kt`
- `app/src/main/java/com/barutdev/kora/ui/screens/student_list/StudentListScreen.kt`
- `app/src/main/res/values*/strings.xml`

## Phase Plan

- **Phase 0 – Research**: Confirm navigation patterns for preserving student-scoped state while introducing non-tab entry points; choose Material 3 iconography and accessibility text.
- **Phase 1 – Design Updates**:
  - Update `KoraDestinations.bottomBarDestinations` to exclude `Reports`.
  - Refactor `KoraNavGraph` bottom bar rendering and click handling to remove the reports branch, while ensuring `bottomBarState` tracking for student tabs remains intact.
  - Add a new `TopBarAction` in the Student List `TopBarConfig` that navigates to Reports via `launchSingleTop`, preserving the current back stack.
  - Inject the new string resource and verify localization coverage.
- **Phase 2 – Validation Prep**: Outline Compose UI test adjustments (bottom bar tab count, reports icon navigation) and manual regression steps for returning to the previous student dashboard.

## Implementation Details by File

- **`KoraNavGraph.kt`**
  - Prune `KoraDestination.Reports` from the `KoraBottomNavigation` item list; adjust `shouldShowBottomBar` logic if it assumes four entries.
  - Remove the `when` branch that calls `navController.navigateToReports()` inside the bottom bar `onNavigate` lambda.
  - Ensure `BottomBarState` operations remain untouched for `Dashboard`, `Calendar`, and `Homework`.
  - Confirm `navigateToReports()` still exists and is reachable from the new top bar action; apply `launchSingleTop = true` so returning via back stack restores the previous student context (per FR-007).
- **`StudentListScreen.kt`**
  - Extend the `TopBarConfig` to include an additional `TopBarAction` preceding the settings action using `Icons.Outlined.BarChart`.
  - Source the content description from the new `student_list_reports_action_description` string.
  - Inject `LocalKoraScaffoldController` action wiring via `ScreenScaffoldConfig` so the reports icon is only present while Student List owns the chrome.
  - Trigger navigation by calling the new callback `onNavigateToReports`, provided by `KoraNavGraph` when composing `StudentListScreen`.
- **`strings.xml` (base + translations)**
  - Add localized entries for `student_list_reports_action_description`.
- **`KoraDestinations.kt`**
  - Reduce `bottomBarDestinations` to `[Dashboard, Calendar, Homework]` while keeping the `Reports` object for direct navigation and label reuse.
