## Why
The Reports screen is currently exposed through a fourth bottom navigation tab, adding friction to the main experience and crowding the primary actions tutors use most often (Dashboard, Calendar, Homework). We need to restore the streamlined three-tab layout while keeping Reports discoverable from the Student List home surface.

## What Changes
- Remove the Reports destination from the bottom navigation bar so it returns to the original three tabs.
- Add a bar-chart style icon button to the Student List top app bar that opens the Reports screen.
- Reuse the existing Reports navigation route so the screen and its ViewModel remain unchanged.

## Impact
- Navigation graph updates for the root shell that hosts the bottom navigation.
- Student List UI updates to surface the new top app bar icon.
- Regenerate or update related UI tests that assert bottom navigation destinations.
