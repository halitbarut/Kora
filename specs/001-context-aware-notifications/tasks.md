# Tasks: Context-Aware Notification Enhancement

**Input**: Design documents from `/specs/001-context-aware-notifications/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/

**Tests**: Manual testing only - no automated test tasks included as not explicitly requested in specification.

**Organization**: Tasks grouped by user story for independent implementation and testing.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Task can run in parallel (no shared files).
- **[Story]**: Maps to user story (e.g., US1, US2, US3).
- Include exact file paths (`app/src/main/java/com/barutdev/kora/...`) for traceability.

## Path Conventions

- Kotlin sources: `app/src/main/java/com/barutdev/kora/<layer>/<feature>/...`
- Domain models: `app/src/main/java/com/barutdev/kora/domain/model/...`
- UseCases: `app/src/main/java/com/barutdev/kora/domain/usecase/notification/...`
- Repositories: `app/src/main/java/com/barutdev/kora/domain/repository/...`
- Data layer: `app/src/main/java/com/barutdev/kora/data/...`
- DI modules: `app/src/main/java/com/barutdev/kora/di/...`
- Resources: `app/src/main/res/values*/strings.xml`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Prepare Android manifest, permissions, and dependencies for notification system.

- [x] T001 Add required permissions to `app/src/main/AndroidManifest.xml` (POST_NOTIFICATIONS, USE_EXACT_ALARM, RECEIVE_BOOT_COMPLETED)
- [x] T002 Verify DataStore Preferences dependency in `gradle/libs.versions.toml` or `build.gradle.kts`
- [x] T003 [P] Add notification string placeholders to `app/src/main/res/values/strings.xml` (8 new strings for channels and notifications)

**Checkpoint**: ✅ Permissions declared, dependencies verified, string keys reserved.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Build domain models, repositories, and core infrastructure required by all user stories.

**⚠️ CRITICAL**: All user stories are blocked until this phase is complete.

### Domain Models

- [x] T004 [P] Create `NotificationType` enum in `app/src/main/java/com/barutdev/kora/domain/model/NotificationType.kt`
- [x] T005 [P] Create `NotificationData` data class in `app/src/main/java/com/barutdev/kora/domain/model/NotificationData.kt`
- [x] T006 [P] Create `AlarmScheduleRequest` data class in `app/src/main/java/com/barutdev/kora/domain/model/AlarmScheduleRequest.kt`
- [x] T007 [P] Create `ReminderSettings` data class in `app/src/main/java/com/barutdev/kora/domain/model/ReminderSettings.kt`
- [x] T008 [P] Create `LessonWithStudent` data class in `app/src/main/java/com/barutdev/kora/domain/model/LessonWithStudent.kt`

### Repository Interfaces

- [x] T009 [P] Create `SettingsRepository` interface in `app/src/main/java/com/barutdev/kora/domain/repository/SettingsRepository.kt`
- [x] T010 [P] Create `AlarmScheduler` interface in `app/src/main/java/com/barutdev/kora/domain/repository/AlarmScheduler.kt`
- [x] T011 [P] Create `NotificationBuilder` interface in `app/src/main/java/com/barutdev/kora/domain/repository/NotificationBuilder.kt`
- [x] T012 Extend `LessonRepository` interface with 4 new methods (`getLessonsForDate`, `getCompletedLessonsForDate`, `getLessonWithStudent`, `getActiveLessons`) in existing repository file

### Repository Implementations

- [x] T013 [P] Implement `SettingsRepositoryImpl` with DataStore Preferences in `app/src/main/java/com/barutdev/kora/data/repository/SettingsRepositoryImpl.kt`
- [x] T014 [P] Create DataStore singleton provider in `app/src/main/java/com/barutdev/kora/data/datasource/local/datastore/SettingsDataStore.kt`
- [x] T015 [P] Implement `AlarmSchedulerImpl` with AlarmManager in `app/src/main/java/com/barutdev/kora/data/notification/AlarmSchedulerImpl.kt`
- [x] T016 [P] Implement `NotificationBuilderImpl` with NotificationCompat in `app/src/main/java/com/barutdev/kora/data/notification/NotificationBuilderImpl.kt`
- [x] T017 Implement 4 new methods in `LessonRepositoryImpl` (in existing repository implementation file)

### UseCase Layer

- [x] T018 [P] Create `GetTodayLessonsUseCase` in `app/src/main/java/com/barutdev/kora/domain/usecase/notification/GetTodayLessonsUseCase.kt`
- [x] T019 [P] Create `GetCompletedLessonsForDateUseCase` in `app/src/main/java/com/barutdev/kora/domain/usecase/notification/GetCompletedLessonsForDateUseCase.kt`
- [x] T020 [P] Create `GetLessonWithStudentUseCase` in `app/src/main/java/com/barutdev/kora/domain/usecase/notification/GetLessonWithStudentUseCase.kt`
- [x] T021 [P] Create `GetReminderSettingsUseCase` in `app/src/main/java/com/barutdev/kora/domain/usecase/notification/GetReminderSettingsUseCase.kt`
- [x] T022 Create `ScheduleNotificationAlarmsUseCase` in `app/src/main/java/com/barutdev/kora/domain/usecase/notification/ScheduleNotificationAlarmsUseCase.kt` (depends on T020, T021)
- [x] T023 [P] Create `CancelNotificationAlarmsUseCase` in `app/src/main/java/com/barutdev/kora/domain/usecase/notification/CancelNotificationAlarmsUseCase.kt`
- [x] T024 Create `RescheduleAllNotificationAlarmsUseCase` in `app/src/main/java/com/barutdev/kora/domain/usecase/notification/RescheduleAllNotificationAlarmsUseCase.kt` (depends on T022, T023)

### BroadcastReceivers

- [x] T025 Implement `NotificationAlarmReceiver` with Hilt injection in `app/src/main/java/com/barutdev/kora/data/notification/NotificationAlarmReceiver.kt` (depends on T016, T020)
- [x] T026 [P] Implement `BootCompletedReceiver` in `app/src/main/java/com/barutdev/kora/data/notification/BootCompletedReceiver.kt` (depends on T024)
- [x] T027 Register both BroadcastReceivers in `app/src/main/AndroidManifest.xml`

### Dependency Injection

- [x] T028 Create `NotificationModule` in `app/src/main/java/com/barutdev/kora/di/NotificationModule.kt` (binds all repositories and provides DataStore)
- [x] T029 Create `NotificationDataModule` in same file as T028 (provides DataStore singleton)

**Checkpoint**: All repositories, UseCases, and infrastructure components implemented and wired via Hilt.

---

## Phase 3: User Story 1 - Receive Clear Lesson Reminder (Priority: P1) 🎯 MVP

**Goal**: Tutors receive clear morning notifications for upcoming lessons with student name and time, navigating to student calendar on tap.

**Independent Test**: Schedule a lesson for today, set "Lesson reminder time" to current time + 2 minutes, wait for notification, verify title "Upcoming Lesson: [Student Name]" and content with lesson time, tap notification to navigate to student calendar screen.

### Implementation for User Story 1

- [x] T030 [US1] Add deep link declaration for student calendar screen in Navigation graph (`app://kora/studentCalendar/{studentId}`)
- [x] T031 [US1] Implement lesson reminder notification channel creation in `NotificationBuilderImpl` (CHANNEL_LESSON_REMINDERS)
- [x] T032 [US1] Implement `buildLessonReminder()` method in `NotificationBuilderImpl` with correct title, content, and PendingIntent
- [x] T033 [US1] Add lesson reminder alarm scheduling logic in `ScheduleNotificationAlarmsUseCase` (morning alarm with lessonReminderTime)
- [x] T034 [US1] Implement lesson reminder handling in `NotificationAlarmReceiver.onReceive()` for LESSON_REMINDER type
- [x] T035 [US1] Add localized strings for lesson reminder title and content to `app/src/main/res/values/strings.xml`
- [x] T036 [US1] Add localized channel name and description for CHANNEL_LESSON_REMINDERS to `app/src/main/res/values/strings.xml`

