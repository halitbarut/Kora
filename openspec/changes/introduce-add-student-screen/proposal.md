## Why
The student list still relies on a cramped modal dialog to capture new students. This diverges from the recently introduced full-screen Student Profile editor, limits which profile fields can be entered, and creates an inconsistent experience between creating and editing students.

## What Changes
- Introduce a dedicated “Add New Student” screen that reuses the Student Profile layout, validation, and save mechanics while starting from a blank state.
- Wire the student list FAB to navigate to the new screen and retire the legacy add-student dialog logic from the list ViewModel.
- Create the supporting ViewModel/data flow so saving a new profile persists all fields and returns tutors to the previous screen with the same smart save behaviour as editing.

## Impact
- Navigation: add a new destination for creating students and update the Kora nav graph + StudentListScreen wiring.
- UI/ViewModel: refactor the shared Student Profile composable to support both add and edit modes, introduce a creation-focused ViewModel, and remove dialog state.
- Data: leverage the existing StudentRepository APIs so the creation flow persists full profiles, ensuring default-rate fallbacks still work when no custom rate is provided.
