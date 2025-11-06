# UseCase Contracts: Context-Aware Notification Enhancement

**Feature**: 001-context-aware-notifications
**Date**: 2025-11-02
**Purpose**: Define UseCase interfaces and contracts for notification scheduling and data access

---

## UseCase Layer Contracts

### 1. GetTodayLessonsUseCase

**Purpose**: Retrieve all lessons scheduled for today

**Package**: `com.barutdev.kora.domain.usecase.notification`

**Contract**:
```kotlin
interface GetTodayLessonsUseCase {
    /**
     * Returns a Flow of lessons scheduled for the current date.
     * Emits updated list whenever lesson data changes.
     *
     * @return Flow<List<Lesson>> - Lessons scheduled for today, any status
     */
    operator fun invoke(): Flow<List<Lesson>>
}
```

**Inputs**: None (uses current date internally)

**Outputs**: `Flow<List<Lesson>>`
- Emits whenever lesson data changes in database
- List may be empty if no lessons today
- Lessons not filtered by status (includes PENDING, COMPLETED, CANCELLED)

**Error Handling**:
- Database errors propagated as exceptions in Flow
- Empty list on no data (not an error)

**Testing**:
- ✅ Returns lessons for today's date
- ✅ Excludes lessons from other dates
- ✅ Includes all statuses (PENDING, COMPLETED, CANCELLED)
- ✅ Emits updates when lessons added/modified for today
- ✅ Returns empty list when no lessons today

---

### 2. GetCompletedLessonsForDateUseCase

**Purpose**: Retrieve lessons marked as COMPLETED for a specific date

**Package**: `com.barutdev.kora.domain.usecase.notification`

**Contract**:
```kotlin
interface GetCompletedLessonsForDateUseCase {
    /**
     * Returns a Flow of completed lessons for the specified date.
     * Only includes lessons with status = COMPLETED.
     *
     * @param date The date to query for completed lessons
     * @return Flow<List<Lesson>> - Completed lessons for the date
     */
    operator fun invoke(date: LocalDate): Flow<List<Lesson>>
}
```

**Inputs**:
- `date: LocalDate` - Date to query (typically "today" for evening note reminders)

**Outputs**: `Flow<List<Lesson>>`
- Emits only lessons with `status == COMPLETED`
- Emits whenever lesson status changes
- Empty list if no completed lessons for date

**Business Rules**:
- Filter: `lesson.status == COMPLETED AND lesson.scheduledTime.toLocalDate() == date`
- PENDING and CANCELLED lessons excluded

**Error Handling**:
- Invalid date: Propagate as IllegalArgumentException
- Database errors propagated in Flow

**Testing**:
- ✅ Returns only COMPLETED lessons for specified date
- ✅ Excludes PENDING lessons for date
- ✅ Excludes CANCELLED lessons for date
- ✅ Excludes COMPLETED lessons from other dates
- ✅ Updates when lesson status changes from PENDING → COMPLETED
- ✅ Returns empty list when no completed lessons

---

### 3. GetLessonWithStudentUseCase

**Purpose**: Fetch a specific lesson with its associated student data for notification building

**Package**: `com.barutdev.kora.domain.usecase.notification`

**Contract**:
```kotlin
interface GetLessonWithStudentUseCase {
    /**
     * Retrieves lesson and student data for notification construction.
     * Used in BroadcastReceiver to get fresh data at notification time.
     *
     * @param lessonId Unique lesson identifier
     * @return LessonWithStudent if found, null if lesson or student not found
     */
    suspend operator fun invoke(lessonId: String): LessonWithStudent?
}
```

**Inputs**:
- `lessonId: String` - Unique lesson identifier

**Outputs**: `LessonWithStudent?`
- Returns combined lesson + student data if both exist
- Returns `null` if lesson not found or student not found (orphaned lesson)

**Data Structure**:
```kotlin
data class LessonWithStudent(
    val lesson: Lesson,
    val student: Student
)
```

**Business Rules**:
- Must join Lesson and Student via `lesson.studentId == student.id`
- Returns null (not exception) if data missing (graceful degradation)

**Error Handling**:
- Missing lesson: Return `null`
- Missing student: Return `null`
- Database errors: Propagate as exceptions

**Testing**:
- ✅ Returns LessonWithStudent when both exist
- ✅ Returns null when lesson not found
- ✅ Returns null when student not found (orphaned lesson)
- ✅ Returns fresh data (not cached)

