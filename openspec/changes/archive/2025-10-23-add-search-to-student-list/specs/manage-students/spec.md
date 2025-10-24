## ADDED Requirements
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
