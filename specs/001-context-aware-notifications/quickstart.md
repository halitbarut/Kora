# Quickstart Guide: Context-Aware Notification Enhancement

**Feature**: 001-context-aware-notifications
**Date**: 2025-11-02
**Purpose**: Setup, development, and testing guide for implementing notification enhancements

---

## Prerequisites

### Required Permissions

Add to `AndroidManifest.xml`:

```xml
<!-- Notification permissions -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" /> <!-- Android 13+ -->
<uses-permission android:name="android.permission.USE_EXACT_ALARM" /> <!-- Exact-time alarms -->
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" /> <!-- Reschedule after boot -->
```

### Required Components

Register BroadcastReceivers in `AndroidManifest.xml`:

```xml
<application>
    <!-- Notification Alarm Receiver -->
    <receiver
        android:name=".data.notification.NotificationAlarmReceiver"
        android:enabled="true"
        android:exported="false" />

    <!-- Boot Completed Receiver (reschedule alarms after reboot) -->
    <receiver
        android:name=".data.notification.BootCompletedReceiver"
        android:enabled="true"
        android:exported="true">
        <intent-filter>
            <action android:name="android.intent.action.BOOT_COMPLETED" />
        </intent-filter>
    </receiver>
</application>
```

### Dependencies

Verify in `build.gradle.kts` (app module):

```kotlin
dependencies {
    // DataStore for settings (if not already present)
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Existing dependencies (should already be present)
    implementation("androidx.hilt:hilt-android:...")
    implementation("androidx.navigation:navigation-compose:...")
    implementation("androidx.room:room-runtime:...")
    kapt("androidx.room:room-compiler:...")

    // Testing
    testImplementation("junit:junit:...")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:...")
    androidTestImplementation("androidx.test.ext:junit:...")
}
```

---

## Project Structure Setup

Create the following package structure:

```
app/src/main/java/com/barutdev/kora/
├── data/
│   ├── notification/
│   │   ├── AlarmSchedulerImpl.kt
│   │   ├── NotificationAlarmReceiver.kt
│   │   ├── BootCompletedReceiver.kt
│   │   └── NotificationBuilderImpl.kt
│   ├── repository/
│   │   └── SettingsRepositoryImpl.kt
│   └── datasource/
│       └── local/
│           └── datastore/
│               └── SettingsDataStore.kt
├── domain/
│   ├── model/
│   │   ├── NotificationType.kt
│   │   ├── NotificationData.kt
│   │   ├── AlarmScheduleRequest.kt
│   │   ├── ReminderSettings.kt
│   │   └── LessonWithStudent.kt
│   ├── repository/
│   │   ├── AlarmScheduler.kt
│   │   ├── SettingsRepository.kt
│   │   └── NotificationBuilder.kt
│   └── usecase/
│       └── notification/
│           ├── GetTodayLessonsUseCase.kt
│           ├── GetCompletedLessonsForDateUseCase.kt
│           ├── GetLessonWithStudentUseCase.kt
│           ├── ScheduleNotificationAlarmsUseCase.kt
│           ├── CancelNotificationAlarmsUseCase.kt
│           ├── RescheduleAllNotificationAlarmsUseCase.kt
│           └── GetReminderSettingsUseCase.kt
└── di/
    └── NotificationModule.kt
```

---

## Implementation Steps

### Step 1: Create Domain Models

**File**: `domain/model/NotificationType.kt`

```kotlin
package com.barutdev.kora.domain.model

enum class NotificationType {
    LESSON_REMINDER,
    NOTE_REMINDER
}
```

**File**: `domain/model/NotificationData.kt`

```kotlin
package com.barutdev.kora.domain.model

import java.time.LocalDate
import java.time.LocalTime

data class NotificationData(
    val type: NotificationType,
    val lessonId: String,
    val studentId: String,
    val studentName: String,
    val lessonTime: LocalTime,
    val lessonDate: LocalDate
)
```

**File**: `domain/model/ReminderSettings.kt`

