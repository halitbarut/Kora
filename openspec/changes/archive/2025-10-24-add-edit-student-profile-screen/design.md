## Overview
Replace the legacy modal editor with a dedicated Student Profile screen that exposes all editable student metadata. The goal is to centralize profile management, unlock richer data capture (parent contact + notes), and support an optional custom hourly rate that falls back to the global default when omitted.

## Data Model Changes
- Extend `Student` in the domain layer with `parentName: String?`, `parentContact: String?`, `notes: String?`, and `customHourlyRate: Double?`.
- Update `StudentEntity` with matching nullable columns. Add a Room migration that introduces the new columns with `NULL` defaults and copies the existing `hourlyRate` values into `customHourlyRate` so historical debt calculations remain unchanged until users clear the field.
- Rename repository APIs that currently accept individual strings/doubles to accept a richer `StudentProfileUpdate` value object (or update signatures) to avoid parameter drift as more fields are added.
- Adjust derived debt calculations to rely on `customHourlyRate ?: defaultHourlyRate` when computing totals so clearing the field immediately uses the global setting.

## Navigation & UI Flow
- Add a new navigation destination `KoraDestination.EditStudentProfile` that accepts a `studentId` argument.
- Student list pencil icon navigates to the new destination instead of showing a dialog; the floating action button keeps launching the add-student dialog.
- Dashboard gets an explicit edit affordance that navigates to the same route with its bound `studentId`.
- The screen itself will render Material 3 text fields (single-line for name/contact, outlined multi-line for notes) plus a numeric-only hourly rate field. Back navigation should discard unsaved edits; Save validates the required name field and forwards the payload to the ViewModel.
- ViewModel loads the existing student, merges edits, and delegates to the repository. On success, it pops the back stack and surfaces a success toast/snackbar via the existing notifier infrastructure.

## Open Questions
None.
