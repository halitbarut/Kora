## Tasks
- [x] Drive `HomeworkViewModel` student/homework streams from a reactive `SavedStateHandle` so the active student can change at runtime.
- [x] Update `HomeworkViewModelTest` to await the new state and clean up the ViewModel scope once assertions complete.
- [x] Run `./gradlew :app:testDebugUnitTest --tests "com.barutdev.kora.ui.screens.homework.HomeworkViewModelTest"` to confirm the coroutine leak is gone.
