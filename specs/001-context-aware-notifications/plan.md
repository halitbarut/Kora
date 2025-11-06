# Implementation Plan: Context-Aware Notification Enhancement

**Branch**: `001-context-aware-notifications` | **Date**: 2025-11-02 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/001-context-aware-notifications/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/scripts/bash/setup-plan.sh` for the execution workflow.

## Summary

This feature enhances the existing notification system to provide context-aware, distinct notifications for lesson reminders and post-lesson note prompts. The technical approach uses Android's AlarmManager to schedule exact-timed notifications based on user-configured times (Settings -> Lesson reminder time and Settings -> Log reminder time). A BroadcastReceiver will handle alarm triggers, query lesson data to build appropriate notifications, and use NotificationManager to display them. The receiver will differentiate notification types via Intent extras and implement smart behavior for note reminders by checking lesson completion status before showing notifications. Deep linking via PendingIntents will navigate users to the correct screens (student calendar or lesson edit screen) when notifications are tapped.

## Technical Context

**Language/Version**: Kotlin (K2) targeting JVM 17
**Primary Dependencies**: Jetpack Compose Material 3, Hilt, Kotlin Coroutines & Flow, Room (KSP), Navigation Compose, Android AlarmManager, Android NotificationManager
**Storage**: Room database with KSP processors (no KAPT) - accessing existing Lesson and Student entities
**Testing**: JUnit, kotlinx-coroutines-test, Compose UI tests, Room in-memory tests, BroadcastReceiver instrumentation tests
**Target Platform**: Android (per app `minSdk`) with Compose-only UI layer
**Project Type**: Android mobile app using Clean Architecture (UI → ViewModel → UseCase → Repository → DataSource)
**Performance Goals**: 60fps UI, <150ms state propagation, resilient offline persistence, exact-time notification delivery
**Constraints**: No XML layouts, no alternative DI/navigation stacks, immutable data models, strings in `strings.xml`, BroadcastReceiver must support dependency injection
**Scale/Scope**: Notification scheduling system within `com.barutdev.kora` integrating with existing lesson management workflows

**Feature-Specific Technical Decisions**:
- **Notification Scheduling**: AlarmManager with `setExactAndAllowWhileIdle()` for precise delivery even in Doze mode
- **Receiver Architecture**: BroadcastReceiver with Hilt injection support using `HiltBroadcastReceiver` and `@AndroidEntryPoint`
- **State Check**: Repository access within BroadcastReceiver via injected UseCase to verify lesson completion status
- **Deep Linking**: Navigation Compose deep links via PendingIntent with specific route parameters (studentId, lessonId)
- **Notification Differentiation**: Intent extras carry notification type enum and associated data (lessonId, studentId)
- **Settings Integration**: Access existing user preferences for reminder times via [NEEDS CLARIFICATION: Settings repository/preferences manager name?]
- **Alarm Rescheduling**: [NEEDS CLARIFICATION: How should alarms be rescheduled when lessons are created/modified/deleted or when settings change?]
- **Notification Channel**: [NEEDS CLARIFICATION: Should we create separate notification channels for lesson reminders vs note reminders, or use existing channel?]

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### Clean MVVM Contract (I)
✅ **PASS** - Architecture flow mapping:
- **UI Layer**: No new UI screens for this feature (uses existing student calendar and lesson edit screens)
- **ViewModel Layer**: Not applicable - BroadcastReceiver operates outside normal UI flow but follows dependency injection
- **UseCase Layer**:
  - `GetTodayLessonsUseCase` - Fetches lessons scheduled for today
  - `GetCompletedLessonsForDateUseCase` - Fetches completed lessons for a specific date
  - `GetLessonWithStudentUseCase` - Fetches lesson details with student information for notification content
- **Repository Layer**: Access existing `LessonRepository` and `StudentRepository` via UseCases
- **DataSource Layer**: Access existing Room DAOs for Lesson and Student entities

**Note**: BroadcastReceiver is an Android platform component that operates outside the typical UI → ViewModel flow but maintains Clean Architecture by:
- Using dependency injection (Hilt) for UseCase access
- Delegating all business logic to UseCases
- Containing only notification display and alarm scheduling logic

### Compose Material 3 Experience (II)
✅ **PASS** - No new UI screens created in this feature
- Existing screens (student calendar, lesson edit) already use Compose Material 3
- No XML layouts introduced
- No design tokens or theme updates required

### Kotlin 17 Concurrency Discipline (III)
✅ **PASS** - Coroutine usage:
- BroadcastReceiver uses `goAsync()` and `CoroutineScope` for database queries and notification building
- All UseCase calls return `Flow` or suspend functions
- AlarmManager callbacks handled via BroadcastReceiver (Android platform pattern)
- No RxJava, threads, or raw callbacks used

### Room Integrity & Immutable Data (IV)
✅ **PASS** - Persistence strategy:
- No new Room entities created
- Accessing existing immutable `Lesson` and `Student` entities via repositories
- No schema migrations required
- No direct DAO access from BroadcastReceiver (goes through UseCases → Repositories)

### Platform Services Compliance (V)
✅ **PASS** - Platform integration:
- **Hilt**: BroadcastReceiver annotated with `@AndroidEntryPoint`, UseCases injected via constructor
- **Navigation Compose**: Deep links use existing navigation graph destinations with parameters
- **Strings**: New notification text strings added to `strings.xml`:
  - `notification_lesson_reminder_title` ("Upcoming Lesson: %s")
  - `notification_lesson_reminder_content` ("Your lesson is today at %s. Don't forget to prepare!")
  - `notification_note_reminder_title` ("Log Lesson Notes: %s")
  - `notification_note_reminder_content` ("Add your notes and observations for the lesson you had today at %s.")

### Summary
✅ **All Constitution principles satisfied**. This feature follows Clean Architecture by isolating notification logic in UseCases and using dependency injection throughout. The BroadcastReceiver acts as an infrastructure component similar to a ViewModel but for background operations.

## Project Structure

### Documentation (this feature)

```text
specs/[###-feature]/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
app/
├── src/main/java/com/barutdev/kora/
│   ├── data/
│   │   ├── datasource/
│   │   └── repository/
│   ├── di/
│   ├── domain/
│   │   ├── model/
│   │   └── usecase/
│   ├── navigation/
│   └── ui/
│       ├── components/
│       └── screens/
└── src/main/res/
    ├── values/strings.xml
    └── values-*/strings.xml

app/src/androidTest/...
app/src/test/...
```

**Structure Decision**:

New feature subpackages added:
- `domain/usecase/notification/` - Notification-specific UseCases (7 new UseCases)
- `data/notification/` - Notification infrastructure (BroadcastReceiver, NotificationBuilder, AlarmScheduler)
- `data/datasource/local/datastore/` - Settings storage via DataStore Preferences
- `di/NotificationModule.kt` - Hilt bindings for notification components

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

**No violations detected.** All Constitution principles satisfied.

---

## Phase 0: Research Summary

**File**: [research.md](./research.md)

Key technical decisions resolved:
1. ✅ Settings access via `SettingsRepository` with DataStore Preferences
2. ✅ Event-driven alarm rescheduling (lesson changes + settings changes)
3. ✅ Two separate notification channels (Lesson Reminders + Lesson Notes)
4. ✅ Hilt `@AndroidEntryPoint` for BroadcastReceiver DI
5. ✅ `USE_EXACT_ALARM` permission with `setExactAndAllowWhileIdle()`
6. ✅ Navigation Compose deep links with URI parameters

---

## Phase 1: Design Artifacts

### Data Model
**File**: [data-model.md](./data-model.md)

**New Domain Models**: 4
- `NotificationType` (enum)
- `NotificationData` (data class)
- `AlarmScheduleRequest` (data class)
- `ReminderSettings` (data class)

**Extended Entities**: 0 (reuses existing `Lesson` and `Student`)

**New Repositories**: 3
- `SettingsRepository` - User preferences
- `AlarmScheduler` - AlarmManager abstraction
- `NotificationBuilder` - Notification construction

**Database Changes**: None (no migrations required)

### Contracts

**File**: [contracts/UseCases.md](./contracts/UseCases.md)

**New UseCases**: 7
1. `GetTodayLessonsUseCase`
2. `GetCompletedLessonsForDateUseCase`
3. `GetLessonWithStudentUseCase`
4. `ScheduleNotificationAlarmsUseCase`
5. `CancelNotificationAlarmsUseCase`
6. `RescheduleAllNotificationAlarmsUseCase`
7. `GetReminderSettingsUseCase`

**File**: [contracts/Repositories.md](./contracts/Repositories.md)

**Repository Contracts**: 3 new + 1 extended
- `SettingsRepository` - Settings persistence
- `AlarmScheduler` - Alarm management
- `NotificationBuilder` - Notification building
- `LessonRepository` (extended with 4 new methods)

### Quickstart Guide

**File**: [quickstart.md](./quickstart.md)

Complete implementation guide including:
- Prerequisites and permissions
- Package structure setup
- Step-by-step implementation instructions
- Testing guide (unit, integration, manual)
- Debugging tips
- Common issues and solutions

---

## Post-Design Constitution Re-evaluation

### Clean MVVM Contract (I)
✅ **STILL PASSING** - All UseCases defined with clear single responsibilities. BroadcastReceiver properly uses dependency injection and delegates to UseCases. No business logic in infrastructure components.

### Compose Material 3 Experience (II)
✅ **STILL PASSING** - No UI changes in this feature. Existing screens already use Compose Material 3.

### Kotlin 17 Concurrency Discipline (III)
✅ **STILL PASSING** - All async work uses coroutines and Flow. BroadcastReceiver properly uses `goAsync()` with CoroutineScope.

### Room Integrity & Immutable Data (IV)
✅ **STILL PASSING** - All new models are immutable data classes. No schema changes. Repository pattern maintained.

### Platform Services Compliance (V)
✅ **STILL PASSING** - Hilt bindings defined in `NotificationModule`. All strings in `strings.xml`. Deep links use Navigation Compose.

**Final Verdict**: ✅ All Constitution principles remain satisfied after detailed design.

---

## Implementation Readiness Checklist

- [x] Phase 0: Research complete (all NEEDS CLARIFICATION resolved)
- [x] Phase 1: Data model designed
- [x] Phase 1: UseCase contracts defined
- [x] Phase 1: Repository contracts defined
- [x] Phase 1: Quickstart guide created
- [x] Phase 1: Agent context updated (CLAUDE.md)
- [x] Constitution Check: All principles satisfied
- [ ] Phase 2: Tasks generated (run `/speckit.tasks` to generate)
- [ ] Phase 3: Implementation (run `/speckit.implement` when ready)

---

## Next Steps

1. **Generate Tasks**: Run `/speckit.tasks` to create detailed task breakdown in `tasks.md`
2. **Review Plan**: Team review of this implementation plan
3. **Begin Implementation**: Follow `quickstart.md` and `tasks.md` for step-by-step development
4. **Testing**: Execute test plan from `quickstart.md`
5. **Code Review**: Verify Constitution compliance before merge

---

## Summary

This implementation plan provides a complete technical blueprint for enhancing the Kora notification system with context-aware, distinct notifications. The design:

- ✅ Follows Clean Architecture (UI → ViewModel → UseCase → Repository → DataSource)
- ✅ Uses Android AlarmManager for exact-time scheduling
- ✅ Implements BroadcastReceiver with Hilt dependency injection
- ✅ Differentiates notification types via Intent extras
- ✅ Implements smart behavior (checks lesson completion status)
- ✅ Uses deep linking for navigation to correct screens
- ✅ Maintains immutable data models throughout
- ✅ Requires zero database migrations
- ✅ Adds all strings to `strings.xml` for localization
- ✅ Satisfies all Constitution principles

**Estimated Complexity**: Medium
- **New Components**: 7 UseCases, 3 Repositories, 1 BroadcastReceiver, 4 domain models
- **Modified Components**: LessonRepository (4 new methods)
- **Testing Scope**: Unit tests for UseCases, integration tests for alarm scheduling, manual testing for notifications

Ready for task generation and implementation.
