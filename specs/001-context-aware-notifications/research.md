# Technical Research: Context-Aware Notification Enhancement

**Feature**: 001-context-aware-notifications
**Date**: 2025-11-02
**Purpose**: Resolve technical unknowns and document architectural decisions

## Research Tasks

### 1. Settings Repository/Preferences Manager

**Question**: What is the name of the existing settings repository or preferences manager for accessing "Lesson reminder time" and "Log reminder time"?

**Decision**: Assume standard Android preferences pattern - `SettingsRepository` or `PreferencesRepository` with DataStore or SharedPreferences backend.

**Rationale**:
- Android best practice is to use Jetpack DataStore (Preferences) or SharedPreferences
- Following Clean Architecture, there should be a Repository abstraction over the preference storage
- Common naming conventions: `SettingsRepository`, `PreferencesRepository`, `UserPreferencesRepository`

**Implementation Approach**:
- Create `SettingsRepository` interface if it doesn't exist
- Implement with DataStore Preferences (modern Android preference solution)
- Define preference keys: `LESSON_REMINDER_TIME_KEY` and `LOG_REMINDER_TIME_KEY`
- Return `Flow<LocalTime>` for reactive preference updates
- Inject repository into notification scheduling UseCase

**Alternatives Considered**:
- Direct SharedPreferences access: Rejected - violates Clean Architecture, not testable, not reactive
- Hardcoded settings: Rejected - spec requires user-configurable times

---

### 2. Alarm Rescheduling Strategy

**Question**: How should alarms be rescheduled when lessons are created/modified/deleted or when settings change?

**Decision**: Implement event-driven rescheduling with two triggers:
1. **Lesson Data Changes**: Reschedule when lessons are created, updated, or deleted
2. **Settings Changes**: Reschedule all alarms when reminder time settings change

**Rationale**:
- Lessons are dynamic - new lessons added, times changed, cancellations occur
- User settings can change at any time
- AlarmManager requires explicit rescheduling - alarms don't auto-update
- Need to maintain alarm accuracy without over-scheduling

**Implementation Approach**:

**A. Lesson Data Change Detection**:
- Create `ScheduleNotificationAlarmsUseCase` that calculates and schedules alarms for a specific lesson
- Call from Repository layer after lesson create/update/delete operations
- For updates: Cancel old alarms first, then schedule new ones
- For deletes: Cancel associated alarms only

**B. Settings Change Detection**:
- Observe `SettingsRepository` Flow in a long-running background worker or Application class
- When settings change detected, trigger `RescheduleAllNotificationAlarmsUseCase`
- Cancel all existing alarms
- Recalculate and reschedule for all active lessons

**C. Alarm Uniqueness**:
- Use unique request codes for PendingIntents: `hash(lessonId + notificationType)`
- Allows targeted cancellation and prevents duplicate alarms

**D. Boot Receiver**:
- Implement `BootCompletedReceiver` to reschedule all alarms after device reboot
- AlarmManager alarms are cleared on reboot

**Alternatives Considered**:
- Daily batch rescheduling: Rejected - inefficient, doesn't handle intraday changes
- Never reschedule: Rejected - stale alarms for deleted/modified lessons
- WorkManager periodic tasks: Rejected - need exact-time alarms, WorkManager has flexibility window

---

### 3. Notification Channel Strategy

**Question**: Should we create separate notification channels for lesson reminders vs note reminders, or use existing channel?

**Decision**: Create two separate notification channels:
1. **"Lesson Reminders"** channel - for upcoming lesson notifications
2. **"Lesson Notes"** channel - for post-lesson note reminder notifications

**Rationale**:
- Android 8.0+ requires notification channels
- Separate channels allow users to control each type independently (importance, sound, vibration)
- User story emphasizes distinction between notification types
- Users may want different notification behaviors (e.g., sound for lesson reminders, silent for note reminders)
- Follows Android best practices for categorizing notifications by purpose

**Implementation Approach**:
- Channel IDs:
  - `CHANNEL_LESSON_REMINDERS = "lesson_reminders"`
  - `CHANNEL_LESSON_NOTES = "lesson_notes"`
- Channel names (localized in strings.xml):
  - `channel_lesson_reminders_name` = "Lesson Reminders"
  - `channel_lesson_notes_name` = "Lesson Notes"
- Channel descriptions (localized):
  - `channel_lesson_reminders_desc` = "Notifications to prepare for upcoming lessons"
  - `channel_lesson_notes_desc` = "Reminders to log notes for completed lessons"
- Default importance: `NotificationManager.IMPORTANCE_HIGH` for both
- Create channels in Application class `onCreate()` or on first notification

**Alternatives Considered**:
- Single unified channel: Rejected - users can't customize behavior per type
- Use existing channel: Rejected - unclear if existing channel exists, separate channels better UX
- Three+ channels: Rejected - over-categorization, two types sufficient

---

### 4. BroadcastReceiver Hilt Integration

**Research**: Best practices for Hilt dependency injection in BroadcastReceiver

