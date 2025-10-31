# Feature Specification: Refactor Reports Entry Point

**Feature Branch**: `[004-refactor-reports-entry-point]`  
**Created**: 2025-10-31  
**Status**: Draft  
**Input**: User description: "As a tutor, I want to access the global reports from the main screen of the app, not from within a specific student's context, to make the bottom navigation less cluttered."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Access reports from home (Priority: P1)

The tutor starts on the Student List home screen and needs a lightweight way to jump to the global Reports view without relying on the bottom navigation tab.

**Why this priority**: The tutor’s primary workflow begins on the Student List; clearing the bottom bar and adding a top-level shortcut reduces friction immediately.

**Independent Test**: Launch the app to the Student List, tap the new reports icon, and assert the Reports screen is shown with no bottom-tab regression elsewhere.

**Acceptance Scenarios**:

1. **Given** the Student List is visible, **When** the tutor taps the new reports icon in the top app bar, **Then** the nav controller pushes the existing `KoraDestination.Reports` route without altering student context.
2. **Given** any screen that renders the bottom navigation, **When** the bottom bar is displayed, **Then** it shows exactly Dashboard, Calendar, and Homework tabs in their original order.
3. **Given** the tutor returns from the Reports screen, **When** they navigate back, **Then** the Student List remains selected with the reports icon still available.

### Edge Cases

- What happens when the tutor has zero students? ➝ Student List still renders, so the reports icon must remain visible and functional.
- How does the system handle navigation if the Student List is not the current destination? ➝ Reports icon appears only when Student List is active; ensure no crash when icon action is invoked from other screens.
- What if Reports is already on the back stack? ➝ Navigating via the icon should use `launchSingleTop` or equivalent to avoid duplicate stack entries.

## Requirements *(mandatory)*

**Constitution Guardrails**:
- Map each requirement to layers: UI (Compose TopAppBar), ViewModel (none), UseCase/Repository (no change), Navigation (route wiring via `KoraDestination` and `NavController` extensions), DataSource (no change).
- Compose Material 3: leverage existing `TopBarConfig` and Material Icons; add new string key for contentDescription in `strings.xml` with localized copies.
- Coroutines/Flow: no new async flows introduced; ensure navigation triggers remain on UI scope without blocking.
- Room/KSP: no persistence change.
- Hilt bindings: no new modules; ensure dependencies removed from bottom bar do not touch DI.
- Navigation Compose: update `KoraDestination.bottomBarDestinations`, bottom bar rendering, and Student List actions to trigger `navController.navigateToReports()`.

### Functional Requirements

- **FR-001 (UI → Navigation)**: Remove `KoraDestination.Reports` from `KoraDestination.bottomBarDestinations` and any bottom bar composables so only Dashboard, Calendar, Homework render.
- **FR-002 (Navigation)**: Delete bottom bar click handling for Reports, ensuring no orphaned logic remains in `KoraNavGraph` or preload ViewModels.
- **FR-003 (UI)**: Add a `TopBarAction` on Student List that surfaces a Material 3 statistics icon aligned with the settings action.
- **FR-004 (Navigation)**: Wire the new action to invoke the existing `navigateToReports()` helper (or equivalent) with `launchSingleTop` semantics to reuse the Reports screen.
- **FR-005 (Localization)**: Introduce `@string/student_list_reports_action_description` (or similar) across base and translated `strings.xml` files for the icon content description.
- **FR-006 (Testing)**: Update Compose UI tests (if present) to assert bottom bar count equals three and clicking the reports icon navigates to the Reports route.

*Example of marking unclear requirements:*

- **FR-007**: System MUST handle reports back stack via [NEEDS CLARIFICATION: confirm whether Reports should clear student-scoped state or keep prior context].

### Key Entities *(include if feature involves data)*

- **KoraDestination**: Defines navigation routes; update `bottomBarDestinations` to exclude `Reports` while keeping the route available for direct navigation.
- **KoraNavGraph**: Scaffold host that renders top/bottom bars; adjusts bottom bar items and configures the new top bar action.
- **TopBarConfig / TopBarAction**: Manages actions for Student List top app bar; extend to include the reports icon.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Bottom navigation renders exactly three tabs across all supported destinations (verified via UI test snapshot).
- **SC-002**: Tapping the reports icon on Student List navigates to the Reports screen within 1 tap every time (manual test run).
- **SC-003**: No additional recompositions or navigation crashes are detected in logcat during interaction (instrumented test run).
- **SC-004**: Localization audit shows new content description string translated for all supported locales before merge.
