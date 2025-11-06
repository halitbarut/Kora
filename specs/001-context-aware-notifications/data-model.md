# Data Model: Context-Aware Notification Enhancement

**Feature**: 001-context-aware-notifications
**Date**: 2025-11-02

## Overview

This feature primarily reuses existing data entities (`Lesson`, `Student`) and introduces minimal new domain models for notification management. No database schema changes are required.

---

## Existing Entities (Reused)

### Lesson Entity

**Source**: Existing Room entity in `com.barutdev.kora.data.datasource.local`

**Attributes** (assumed based on requirements):
- `id: String` - Unique lesson identifier
- `studentId: String` - Foreign key to Student
- `scheduledTime: LocalDateTime` - Lesson date and time
- `status: LessonStatus` - Enum: PENDING, COMPLETED, CANCELLED
- `notes: String?` - Lesson notes (optional)
- Other fields (duration, subject, etc.)

**Relationships**:
- One-to-one with `Student` via `studentId`

**Usage in Feature**:
- Query lessons scheduled for "today" at morning reminder time
- Query completed lessons for "today" at evening reminder time
- Access `scheduledTime` for notification content formatting
- Check `status` to determine if note reminder should be sent

---

### Student Entity

**Source**: Existing Room entity in `com.barutdev.kora.data.datasource.local`

**Attributes** (assumed based on requirements):
- `id: String` - Unique student identifier
- `name: String` - Student name for notification content
- Other fields (contact info, etc.)

**Usage in Feature**:
- Display student name in notification title and content
- Navigate to student's calendar screen via deep link

---

## New Domain Models

### NotificationType (Enum)

**Purpose**: Differentiate between lesson reminder and note reminder notifications

**Package**: `com.barutdev.kora.domain.model`

```kotlin
enum class NotificationType {
    LESSON_REMINDER,  // Morning reminder for upcoming lesson
    NOTE_REMINDER     // Evening reminder to log notes for completed lesson
}
```

**Usage**:
- Passed as Intent extra to BroadcastReceiver
- Determines notification channel, content, and click action
- Used in alarm scheduling to create distinct alarms

---

### NotificationData (Data Class)

**Purpose**: Encapsulate data needed to build a notification

**Package**: `com.barutdev.kora.domain.model`

```kotlin
data class NotificationData(
    val type: NotificationType,
    val lessonId: String,
    val studentId: String,
    val studentName: String,
    val lessonTime: LocalTime,
    val lessonDate: LocalDate
)
```