**Findings**:
- Hilt provides `@AndroidEntryPoint` support for BroadcastReceiver
- Base class must extend `HiltBroadcastReceiver` (not standard BroadcastReceiver)
- Constructor injection supported for UseCases and Repositories
- BroadcastReceiver must be declared in AndroidManifest.xml for Hilt to recognize it

**Implementation**:
```kotlin
@AndroidEntryPoint
class NotificationAlarmReceiver : HiltBroadcastReceiver() {
    @Inject lateinit var getLessonWithStudentUseCase: GetLessonWithStudentUseCase
    @Inject lateinit var getCompletedLessonsForDateUseCase: GetCompletedLessonsForDateUseCase

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        // Use goAsync() for coroutine work
    }
}
```

**References**:
- Hilt documentation: BroadcastReceiver injection
- Android best practices: Background work in receivers

---

### 5. AlarmManager Exact Alarm Permissions

**Research**: Android 12+ exact alarm permissions and best practices

**Findings**:
- Android 12 (API 31+) requires `SCHEDULE_EXACT_ALARM` or `USE_EXACT_ALARM` permission
- `USE_EXACT_ALARM` is for user-facing alarms (fits our use case - lesson reminders)
- Declare in AndroidManifest.xml: `<uses-permission android:name="android.permission.USE_EXACT_ALARM" />`
- No runtime permission request needed for `USE_EXACT_ALARM`
- Use `setExactAndAllowWhileIdle()` for delivery even in Doze mode

**Implementation**:
```kotlin
alarmManager.setExactAndAllowWhileIdle(
    AlarmManager.RTC_WAKEUP,
    triggerAtMillis,
    pendingIntent
)
```

**Decision**: Use `USE_EXACT_ALARM` permission with `setExactAndAllowWhileIdle()`

---

### 6. Deep Linking Architecture

**Research**: Navigation Compose deep link patterns with parameters

**Findings**:
- Navigation Compose supports deep links with path parameters
- Define routes with parameters: `"studentCalendar/{studentId}"`
- Create PendingIntent with deep link URI: `app://kora/studentCalendar/123`
- NavHost must have matching deep link declarations

**Implementation**:
```kotlin
// In Navigation Graph
composable(
    route = "studentCalendar/{studentId}",
    deepLinks = listOf(navDeepLink { uriPattern = "app://kora/studentCalendar/{studentId}" })
) { backStackEntry ->
    val studentId = backStackEntry.arguments?.getString("studentId")
    StudentCalendarScreen(studentId)
}

// In BroadcastReceiver
val deepLinkUri = "app://kora/studentCalendar/$studentId".toUri()
val pendingIntent = NavDeepLinkBuilder(context)
    .setGraph(R.navigation.nav_graph)
    .setDestination(R.id.studentCalendarFragment)
    .setArguments(bundleOf("studentId" to studentId))
    .createPendingIntent()
```

**Decision**: Use Navigation Compose deep links with URI patterns for both student calendar and lesson edit screens

---

## Technology Decisions Summary

| Decision Area | Choice | Justification |
|---------------|--------|---------------|
| Settings Access | `SettingsRepository` with DataStore Preferences | Clean Architecture, reactive, testable |
| Alarm Rescheduling | Event-driven (lesson changes + settings changes) | Maintains accuracy, responds to data/config changes |
| Notification Channels | Two separate channels (Reminders + Notes) | User control, distinct purposes, Android best practices |
| BroadcastReceiver DI | Hilt `@AndroidEntryPoint` + `HiltBroadcastReceiver` | Consistent DI, testable, follows app patterns |
| Exact Alarms | `USE_EXACT_ALARM` + `setExactAndAllowWhileIdle()` | Precise delivery, Doze-resistant, permission-friendly |
| Deep Linking | Navigation Compose deep links with URI parameters | Type-safe, integrated with existing navigation |

---

## New Dependencies

None required - all technologies available in existing project dependencies:
- AlarmManager: Android platform API
- NotificationManager: Android platform API
- DataStore Preferences: Likely already present; add if missing: `androidx.datastore:datastore-preferences`
- Hilt BroadcastReceiver support: Included in Hilt Android dependency

---

## Risks and Mitigations

| Risk | Impact | Mitigation |
|------|--------|------------|
| Alarm scheduling fails in restricted battery modes | Users miss notifications | Use `setExactAndAllowWhileIdle()`, request battery optimization exemption if needed |
| Database queries block BroadcastReceiver main thread | ANR crashes | Use `goAsync()` + coroutines for all DB access |
| Lesson data stale at notification time | Wrong notification content | Query fresh data in BroadcastReceiver, don't cache in alarm Intent |
| Settings changes not reflected immediately | Incorrect alarm times | Observe settings Flow, reschedule on change |
| Deep link navigation fails if app state corrupted | Poor UX on notification tap | Test deep links thoroughly, handle missing data gracefully |

---

## Next Steps

With research complete, proceed to:
1. **Phase 1**: Generate data-model.md (minimal - reusing existing entities)
2. **Phase 1**: Generate contracts/ (UseCase interfaces)
3. **Phase 1**: Generate quickstart.md (setup and testing guide)
4. **Phase 1**: Update agent context with notification-specific technologies