```kotlin
package com.barutdev.kora.domain.model

import java.time.LocalTime

data class ReminderSettings(
    val lessonReminderTime: LocalTime = LocalTime.of(8, 0),
    val logReminderTime: LocalTime = LocalTime.of(20, 0)
)
```

**File**: `domain/model/AlarmScheduleRequest.kt`

```kotlin
package com.barutdev.kora.domain.model

import java.time.LocalDateTime

data class AlarmScheduleRequest(
    val lessonId: String,
    val notificationType: NotificationType,
    val triggerTime: LocalDateTime,
    val notificationData: NotificationData
)
```

---

### Step 2: Create Repository Interfaces

**File**: `domain/repository/SettingsRepository.kt`

```kotlin
package com.barutdev.kora.domain.repository

import com.barutdev.kora.domain.model.ReminderSettings
import kotlinx.coroutines.flow.Flow
import java.time.LocalTime

interface SettingsRepository {
    fun getReminderSettings(): Flow<ReminderSettings>
    suspend fun updateLessonReminderTime(time: LocalTime)
    suspend fun updateLogReminderTime(time: LocalTime)
}
```

**File**: `domain/repository/AlarmScheduler.kt`

```kotlin
package com.barutdev.kora.domain.repository

import com.barutdev.kora.domain.model.AlarmScheduleRequest
import com.barutdev.kora.domain.model.NotificationType

interface AlarmScheduler {
    suspend fun scheduleAlarm(request: AlarmScheduleRequest)
    suspend fun cancelAlarm(lessonId: String, type: NotificationType)
    suspend fun cancelAllAlarms()
}
```

**File**: `domain/repository/NotificationBuilder.kt`

```kotlin
package com.barutdev.kora.domain.repository

import android.app.Notification
import com.barutdev.kora.domain.model.LessonWithStudent
import com.barutdev.kora.domain.model.NotificationType

interface NotificationBuilder {
    fun build(type: NotificationType, lessonWithStudent: LessonWithStudent): Notification
}
```

---

### Step 3: Extend LessonRepository

Add methods to existing `LessonRepository` interface:

```kotlin
// In domain/repository/LessonRepository.kt

fun getLessonsForDate(date: LocalDate): Flow<List<Lesson>>
fun getCompletedLessonsForDate(date: LocalDate): Flow<List<Lesson>>
suspend fun getLessonWithStudent(lessonId: String): LessonWithStudent?
fun getActiveLessons(): Flow<List<Lesson>>
```

Implement in `LessonRepositoryImpl`:

```kotlin
// In data/repository/LessonRepositoryImpl.kt

override fun getLessonsForDate(date: LocalDate): Flow<List<Lesson>> {
    return lessonDao.getLessonsForDate(date)
}

override fun getCompletedLessonsForDate(date: LocalDate): Flow<List<Lesson>> {
    return lessonDao.getLessonsForDate(date)
        .map { lessons -> lessons.filter { it.status == LessonStatus.COMPLETED } }
}

override suspend fun getLessonWithStudent(lessonId: String): LessonWithStudent? {
    val lesson = lessonDao.getLessonById(lessonId) ?: return null
    val student = studentDao.getStudentById(lesson.studentId) ?: return null
    return LessonWithStudent(lesson, student)
}

override fun getActiveLessons(): Flow<List<Lesson>> {
    val today = LocalDate.now()
    return lessonDao.getAllLessons()
        .map { lessons ->
            lessons.filter {
                it.status != LessonStatus.CANCELLED &&
                it.scheduledTime.toLocalDate() >= today
            }
        }
}
```

---

### Step 4: Create UseCases

See `contracts/UseCases.md` for full implementations. Example:

**File**: `domain/usecase/notification/GetTodayLessonsUseCase.kt`

```kotlin
package com.barutdev.kora.domain.usecase.notification

import com.barutdev.kora.domain.model.Lesson
import com.barutdev.kora.domain.repository.LessonRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class GetTodayLessonsUseCase @Inject constructor(
    private val lessonRepository: LessonRepository
) {
    operator fun invoke(): Flow<List<Lesson>> {
        val today = LocalDate.now()
        return lessonRepository.getLessonsForDate(today)
    }
}
```