**Attributes**:
- `type` - Which notification to display (lesson vs note reminder)
- `lessonId` - For deep linking to lesson edit screen
- `studentId` - For deep linking to student calendar
- `studentName` - For notification title formatting
- `lessonTime` - For notification content formatting
- `lessonDate` - For context (today's date)

**Validation Rules**:
- All fields non-null (except where marked optional)
- `studentName` non-empty
- `lessonTime` and `lessonDate` valid values

**Usage**:
- Built by UseCase from Lesson + Student entities
- Serialized to Intent extras for BroadcastReceiver
- Consumed by notification builder to create NotificationCompat instance

---

### AlarmScheduleRequest (Data Class)

**Purpose**: Represent a request to schedule an alarm

**Package**: `com.barutdev.kora.domain.model`

```kotlin
data class AlarmScheduleRequest(
    val lessonId: String,
    val notificationType: NotificationType,
    val triggerTime: LocalDateTime,
    val notificationData: NotificationData
)
```

**Attributes**:
- `lessonId` - Unique identifier for alarm cancellation
- `notificationType` - Type of notification (for unique request code)
- `triggerTime` - Exact time to trigger alarm
- `notificationData` - Data to pass to BroadcastReceiver

**Usage**:
- Passed to `ScheduleNotificationAlarmsUseCase`
- Used to calculate unique request code for PendingIntent
- Persisted as Intent extras in alarm

---

## Settings Model

### ReminderSettings (Data Class)

**Purpose**: User's notification timing preferences

**Package**: `com.barutdev.kora.domain.model`

```kotlin
data class ReminderSettings(
    val lessonReminderTime: LocalTime,  // "Settings -> Lesson reminder time"
    val logReminderTime: LocalTime      // "Settings -> Log reminder time"
)
```

**Attributes**:
- `lessonReminderTime` - Daily time to send lesson reminder notifications (e.g., 8:00 AM)
- `logReminderTime` - Daily time to send note reminder notifications (e.g., 8:00 PM)

**Validation Rules**:
- Both times must be valid 24-hour times
- No cross-validation needed (times can be same or different)

**Usage**:
- Retrieved from `SettingsRepository`
- Used to calculate exact trigger times for alarms
- Changes trigger alarm rescheduling

---

## State Transitions

### Lesson Status Flow (Existing)

```
PENDING → COMPLETED
        ↘ CANCELLED
```

**Relevance to Feature**:
- **PENDING → COMPLETED**: Qualifies lesson for note reminder
- **PENDING → CANCELLED**: Disqualifies lesson from note reminder
- **COMPLETED → CANCELLED**: Must cancel scheduled note reminder alarm

---

## Data Flow

### Morning Lesson Reminder Flow

```
1. User sets "Lesson reminder time" to 8:00 AM
2. Lesson created/updated → ScheduleNotificationAlarmsUseCase called
3. UseCase calculates trigger time: today 8:00 AM
4. AlarmScheduleRequest created with LESSON_REMINDER type
5. AlarmManager schedules exact alarm
6. At 8:00 AM → NotificationAlarmReceiver.onReceive() triggered
7. Receiver extracts Intent extras → NotificationData
8. GetLessonWithStudentUseCase fetches fresh Lesson + Student data
9. Build notification with "Upcoming Lesson: {studentName}" title
10. NotificationManager.notify() displays notification
11. User taps → Deep link to student calendar screen
```

### Evening Note Reminder Flow

```
1. User sets "Log reminder time" to 8:00 PM
2. Lesson created/updated → ScheduleNotificationAlarmsUseCase called
3. UseCase calculates trigger time: today 8:00 PM
4. AlarmScheduleRequest created with NOTE_REMINDER type
5. AlarmManager schedules exact alarm
6. During day: Lesson marked COMPLETED
7. At 8:00 PM → NotificationAlarmReceiver.onReceive() triggered
8. Receiver extracts Intent extras → NotificationData
9. GetLessonWithStudentUseCase fetches fresh Lesson + Student data
10. Check lesson.status == COMPLETED
    - If true: Build notification with "Log Lesson Notes: {studentName}" title
    - If false (PENDING/CANCELLED): Skip notification, return early
11. NotificationManager.notify() displays notification (if qualified)
12. User taps → Deep link to lesson edit screen
```

---

## Repository Interfaces

### SettingsRepository

**Purpose**: Access user notification preferences

**Package**: `com.barutdev.kora.domain.repository`

```kotlin
interface SettingsRepository {
    fun getReminderSettings(): Flow<ReminderSettings>
    suspend fun updateLessonReminderTime(time: LocalTime)
    suspend fun updateLogReminderTime(time: LocalTime)
}
```

**Implementation**:
- Backed by DataStore Preferences or SharedPreferences
- Keys: `LESSON_REMINDER_TIME_KEY`, `LOG_REMINDER_TIME_KEY`
- Stores as ISO-8601 time strings, parses to `LocalTime`

---

### LessonRepository (Existing - Extended)

**New Methods Needed**:

```kotlin
interface LessonRepository {
    // Existing methods...

    // New methods for notification feature
    fun getLessonsForDate(date: LocalDate): Flow<List<Lesson>>
    fun getCompletedLessonsForDate(date: LocalDate): Flow<List<Lesson>>
    suspend fun getLessonWithStudent(lessonId: String): LessonWithStudent?
}
```

**Data Classes**:

```kotlin
data class LessonWithStudent(
    val lesson: Lesson,
    val student: Student
)
```

---

## Immutability Guarantees

All domain models are Kotlin `data class` with `val` properties:
- ✅ `NotificationType` - Enum (inherently immutable)
- ✅ `NotificationData` - Immutable data class
- ✅ `AlarmScheduleRequest` - Immutable data class
- ✅ `ReminderSettings` - Immutable data class
- ✅ `LessonWithStudent` - Immutable data class containing immutable entities

No mutations occur - changes create new instances via `.copy()` or repository updates.

---

## Migration Strategy

**Database Migrations**: None required
- No new Room entities
- No schema changes to existing entities
- Feature uses existing Lesson and Student tables as-is

**Data Migration**: None required
- No historical data transformation needed
- Works with existing lesson and student records

**Settings Migration**:
- If `ReminderSettings` preferences don't exist, provide defaults:
  - `lessonReminderTime`: 08:00 (8:00 AM)
  - `logReminderTime`: 20:00 (8:00 PM)

---

## Testing Considerations

### Test Data Requirements

1. **Lesson Entities**:
   - Lessons with status PENDING, COMPLETED, CANCELLED
   - Lessons scheduled for "today", "tomorrow", "yesterday"
   - Lessons with various times throughout the day
   - Multiple lessons for same student on same day

2. **Student Entities**:
   - Students with various name lengths (short, long, special characters)
   - Students with multiple lessons

3. **Settings**:
   - Various reminder time configurations
   - Edge cases: midnight (00:00), just before midnight (23:59)

### Validation Test Cases

1. **NotificationData**:
   - ✅ Valid data creates instance
   - ❌ Empty studentName throws/fails validation
   - ❌ Invalid lessonTime/lessonDate throws/fails validation

2. **Status Filtering**:
   - ✅ COMPLETED lessons included in note reminder query
   - ❌ PENDING lessons excluded from note reminder query
   - ❌ CANCELLED lessons excluded from note reminder query

3. **Date Filtering**:
   - ✅ Only today's lessons returned for morning reminders
   - ✅ Only today's completed lessons returned for evening reminders
   - ❌ Yesterday's/tomorrow's lessons not included

---

## Summary

This feature introduces minimal new data models:
- **3 new domain models**: `NotificationType`, `NotificationData`, `AlarmScheduleRequest`
- **1 new settings model**: `ReminderSettings`
- **0 new Room entities**
- **3 new repository methods** on existing `LessonRepository`
- **1 new repository**: `SettingsRepository`

All models follow immutability principles. No database migrations required.