---

### 4. ScheduleNotificationAlarmsUseCase

**Purpose**: Schedule AlarmManager alarms for a specific lesson's notifications

**Package**: `com.barutdev.kora.domain.usecase.notification`

**Contract**:
```kotlin
interface ScheduleNotificationAlarmsUseCase {
    /**
     * Schedules both lesson reminder and note reminder alarms for a lesson.
     * Calculates trigger times based on user settings and lesson date.
     *
     * @param lessonId Lesson to schedule notifications for
     * @throws LessonNotFoundException if lesson doesn't exist
     * @throws SettingsNotConfiguredException if reminder times not set
     */
    suspend operator fun invoke(lessonId: String)
}
```

**Inputs**:
- `lessonId: String` - Lesson to schedule notifications for

**Outputs**: None (side effect: schedules alarms)

**Business Logic**:
1. Fetch lesson and student data via `GetLessonWithStudentUseCase`
2. Fetch reminder settings via `SettingsRepository.getReminderSettings()`
3. Calculate trigger times:
   - Lesson reminder: `lessonDate + lessonReminderTime`
   - Note reminder: `lessonDate + logReminderTime`
4. Create `AlarmScheduleRequest` for each notification type
5. Schedule alarms via AlarmManager wrapper/repository
6. Store alarm metadata for cancellation

**Business Rules**:
- Skip scheduling if lesson date is in the past
- Skip scheduling if lesson is CANCELLED
- Reschedule if alarms already exist for this lesson (cancel first)
- Use unique request codes: `hash(lessonId + notificationType)`

**Error Handling**:
- Lesson not found: Throw `LessonNotFoundException`
- Settings not configured: Throw `SettingsNotConfiguredException`
- AlarmManager errors: Propagate

**Testing**:
- ✅ Schedules two alarms (lesson + note reminder) for valid lesson
- ✅ Uses correct trigger times based on settings
- ✅ Skips scheduling for past-dated lessons
- ✅ Skips scheduling for CANCELLED lessons
- ✅ Cancels existing alarms before rescheduling
- ❌ Throws exception when lesson not found
- ❌ Throws exception when settings not configured

---

### 5. CancelNotificationAlarmsUseCase

**Purpose**: Cancel scheduled alarms for a specific lesson

**Package**: `com.barutdev.kora.domain.usecase.notification`

**Contract**:
```kotlin
interface CancelNotificationAlarmsUseCase {
    /**
     * Cancels both lesson and note reminder alarms for a lesson.
     * Used when lessons are deleted or cancelled.
     *
     * @param lessonId Lesson whose alarms should be cancelled
     */
    suspend operator fun invoke(lessonId: String)
}
```

**Inputs**:
- `lessonId: String` - Lesson whose alarms to cancel

**Outputs**: None (side effect: cancels alarms)

**Business Logic**:
1. Calculate request codes for both notification types
2. Cancel alarms via AlarmManager wrapper
3. Remove alarm metadata from tracking storage

**Business Rules**:
- Idempotent: Safe to call even if no alarms scheduled
- Cancels both types (lesson + note reminders)

**Error Handling**:
- AlarmManager errors: Log but don't throw (cancellation is best-effort)

