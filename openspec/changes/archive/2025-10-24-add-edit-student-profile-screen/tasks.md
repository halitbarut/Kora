## 1. Implementation
- [x] 1.1 Expand the student data model (domain + Room) with parent name, parent contact, notes, and nullable custom hourly rate, including a migration and mapper updates.
- [x] 1.2 Update repositories/use cases so student debt calculations use `customHourlyRate ?: defaultHourlyRate`, and expose a cohesive profile update API.
- [x] 1.3 Create `EditStudentProfileViewModel` + Compose screen with form validation, string resources, and persistence wiring.
- [x] 1.4 Wire navigation: route student list pencil icon and dashboard edit affordance to the new screen, remove the legacy edit dialog, and ensure back navigation pops correctly.

## 2. Testing & Validation
- [x] 2.1 Add unit tests covering repository/profile update logic, including clearing the custom hourly rate to fall back to the default.
- [x] 2.2 Add a Room migration test verifying existing students retain data and support the new nullable columns.
- [x] 2.3 Add a Compose UI test (or robolectric) that exercises required-name validation and successful save.
- [x] 2.4 Run `./gradlew testDebugUnitTest` (and targeted instrumentation test if the Compose test needs it) before hand-off.
