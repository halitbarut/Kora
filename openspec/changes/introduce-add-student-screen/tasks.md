## 1. Implementation
- [ ] 1.1 Refactor the shared Student Profile composable (title/save wiring) so it can render both edit and add modes without duplicating layout logic.
- [ ] 1.2 Build an AddStudentProfileViewModel that initializes blank state, loads default hourly rate/currency, and saves new students through StudentRepository.
- [ ] 1.3 Register a navigation destination for the add-student screen, update StudentListScreen to navigate instead of showing the dialog, and remove obsolete dialog state.
- [ ] 1.4 Ensure the new screen shows success/error messaging via the notifier and pops back to the list after a successful save.

## 2. Testing & Validation
- [ ] 2.1 Add unit coverage for the new ViewModel (validation, default-rate wiring, save success/failure).
- [ ] 2.2 Add or update Compose UI tests to cover the Add Student flow (required-name guard, smart save button, successful save path).
- [ ] 2.3 Run `./gradlew testDebugUnitTest` before hand-off.
