# manage-homework Specification

## Purpose
TBD - created by archiving change fix-homework-vm-test-coroutines. Update Purpose after archive.
## Requirements
### Requirement: Homework list reacts to student change
Homework screens MUST refresh their data when the active student reference changes.

#### Scenario: Switching student replaces homework data
- **GIVEN** homework for student A is visible
- **WHEN** the ViewModel receives a different `studentId`
- **THEN** the previously shown homework is cleared and the list shows only student B's homework entries

