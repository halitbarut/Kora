## Why
Tutors can currently edit only a student's name and hourly rate through a small dialog. This prevents Kora from storing key contact information, requires duplicate tracking outside the app, and makes it awkward to manage students who rely on the account holder's contact details or who use the global default hourly rate.

## What Changes
- Introduce a full-screen Student Profile editor that surfaces all student-facing metadata (full name, parent/guardian details, contact info, optional custom hourly rate, and notes) in one place.
- Replace the legacy edit dialog by routing both the student list pencil icon and the dashboard "Edit" affordance to the new screen.
- Persist the new profile fields across the data layer so other surfaces (dashboards, upcoming features) can consume them, and treat an empty hourly rate as "use the global default".

## Impact
- Architecture: Requires extending the `Student` domain model, Room entity, DAO, repository APIs, and migrations to support the new fields and nullable custom hourly rate.
- UI: Adds a new Compose screen + ViewModel, new navigation destination, and removes the existing dialog. Student list and dashboard flows must update their navigation triggers.
- QA: Needs unit tests for profile persistence/default-rate fallback plus Compose UI tests covering validation and save flows. Migration tests ensure current installations keep existing data intact.
