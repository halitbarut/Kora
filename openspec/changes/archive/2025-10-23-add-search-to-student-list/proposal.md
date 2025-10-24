## Why
Tutors with large student rosters struggle to locate individuals quickly on the Student List. Scrolling through long lists slows down lesson prep and payment checks.

## What Changes
- Add client-side search tooling to the Student List screen so tutors can filter students by name without leaving the screen.
- Persist the active query while the screen is in memory and clear it when the user exits, keeping the workflow focused but predictable.
- Ensure empty/search states remain localized and aligned with Material 3 design patterns.

## Impact
- UX: Faster navigation to student detail cards; reduced friction for high-volume tutors.
- Architecture: Extends the existing StudentListViewModel with query state and filtering; reuses repositories without new data sources.
- QA: Requires new UI/VM tests to cover filtering logic and empty-state transitions.
