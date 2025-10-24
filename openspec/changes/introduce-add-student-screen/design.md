## Overview
Add a first-class “Add New Student” experience that mirrors the Student Profile editor. The screen should render the exact same form, validation, and save affordance as editing, but start with empty fields and a different title. We will lean on shared UI state/handlers so the layout stays single-sourced.

## UI & ViewModel Structure
- Extract the existing `EditStudentProfileContent` into a mode-aware composable (`StudentProfileScreenContent`) that accepts dynamic title/cta strings and delegates form events to the caller. This keeps field rendering identical for both edit and add.
- Keep `EditStudentProfileViewModel` mostly unchanged; introduce a sibling `AddStudentProfileViewModel` that:
  - Loads default hourly rate + currency from `UserPreferencesRepository`.
  - Tracks form input with the same validation helpers (full name required, hourly rate format) by reusing shared utilities where possible.
  - Emits `ProfileSaved`/`SaveFailed` events through a shared sealed interface so the screen wrapper can display toasts/snackbars consistently.
- The add ViewModel will create a `Student` domain object and call `StudentRepository.addStudent`, storing the new record before signalling success.

## Navigation Flow
- Define a new top-level destination `KoraDestination.AddStudentProfile` (no student ID arg).
- Update `KoraNavGraph` so the student list FAB navigates to this destination. On successful save, the screen pops to the list; on cancel/back, no state is mutated.
- Remove the legacy dialog state from `StudentListViewModel` to avoid unused flows and simplify the FAB click path.

## Open Questions
None at this time.