---

### Step 5: Implement Repositories

**File**: `data/repository/SettingsRepositoryImpl.kt`

```kotlin
package com.barutdev.kora.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.barutdev.kora.domain.model.ReminderSettings
import com.barutdev.kora.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalTime
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    private val lessonReminderTimeKey = stringPreferencesKey("lesson_reminder_time")
    private val logReminderTimeKey = stringPreferencesKey("log_reminder_time")

    override fun getReminderSettings(): Flow<ReminderSettings> {
        return dataStore.data.map { preferences ->
            val lessonTime = preferences[lessonReminderTimeKey]
                ?.let { LocalTime.parse(it) }
                ?: LocalTime.of(8, 0)

            val logTime = preferences[logReminderTimeKey]
                ?.let { LocalTime.parse(it) }
                ?: LocalTime.of(20, 0)

            ReminderSettings(lessonTime, logTime)
        }
    }

    override suspend fun updateLessonReminderTime(time: LocalTime) {
        dataStore.edit { preferences ->
            preferences[lessonReminderTimeKey] = time.toString()
        }
    }

    override suspend fun updateLogReminderTime(time: LocalTime) {
        dataStore.edit { preferences ->
            preferences[logReminderTimeKey] = time.toString()
        }
    }
}
```

**File**: `data/notification/AlarmSchedulerImpl.kt` - See `contracts/Repositories.md` for full code

---

### Step 6: Create BroadcastReceiver

**File**: `data/notification/NotificationAlarmReceiver.kt`

```kotlin
package com.barutdev.kora.data.notification

import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.barutdev.kora.domain.model.NotificationType
import com.barutdev.kora.domain.repository.NotificationBuilder
import com.barutdev.kora.domain.usecase.notification.GetLessonWithStudentUseCase
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotificationAlarmReceiver : HiltBroadcastReceiver() {

    @Inject
    lateinit var getLessonWithStudentUseCase: GetLessonWithStudentUseCase

    @Inject
    lateinit var notificationBuilder: NotificationBuilder

    @Inject
    @ApplicationContext
    lateinit var context: Context

    private val receiverScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        val pendingResult = goAsync()

        receiverScope.launch {
            try {
                handleNotification(intent)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun handleNotification(intent: Intent) {
        val lessonId = intent.getStringExtra("lessonId") ?: return
        val type = intent.getSerializableExtra("type") as? NotificationType ?: return

        val lessonWithStudent = getLessonWithStudentUseCase(lessonId) ?: return

        // Smart behavior: only show note reminders for completed lessons
        if (type == NotificationType.NOTE_REMINDER &&
            lessonWithStudent.lesson.status != LessonStatus.COMPLETED
        ) {
            return
        }

        val notification = notificationBuilder.build(type, lessonWithStudent)
        val notificationId = lessonId.hashCode()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }
}
```

---

### Step 7: Create Hilt Module

**File**: `di/NotificationModule.kt`

```kotlin
package com.barutdev.kora.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.barutdev.kora.data.notification.AlarmSchedulerImpl
import com.barutdev.kora.data.notification.NotificationBuilderImpl
import com.barutdev.kora.data.repository.SettingsRepositoryImpl
import com.barutdev.kora.domain.repository.AlarmScheduler
import com.barutdev.kora.domain.repository.NotificationBuilder
import com.barutdev.kora.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Module
@InstallIn(SingletonComponent::class)
object NotificationDataModule {

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationModule {

    @Binds
    abstract fun bindAlarmScheduler(impl: AlarmSchedulerImpl): AlarmScheduler

    @Binds
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds
    abstract fun bindNotificationBuilder(impl: NotificationBuilderImpl): NotificationBuilder
}
```

---

### Step 8: Add Strings

**File**: `res/values/strings.xml`