**Testing**:
- ✅ Cancels both alarm types for lesson
- ✅ Idempotent (no error if alarms don't exist)
- ✅ Removes alarms from AlarmManager

---

### 6. RescheduleAllNotificationAlarmsUseCase

**Purpose**: Reschedule all alarms when settings change or after boot

**Package**: `com.barutdev.kora.domain.usecase.notification`

**Contract**:
```kotlin
interface RescheduleAllNotificationAlarmsUseCase {
    /**
     * Cancels all existing alarms and reschedules for all active lessons.
     * Used when reminder time settings change or after device boot.
     *
     * @throws SettingsNotConfiguredException if reminder times not set
     */
    suspend operator fun invoke()
}
```

**Inputs**: None

**Outputs**: None (side effect: reschedules all alarms)

**Business Logic**:
1. Cancel all existing notification alarms
2. Fetch all active lessons (status != CANCELLED, scheduledTime >= today)
3. For each lesson:
   - Call `ScheduleNotificationAlarmsUseCase(lesson.id)`
4. Skip lessons with past dates

**Business Rules**:
- Only reschedule for today and future lessons
- Exclude CANCELLED lessons
- Use current settings for new trigger times

**Error Handling**:
- Settings not configured: Throw exception
- Individual lesson scheduling failures: Log and continue
- Database errors: Propagate

**Testing**:
- ✅ Cancels all existing alarms
- ✅ Reschedules for all active future lessons
- ✅ Excludes CANCELLED lessons
- ✅ Excludes past-dated lessons
- ❌ Throws exception if settings not configured

---

### 7. GetReminderSettingsUseCase

**Purpose**: Retrieve user's notification timing preferences

**Package**: `com.barutdev.kora.domain.usecase.notification`

**Contract**:
```kotlin
interface GetReminderSettingsUseCase {
    /**
     * Returns a Flow of reminder settings that emits whenever settings change.
     *
     * @return Flow<ReminderSettings> - Current reminder time configuration
     */
    operator fun invoke(): Flow<ReminderSettings>
}
```

**Inputs**: None

**Outputs**: `Flow<ReminderSettings>`
- Emits current settings immediately
- Emits updates whenever settings change
- Never emits null (uses defaults if not configured)

**Data Structure**:
```kotlin
data class ReminderSettings(
    val lessonReminderTime: LocalTime,
    val logReminderTime: LocalTime
)
```

**Business Rules**:
- Default `lessonReminderTime`: 08:00
- Default `logReminderTime`: 20:00

**Error Handling**:
- Parse errors: Use defaults
- Missing settings: Use defaults

**Testing**:
- ✅ Returns configured settings
- ✅ Returns defaults when not configured
- ✅ Emits updates when settings change
- ✅ Handles parse errors gracefully

---

## UseCase Dependency Graph

```
RescheduleAllNotificationAlarmsUseCase
├─> ScheduleNotificationAlarmsUseCase
│   ├─> GetLessonWithStudentUseCase
│   │   ├─> LessonRepository
│   │   └─> StudentRepository
│   ├─> GetReminderSettingsUseCase
│   │   └─> SettingsRepository
│   └─> AlarmScheduler (repository/manager)
│
├─> CancelNotificationAlarmsUseCase
│   └─> AlarmScheduler
│
GetTodayLessonsUseCase
└─> LessonRepository

GetCompletedLessonsForDateUseCase
└─> LessonRepository
```

---

## BroadcastReceiver Usage

**NotificationAlarmReceiver** uses these UseCases:

```kotlin
@AndroidEntryPoint
class NotificationAlarmReceiver : HiltBroadcastReceiver() {
    @Inject lateinit var getLessonWithStudentUseCase: GetLessonWithStudentUseCase
    @Inject lateinit var notificationBuilder: NotificationBuilder

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        val pendingResult = goAsync()
        receiverScope.launch {
            try {
                // Extract from intent
                val lessonId = intent.getStringExtra("lessonId") ?: return@launch
                val notificationType = intent.getSerializableExtra("type") as NotificationType

                // Fetch fresh data
                val lessonWithStudent = getLessonWithStudentUseCase(lessonId)
                if (lessonWithStudent == null) {
                    // Lesson or student deleted
                    return@launch
                }

                // Smart behavior for note reminders
                if (notificationType == NOTE_REMINDER &&
                    lessonWithStudent.lesson.status != COMPLETED) {
                    // Skip notification for non-completed lessons
                    return@launch
                }

                // Build and show notification
                val notification = notificationBuilder.build(
                    notificationType,
                    lessonWithStudent
                )
                notificationManager.notify(notification.id, notification)

            } finally {
                pendingResult.finish()
            }
        }
    }
}
```

---

## Summary

**New UseCases**: 7 total
1. `GetTodayLessonsUseCase` - Query today's lessons
2. `GetCompletedLessonsForDateUseCase` - Query completed lessons
3. `GetLessonWithStudentUseCase` - Fetch lesson + student for notifications
4. `ScheduleNotificationAlarmsUseCase` - Schedule alarms for a lesson
5. `CancelNotificationAlarmsUseCase` - Cancel alarms for a lesson
6. `RescheduleAllNotificationAlarmsUseCase` - Reschedule all alarms
7. `GetReminderSettingsUseCase` - Access notification timing settings

**Repositories Used**:
- `LessonRepository` (existing, extended)
- `StudentRepository` (existing)
- `SettingsRepository` (new)
- `AlarmScheduler` (new wrapper for AlarmManager)

All UseCases follow single responsibility principle and are fully testable with mock repositories.
