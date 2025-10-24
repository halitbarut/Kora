# manage-students Specification

## Purpose
TBD - created by archiving change add-search-to-student-list. Update Purpose after archive.
## Requirements
### Requirement: Student List Search
Tutors MUST be able to narrow the Student List by typing a name-based query without leaving the screen.

#### Scenario: Matching students are filtered
- **GIVEN** the Student List contains "Alice Johnson" and "Bob Stone"
- **WHEN** the tutor enters `al` into the search field
- **THEN** only students whose names contain `al` (case-insensitive) remain visible and the rest are hidden

#### Scenario: No results message
- **GIVEN** the Student List is visible
- **WHEN** the tutor enters a query that matches no students
- **THEN** the list area shows a localized "no matches" state instead of the generic empty state

#### Scenario: Clearing search restores full list
- **GIVEN** a query is active and the filtered list is shown
- **WHEN** the tutor clears the search field or leaves it blank
- **THEN** the full, unfiltered Student List becomes visible again

### Requirement: Student Profile Editing Screen
Tutors MUST edit student details in a dedicated screen that replaces the legacy dialog and centralizes all profile fields.

#### Scenario: Access from student list
- **GIVEN** the Student List is visible
- **WHEN** the tutor taps the edit (pencil) icon on a student card
- **THEN** the app navigates to the Student Profile editing screen pre-populated with that student's saved details

#### Scenario: Access from student dashboard
- **GIVEN** the tutor has opened a student's dashboard
- **WHEN** the tutor activates the dashboard's edit affordance
- **THEN** the same Student Profile editing screen opens for that student

#### Scenario: Saving returns to prior flow
- **GIVEN** the tutor is editing a student profile and enters valid data
- **WHEN** they tap Save
- **THEN** the profile updates persist, dependent screens (dashboard, list) reflect the changes, and the app returns to the previous screen

#### Scenario: Back navigation discards changes
- **GIVEN** the tutor is editing a profile with unsaved changes
- **WHEN** they use system back or the top-bar navigation icon without saving
- **THEN** no changes persist and the app returns to the previous screen

### Requirement: Student Profile Field Handling
The Student Profile screen MUST enforce required fields, treat optional entries correctly, and respect the global default hourly rate.

#### Scenario: Full name is required
- **GIVEN** the Student Profile screen is open
- **WHEN** the tutor clears the full name field and attempts to save
- **THEN** the save action is blocked (disabled or error surfaced) until a non-blank full name is provided

#### Scenario: Optional fields persist when provided
- **GIVEN** the tutor enters a parent name, parent contact info, hourly rate, and notes
- **WHEN** they save the profile
- **THEN** the entered values persist and reappear when the profile is reopened

#### Scenario: Clearing hourly rate uses global default
- **GIVEN** a student previously had a custom hourly rate
- **WHEN** the tutor clears the hourly rate field and saves
- **THEN** the stored custom rate becomes null and future calculations use the app's default hourly rate for that student