```xml
<!-- Notification strings -->
<string name="notification_lesson_reminder_title">Upcoming Lesson: %s</string>
<string name="notification_lesson_reminder_content">Your lesson is today at %s. Don\'t forget to prepare!</string>
<string name="notification_note_reminder_title">Log Lesson Notes: %s</string>
<string name="notification_note_reminder_content">Add your notes and observations for the lesson you had today at %s.</string>

<!-- Notification channels -->
<string name="channel_lesson_reminders_name">Lesson Reminders</string>
<string name="channel_lesson_reminders_desc">Notifications to prepare for upcoming lessons</string>
<string name="channel_lesson_notes_name">Lesson Notes</string>
<string name="channel_lesson_notes_desc">Reminders to log notes for completed lessons</string>
```

---

## Testing Guide

### Unit Tests

**Test**: `GetTodayLessonsUseCaseTest.kt`

```kotlin
@Test
fun `invoke returns lessons for today only`() = runTest {
    // Arrange
    val today = LocalDate.now()
    val todayLesson = Lesson(id = "1", scheduledTime = today.atTime(10, 0), ...)
    val tomorrowLesson = Lesson(id = "2", scheduledTime = today.plusDays(1).atTime(10, 0), ...)

    whenever(lessonRepository.getLessonsForDate(today))
        .thenReturn(flowOf(listOf(todayLesson)))

    // Act
    val result = getTodayLessonsUseCase().first()

    // Assert
    assertEquals(1, result.size)
    assertEquals("1", result[0].id)
}
```

### Integration Tests

**Test**: Alarm scheduling end-to-end

```kotlin
@Test
fun `scheduleAlarm creates alarm with correct trigger time`() = runTest {
    // Arrange
    val lesson = createTestLesson(scheduledTime = LocalDate.now().atTime(14, 0))
    val settings = ReminderSettings(
        lessonReminderTime = LocalTime.of(8, 0),
        logReminderTime = LocalTime.of(20, 0)
    )

    // Act
    scheduleNotificationAlarmsUseCase(lesson.id)

    // Assert
    verify(alarmScheduler).scheduleAlarm(
        argThat { request ->
            request.triggerTime == LocalDate.now().atTime(8, 0) &&
            request.notificationType == NotificationType.LESSON_REMINDER
        }
    )
}
```

### Manual Testing

1. **Test Lesson Reminder**:
   - Create a lesson for today
   - Set "Lesson reminder time" to current time + 2 minutes
   - Wait for notification
   - Tap notification → should open student calendar

2. **Test Note Reminder (Completed)**:
   - Create a lesson for today
   - Mark lesson as COMPLETED
   - Set "Log reminder time" to current time + 2 minutes
   - Wait for notification
   - Tap notification → should open lesson edit screen

3. **Test Note Reminder (Not Completed)**:
   - Create a lesson for today (keep as PENDING)
   - Set "Log reminder time" to current time + 2 minutes
   - Wait → **no notification should appear**

4. **Test Settings Change**:
   - Schedule multiple lessons
   - Change "Lesson reminder time"
   - Verify alarms rescheduled with new time

---

## Debugging

### Check Scheduled Alarms

```bash
adb shell dumpsys alarm | grep kora
```

### Check Notification Channels

```bash
adb shell dumpsys notification | grep kora
```

### Force Trigger Alarm (Testing)

Use ADB to manually trigger alarms:

```bash
adb shell am broadcast -a android.intent.action.BOOT_COMPLETED
```

---

## Common Issues

| Issue | Solution |
|-------|----------|
| Notifications not appearing | Check POST_NOTIFICATIONS permission granted (Android 13+) |
| Alarms not firing | Verify USE_EXACT_ALARM permission in manifest |
| Deep links not working | Check Navigation graph has matching deep link declarations |
| Receiver not injecting dependencies | Ensure `@AndroidEntryPoint` annotation present |
| Alarms cleared after reboot | Implement BootCompletedReceiver to reschedule |

---

## Next Steps

After implementing this feature:

1. Run unit tests: `./gradlew test`
2. Run instrumentation tests: `./gradlew connectedAndroidTest`
3. Build debug APK: `./gradlew assembleDebug`
4. Manual testing on physical device (Doze mode testing)
5. Create pull request with implementation

Refer to `tasks.md` (generated via `/speckit.tasks`) for detailed task breakdown.