**Checkpoint**: User Story 1 is manually testable - lesson reminder notifications display with correct content and navigate to student calendar.

---

## Phase 4: User Story 2 - Receive Smart Post-Lesson Note Reminder (Priority: P2)

**Goal**: Tutors receive evening notifications only for completed lessons, prompting to log notes, navigating to lesson edit screen on tap.

**Independent Test**: Complete a lesson today, set "Log reminder time" to current time + 2 minutes, wait for notification, verify title "Log Lesson Notes: [Student Name]" appears. Test with pending/cancelled lessons to verify no notification sent.

### Implementation for User Story 2

- [ ] T037 [US2] Add deep link declaration for lesson edit screen in Navigation graph (`app://kora/lessonEdit/{lessonId}`)
- [ ] T038 [US2] Implement note reminder notification channel creation in `NotificationBuilderImpl` (CHANNEL_LESSON_NOTES)
- [ ] T039 [US2] Implement `buildNoteReminder()` method in `NotificationBuilderImpl` with correct title, content, and PendingIntent
- [ ] T040 [US2] Add note reminder alarm scheduling logic in `ScheduleNotificationAlarmsUseCase` (evening alarm with logReminderTime)
- [ ] T041 [US2] Implement smart behavior in `NotificationAlarmReceiver.onReceive()` for NOTE_REMINDER type (check lesson status == COMPLETED before showing)
- [ ] T042 [US2] Add localized strings for note reminder title and content to `app/src/main/res/values/strings.xml`
- [ ] T043 [US2] Add localized channel name and description for CHANNEL_LESSON_NOTES to `app/src/main/res/values/strings.xml`

