# Repository Contracts: Context-Aware Notification Enhancement

**Feature**: 001-context-aware-notifications
**Date**: 2025-11-02
**Purpose**: Define Repository interfaces for data access and alarm management

---

## Repository Layer Contracts

### 1. SettingsRepository (New)

**Purpose**: Manage user notification preferences using DataStore Preferences

**Package**: `com.barutdev.kora.domain.repository`

**Contract**:
```kotlin
interface SettingsRepository {
    /**
     * Returns a Flow of reminder settings that emits whenever settings change.
     * Never returns null - provides defaults if not configured.
     *
     * @return Flow<ReminderSettings> - Current notification timing preferences
     */
    fun getReminderSettings(): Flow<ReminderSettings>

    /**
     * Updates the time for morning lesson reminders.
     *
     * @param time New lesson reminder time (24-hour format)
     */
    suspend fun updateLessonReminderTime(time: LocalTime)

    /**
     * Updates the time for evening note reminders.
     *
     * @param time New log reminder time (24-hour format)
     */
    suspend fun updateLogReminderTime(time: LocalTime)
}
```

**Implementation Details**:
- **Backing Store**: DataStore Preferences (preferred) or SharedPreferences
- **Preference Keys**:
  - `LESSON_REMINDER_TIME_KEY = "lesson_reminder_time"`
  - `LOG_REMINDER_TIME_KEY = "log_reminder_time"`
- **Storage Format**: ISO-8601 time string (e.g., "08:00", "20:00")
- **Parsing**: `LocalTime.parse(value)` with try-catch for defaults
- **Defaults**:
  - `lessonReminderTime`: `LocalTime.of(8, 0)` (08:00)
  - `logReminderTime`: `LocalTime.of(20, 0)` (20:00)

**Data Model**:
```kotlin
data class ReminderSettings(
    val lessonReminderTime: LocalTime = LocalTime.of(8, 0),
    val logReminderTime: LocalTime = LocalTime.of(20, 0)
)
```

**Error Handling**:
- Parse errors: Use default values, log warning
- DataStore errors: Propagate exceptions
- Invalid times: Validated before saving

**Testing**:
- ✅ Returns defaults when not configured
- ✅ Persists and retrieves custom times
- ✅ Emits updates when times change
- ✅ Handles parse errors gracefully
- ✅ Validates time ranges (00:00-23:59)

---

### 2. LessonRepository (Existing - Extended)

**Purpose**: Access lesson data with new query methods for notification scheduling

**Package**: `com.barutdev.kora.domain.repository`

**New Methods**:
```kotlin
interface LessonRepository {
    // Existing methods...
    // suspend fun createLesson(lesson: Lesson)
    // suspend fun updateLesson(lesson: Lesson)
    // suspend fun deleteLesson(lessonId: String)
    // fun getLessons(): Flow<List<Lesson>>
    // etc.

    /**
     * Returns a Flow of lessons scheduled for a specific date.
     * Includes all statuses (PENDING, COMPLETED, CANCELLED).
     *
     * @param date Date to query
     * @return Flow<List<Lesson>> - Lessons on the specified date
     */
    fun getLessonsForDate(date: LocalDate): Flow<List<Lesson>>

    /**
     * Returns a Flow of completed lessons for a specific date.
     * Filters to only status == COMPLETED.
     *
     * @param date Date to query
     * @return Flow<List<Lesson>> - Completed lessons on the specified date
     */
    fun getCompletedLessonsForDate(date: LocalDate): Flow<List<Lesson>>

    /**
     * Retrieves a lesson with its associated student data.
     * Returns null if lesson or student not found.
     *
     * @param lessonId Lesson identifier
     * @return LessonWithStudent if found, null otherwise
     */
    suspend fun getLessonWithStudent(lessonId: String): LessonWithStudent?

    /**
     * Returns a Flow of all active lessons (not cancelled, scheduled >= today).
     * Used for rescheduling alarms.
     *
     * @return Flow<List<Lesson>> - Active future lessons
     */
    fun getActiveLessons(): Flow<List<Lesson>>
}
```

**Data Structures**:
```kotlin
data class LessonWithStudent(
    val lesson: Lesson,
    val student: Student
)
```

**Implementation Notes**:
- `getLessonsForDate()`: Room query with `WHERE DATE(scheduledTime) = :date`
- `getCompletedLessonsForDate()`: Additional filter `AND status = 'COMPLETED'`
- `getLessonWithStudent()`: Room relation or manual join via `studentId`
- `getActiveLessons()`: `WHERE status != 'CANCELLED' AND scheduledTime >= :today`

