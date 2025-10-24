## ADDED Requirements
### Requirement: Add Student Screen
Tutors MUST add new students through a full-screen Student Profile form that mirrors the edit experience instead of a modal dialog.

#### Scenario: Access from student list FAB
- **GIVEN** the Student List is visible
- **WHEN** the tutor taps the floating “add student” action
- **THEN** the app navigates to the full-screen Add Student screen (not a dialog) with all profile fields visible

#### Scenario: Blank state with field parity
- **GIVEN** the Add Student screen just opened
- **THEN** every field present on the Student Profile editing screen (full name, parent name/contact, custom rate, notes) appears with the same required/optional indicators, and all inputs are initially empty

#### Scenario: Saving creates student and returns
- **GIVEN** the tutor enters a valid full name (other fields optional) on the Add Student screen
- **WHEN** they tap Save
- **THEN** the student is created with the entered details (respecting the default hourly rate when custom rate is blank) and the app returns to the Student List showing the new entry

#### Scenario: Cancel discards draft
- **GIVEN** the tutor has entered details on the Add Student screen
- **WHEN** they use system back or the top-bar navigation icon without saving
- **THEN** no new student is added and the app returns to the Student List
