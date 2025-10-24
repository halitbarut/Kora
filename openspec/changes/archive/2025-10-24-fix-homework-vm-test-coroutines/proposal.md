## Why
HomeworkViewModelTest failed with `UncompletedCoroutinesError` because the ViewModel never observed some flows, leaving coroutines running and the test hanging while waiting for updated student data. The underlying issue was that `HomeworkViewModel` captured a single `studentId` at construction time, so tests (and the UI) could not react when the saved state changed.

## What Changes
- Drive `HomeworkViewModel`'s student and homework streams from a `SavedStateHandle` `StateFlow` so switching the student ID re-wires the flows automatically.
- Lazily start the AI insight observer only after a locale request and make its job idempotent to avoid unnecessary work in tests.
- Cancel and join the ViewModel scope in `HomeworkViewModelTest` to clean up background jobs, with the test asserting through hot flow collectors.
- Keep the unit test focused on state replacement when the student changes.

## Impact
- Homework data now updates correctly when the saved student ID changes, eliminating the coroutine leak that blocked the test.
- The test suite passes without timeouts, and the production screen benefits from responsive student switching.