**Testing**:
- ✅ getLessonsForDate() returns all lessons for date
- ✅ getCompletedLessonsForDate() filters by status
- ✅ getLessonWithStudent() returns combined data
- ✅ getLessonWithStudent() returns null when missing
- ✅ getActiveLessons() excludes cancelled and past lessons

---

### 3. AlarmScheduler (New)

**Purpose**: Abstract AlarmManager operations for testability and Clean Architecture

**Package**: `com.barutdev.kora.domain.repository`

**Contract**:
```kotlin
interface AlarmScheduler {
    /**
     * Schedules an exact alarm for a notification.
     * Uses setExactAndAllowWhileIdle() for Doze compatibility.
     *
     * @param request Alarm scheduling details
     */
    suspend fun scheduleAlarm(request: AlarmScheduleRequest)

    /**
     * Cancels a specific alarm by lesson and type.
     *
     * @param lessonId Lesson identifier
     * @param type Notification type (lesson or note reminder)
     */
    suspend fun cancelAlarm(lessonId: String, type: NotificationType)

    /**
     * Cancels all notification alarms.
     * Used when rescheduling or during testing cleanup.
     */
    suspend fun cancelAllAlarms()
}
```

**Data Model**:
```kotlin
data class AlarmScheduleRequest(
    val lessonId: String,
    val notificationType: NotificationType,
    val triggerTime: LocalDateTime,
    val notificationData: NotificationData
)
```

**Implementation Details** (in `AlarmSchedulerImpl`):

```kotlin
class AlarmSchedulerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AlarmScheduler {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override suspend fun scheduleAlarm(request: AlarmScheduleRequest) {
        val intent = Intent(context, NotificationAlarmReceiver::class.java).apply {
            putExtra("lessonId", request.lessonId)
            putExtra("type", request.notificationType)
            putExtra("studentId", request.notificationData.studentId)
            putExtra("studentName", request.notificationData.studentName)
            putExtra("lessonTime", request.notificationData.lessonTime.toString())
            putExtra("lessonDate", request.notificationData.lessonDate.toString())
        }

        val requestCode = calculateRequestCode(request.lessonId, request.notificationType)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAtMillis = request.triggerTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
    }

    override suspend fun cancelAlarm(lessonId: String, type: NotificationType) {
        val intent = Intent(context, NotificationAlarmReceiver::class.java)
        val requestCode = calculateRequestCode(lessonId, type)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
    }

    override suspend fun cancelAllAlarms() {
        // Implementation would track active alarms or cancel by known patterns
        // For simplicity, can rely on individual cancellation during reschedule
    }

    private fun calculateRequestCode(lessonId: String, type: NotificationType): Int {
        return "$lessonId:${type.name}".hashCode()
    }
}
```

**Business Rules**:
- Request codes must be unique per lesson + type combination
- Use `FLAG_IMMUTABLE` for Android 12+ compatibility
- Use `setExactAndAllowWhileIdle()` for Doze mode delivery
- Cancel existing PendingIntent before creating new one (via FLAG_UPDATE_CURRENT)

**Error Handling**:
- AlarmManager exceptions: Propagate to caller
- Invalid trigger times: Validate before scheduling

**Testing**:
- ✅ Schedules alarm with correct trigger time
- ✅ Creates unique request codes per lesson+type
- ✅ Cancels alarms correctly
- ✅ Handles duplicate scheduling (updates existing)
- Mock AlarmManager for unit tests

---

### 4. NotificationBuilder (New)

**Purpose**: Build Android notifications from domain data

**Package**: `com.barutdev.kora.data.notification` (infrastructure concern)

**Contract**:
```kotlin
interface NotificationBuilder {
    /**
     * Builds a notification for display.
     *
     * @param type Type of notification (lesson or note reminder)
     * @param lessonWithStudent Lesson and student data
     * @return Notification ready for display
     */
    fun build(
        type: NotificationType,
        lessonWithStudent: LessonWithStudent
    ): Notification
}
```

**Implementation Details**:

