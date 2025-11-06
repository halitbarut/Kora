package com.barutdev.kora.domain.model

/**
 * Enumeration of notification types for the context-aware notification system.
 *
 * Used to differentiate between lesson reminder notifications (morning) and
 * note reminder notifications (evening) when scheduling alarms and building notifications.
 */
enum class NotificationType {
    /**
     * Morning reminder for an upcoming lesson.
     * Sent at the time configured in "Settings -> Lesson reminder time".
     */
    LESSON_REMINDER,

    /**
     * Evening reminder to log notes for a completed lesson.
     * Sent at the time configured in "Settings -> Log reminder time".
     * Only sent for lessons with status == COMPLETED.
     */
    NOTE_REMINDER
}