**Checkpoint**: User Story 2 is manually testable - note reminders only show for completed lessons and navigate to lesson edit screen.

---

## Phase 5: User Story 3 - Distinguish Notification Types at a Glance (Priority: P3)

**Goal**: Notification titles and content clearly differentiate lesson reminders from note reminders without opening notifications.

**Independent Test**: Generate both notification types and verify titles clearly distinguish them ("Upcoming Lesson: [Name]" vs "Log Lesson Notes: [Name]").

### Implementation for User Story 3

- [ ] T044 [US3] Verify notification titles use distinct verbs and context per spec in `NotificationBuilderImpl` (already implemented in US1 & US2, this is validation)
- [ ] T045 [US3] Verify notification icons are distinct (optional enhancement: use different icons for lesson vs note reminders in `NotificationBuilderImpl`)
- [ ] T046 [US3] Review and refine notification content clarity in localized strings for both types

**Checkpoint**: User Story 3 is manually testable - both notification types are immediately distinguishable by title and content.

---

## Phase 6: Integration & Alarm Rescheduling

**Purpose**: Integrate alarm rescheduling when lessons or settings change.

- [ ] T047 Call `ScheduleNotificationAlarmsUseCase` after lesson create/update in `LessonRepositoryImpl` (hook into existing lesson CRUD operations)
- [ ] T048 Call `CancelNotificationAlarmsUseCase` after lesson delete in `LessonRepositoryImpl`
- [ ] T049 Implement settings change observer in Application class or WorkManager to call `RescheduleAllNotificationAlarmsUseCase` when reminder times change
- [ ] T050 Implement alarm rescheduling in `BootCompletedReceiver.onReceive()` to restore alarms after device reboot

**Checkpoint**: Alarms automatically reschedule when lessons are modified, settings change, or device reboots.

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Hardening, validation, and final preparations for release.

- [x] T051 [P] Add notification permission request flow if POST_NOTIFICATIONS not granted (Android 13+)
- [ ] T052 [P] Audit all 8 notification strings in `strings.xml` for correctness and localization quality
- [ ] T053 [P] Add logging/error handling for alarm scheduling failures in `AlarmSchedulerImpl`
- [ ] T054 [P] Add logging for notification display events in `NotificationAlarmReceiver`
- [ ] T055 Verify NotificationBuilderImpl creates channels on initialization (call from Application class or first notification)
- [X] T056 Test deep linking with app in terminated state (verify PendingIntent flags correct)
- [ ] T057 Manual testing: Multiple lessons on same day (verify separate notifications)
- [ ] T058 Manual testing: Lesson status changes (PENDING → COMPLETED → notification sent, PENDING → CANCELLED → no notification)
- [ ] T059 Manual testing: Settings change triggers alarm rescheduling
- [ ] T060 Manual testing: Device reboot restores alarms
- [ ] T061 Manual testing: Doze mode compatibility (test `setExactAndAllowWhileIdle()` delivery)
- [ ] T062 Run `./gradlew assembleDebug` and verify build succeeds
- [ ] T063 Run `./gradlew app:lintDebug` and fix any warnings related to notification code
- [ ] T064 Update CLAUDE.md if any new patterns/conventions established (optional)

**Checkpoint**: Feature is production-ready with proper error handling, logging, and edge cases covered.

---

## Dependencies & Execution Order

### Phase Dependencies

```
Phase 1 (Setup)
    ↓
Phase 2 (Foundational) ← BLOCKS all user stories
    ↓
Phase 3 (US1) ← MVP baseline
    ↓
Phase 4 (US2) ← Builds on notification infrastructure
    ↓
Phase 5 (US3) ← Validation/polish of existing notifications
    ↓
Phase 6 (Integration) ← Requires all UseCases complete
    ↓
Phase 7 (Polish) ← Final hardening
```

### User Story Dependencies

- **User Story 1 (P1)**: No dependencies on other user stories (baseline feature)
- **User Story 2 (P2)**: Reuses notification infrastructure from US1 but independently testable
- **User Story 3 (P3)**: Validates distinction between US1 and US2 notifications

### Within Each Phase