```kotlin
class NotificationBuilderImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationManager: NotificationManager
) : NotificationBuilder {

    companion object {
        const val CHANNEL_LESSON_REMINDERS = "lesson_reminders"
        const val CHANNEL_LESSON_NOTES = "lesson_notes"
    }

    init {
        createNotificationChannels()
    }

    override fun build(
        type: NotificationType,
        lessonWithStudent: LessonWithStudent
    ): Notification {
        val (lesson, student) = lessonWithStudent
        val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
        val lessonTimeStr = lesson.scheduledTime.format(timeFormatter)

        return when (type) {
            NotificationType.LESSON_REMINDER -> buildLessonReminder(student.name, lessonTimeStr, student.id)
            NotificationType.NOTE_REMINDER -> buildNoteReminder(student.name, lessonTimeStr, lesson.id)
        }
    }

    private fun buildLessonReminder(studentName: String, lessonTime: String, studentId: String): Notification {
        val title = context.getString(R.string.notification_lesson_reminder_title, studentName)
        val content = context.getString(R.string.notification_lesson_reminder_content, lessonTime)

        val deepLinkUri = "app://kora/studentCalendar/$studentId".toUri()
        val pendingIntent = NavDeepLinkBuilder(context)
            .setGraph(R.navigation.nav_graph)
            .setDestination(R.id.studentCalendar)
            .setArguments(bundleOf("studentId" to studentId))
            .createPendingIntent()

        return NotificationCompat.Builder(context, CHANNEL_LESSON_REMINDERS)
            .setSmallIcon(R.drawable.ic_notification_lesson)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
    }

    private fun buildNoteReminder(studentName: String, lessonTime: String, lessonId: String): Notification {
        val title = context.getString(R.string.notification_note_reminder_title, studentName)
        val content = context.getString(R.string.notification_note_reminder_content, lessonTime)

        val deepLinkUri = "app://kora/lessonEdit/$lessonId".toUri()
        val pendingIntent = NavDeepLinkBuilder(context)
            .setGraph(R.navigation.nav_graph)
            .setDestination(R.id.lessonEdit)
            .setArguments(bundleOf("lessonId" to lessonId))
            .createPendingIntent()

        return NotificationCompat.Builder(context, CHANNEL_LESSON_NOTES)
            .setSmallIcon(R.drawable.ic_notification_notes)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
    }

    private fun createNotificationChannels() {
        val lessonRemindersChannel = NotificationChannel(
            CHANNEL_LESSON_REMINDERS,
            context.getString(R.string.channel_lesson_reminders_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.channel_lesson_reminders_desc)
        }

        val lessonNotesChannel = NotificationChannel(
            CHANNEL_LESSON_NOTES,
            context.getString(R.string.channel_lesson_notes_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.channel_lesson_notes_desc)
        }

        notificationManager.createNotificationChannels(
            listOf(lessonRemindersChannel, lessonNotesChannel)
        )
    }
}
```

**Localized Strings** (to add to `strings.xml`):
```xml
<!-- Notification titles -->
<string name="notification_lesson_reminder_title">Upcoming Lesson: %s</string>
<string name="notification_note_reminder_title">Log Lesson Notes: %s</string>

<!-- Notification content -->
<string name="notification_lesson_reminder_content">Your lesson is today at %s. Don't forget to prepare!</string>
<string name="notification_note_reminder_content">Add your notes and observations for the lesson you had today at %s.</string>

<!-- Notification channels -->
<string name="channel_lesson_reminders_name">Lesson Reminders</string>
<string name="channel_lesson_reminders_desc">Notifications to prepare for upcoming lessons</string>
<string name="channel_lesson_notes_name">Lesson Notes</string>
<string name="channel_lesson_notes_desc">Reminders to log notes for completed lessons</string>
```

**Testing**:
- ✅ Builds correct notification for lesson reminder
- ✅ Builds correct notification for note reminder
- ✅ Sets correct channel per type
- ✅ Formats student name and time correctly
- ✅ Creates correct deep link intents
- ✅ Channels created on initialization

---

## Repository Dependencies

```
AlarmScheduler (implementation: AlarmSchedulerImpl)
└─> Android AlarmManager (system service)

SettingsRepository (implementation: SettingsRepositoryImpl)
└─> DataStore Preferences or SharedPreferences

LessonRepository (existing, implementation: LessonRepositoryImpl)
├─> LessonDao (Room)
└─> StudentDao (Room)

NotificationBuilder (implementation: NotificationBuilderImpl)
├─> NotificationManager (system service)
└─> Navigation Compose (deep links)
```

---

## Hilt Bindings

**Module**: `NotificationModule`

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationModule {

    @Binds
    abstract fun bindAlarmScheduler(
        impl: AlarmSchedulerImpl
    ): AlarmScheduler

    @Binds
    abstract fun bindSettingsRepository(
        impl: SettingsRepositoryImpl
    ): SettingsRepository

    @Binds
    abstract fun bindNotificationBuilder(
        impl: NotificationBuilderImpl
    ): NotificationBuilder
}
```

---

## Summary

**New Repositories**: 3
1. `SettingsRepository` - User notification preferences
2. `AlarmScheduler` - AlarmManager abstraction
3. `NotificationBuilder` - Notification construction

**Extended Repositories**: 1
1. `LessonRepository` - 4 new query methods

**System Services**:
- AlarmManager (exact alarms)
- NotificationManager (channels, display)
- DataStore/SharedPreferences (settings storage)

All repositories follow Clean Architecture by abstracting Android platform dependencies behind interfaces.
