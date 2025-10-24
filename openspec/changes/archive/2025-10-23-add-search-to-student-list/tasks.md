## 1. Implementation
- [x] 1.1 Extend `StudentListViewModel` with search query state and derived filtered students list.
- [x] 1.2 Add a localized search field to `StudentListScreen` that updates the query and shows clear/no-results affordances.
- [x] 1.3 Adjust empty and loading visuals so the generic empty state and "no matches" state behave correctly.

## 2. Testing
- [x] 2.1 Cover view-model filtering logic with unit tests (case-insensitive match, empty query, no results).
- [x] 2.2 Add a Compose UI test that types into the search field and verifies filtered list behavior.

## 3. Validation
- [x] 3.1 Run `./gradlew testDebugUnitTest` (and the new UI test via `connectedAndroidTest` if runnable) before hand-off. *(Unit suite fails on existing `HomeworkViewModelTest` due to `UncompletedCoroutinesError`; new tests pass.)*