- **Domain models** (T004-T008) can be created in parallel
- **Repository interfaces** (T009-T012) can be created in parallel after models
- **Repository implementations** (T013-T017) can be created in parallel after interfaces
- **UseCases** (T018-T024) have internal dependencies:
  - T022 depends on T020, T021
  - T024 depends on T022, T023
  - Others can be parallel
- **BroadcastReceivers** (T025-T027):
  - T025 depends on T016, T020
  - T026 depends on T024
- **User Story tasks** within each phase can mostly be done sequentially as they touch same files

### Parallel Opportunities

Tasks marked `[P]` can run simultaneously:
- Phase 1: T003 can run parallel to T001-T002
- Phase 2: T004-T008 (all domain models), T009-T011 (repository interfaces), T013-T016 (repository implementations), T018-T021 (some UseCases)
- Phase 7: T051-T054 (polish tasks)

---

## Parallel Example: Phase 2 Foundational

```bash
# Create all domain models in parallel (T004-T008)
Task: "Create NotificationType enum"
Task: "Create NotificationData data class"
Task: "Create AlarmScheduleRequest data class"
Task: "Create ReminderSettings data class"
Task: "Create LessonWithStudent data class"

# Then create repository interfaces in parallel (T009-T011)
Task: "Create SettingsRepository interface"
Task: "Create AlarmScheduler interface"
Task: "Create NotificationBuilder interface"

# Then implement repositories in parallel (T013-T016)
Task: "Implement SettingsRepositoryImpl"
Task: "Create DataStore singleton"
Task: "Implement AlarmSchedulerImpl"
Task: "Implement NotificationBuilderImpl"
```

---

## Execution Strategy

### MVP Scope (Minimum Viable Product)

- **Phases 1-3**: Setup, Foundational, User Story 1
- **Estimated Tasks**: T001-T036 (36 tasks)
- **Delivers**: Basic lesson reminder notifications with student calendar navigation
- **Manual Test**: Can schedule lesson and receive/tap notification

### Full Feature Scope

- **All Phases**: 1-7
- **Total Tasks**: 64 tasks
- **Delivers**: Complete notification system with lesson reminders, smart note reminders, alarm rescheduling, and production hardening

### Recommended Approach

1. **Sprint 1**: Phases 1-3 (MVP) - Deliver basic lesson reminders
2. **Sprint 2**: Phases 4-5 - Add smart note reminders and validation
3. **Sprint 3**: Phases 6-7 - Integration, rescheduling, polish

---

## Task Count Summary

- **Phase 1 (Setup)**: 3 tasks
- **Phase 2 (Foundational)**: 26 tasks (T004-T029)
- **Phase 3 (US1)**: 7 tasks (T030-T036)
- **Phase 4 (US2)**: 7 tasks (T037-T043)
- **Phase 5 (US3)**: 3 tasks (T044-T046)
- **Phase 6 (Integration)**: 4 tasks (T047-T050)
- **Phase 7 (Polish)**: 14 tasks (T051-T064)

**Total**: 64 tasks

**Parallel Opportunities**: 18 tasks marked `[P]`

**User Story Distribution**:
- US1: 7 tasks
- US2: 7 tasks
- US3: 3 tasks
- Foundation/Infrastructure: 47 tasks

---

## Independent Test Criteria

### User Story 1 (P1)
✅ Schedule a lesson for today
✅ Set "Lesson reminder time" to 2 minutes from now
✅ Wait for notification
✅ Verify title: "Upcoming Lesson: [Student Name]"
✅ Verify content includes lesson time
✅ Tap notification → navigates to student calendar
✅ Test with multiple lessons → each gets separate notification

### User Story 2 (P2)
✅ Complete a lesson today
✅ Set "Log reminder time" to 2 minutes from now
✅ Wait for notification
✅ Verify title: "Log Lesson Notes: [Student Name]"
✅ Verify content includes lesson time
✅ Tap notification → navigates to lesson edit screen
✅ Test with pending lesson → no notification sent
✅ Test with cancelled lesson → no notification sent
✅ Test with mix of completed/cancelled → only completed get notifications

### User Story 3 (P3)
✅ Generate both notification types
✅ Verify titles clearly distinguish them
✅ Verify content immediately indicates purpose
✅ No need to open notification to understand type

---

## Format Validation

✅ All tasks follow format: `- [ ] [ID] [P?] [Story?] Description`
✅ All tasks have sequential IDs (T001-T064)
✅ All user story tasks have [US1], [US2], or [US3] labels
✅ All parallel tasks have [P] marker
✅ All tasks include specific file paths
✅ No setup/foundational/polish tasks have story labels

---
