package com.barutdev.kora.domain.repository

import android.app.Notification
import com.barutdev.kora.domain.model.LessonWithStudent
import com.barutdev.kora.domain.model.NotificationType

/**
 * Repository interface for building Android notifications.
 *
 * Abstracts notification construction logic including channel management,
 * content formatting, and deep linking setup. Handles both lesson reminder
 * and note reminder notification types.
 */
interface NotificationBuilder {
    /**
     * Builds a notification for display.
     *
     * Creates a NotificationCompat notification with:
     * - Appropriate channel (lesson reminders or lesson notes)
     * - Formatted title and content based on notification type
     * - Deep link PendingIntent for navigation
     * - High priority for visibility
     * - Auto-cancel on tap
     *
     * @param type Type of notification (lesson reminder or note reminder)
     * @param lessonWithStudent Lesson and student data for content formatting
     * @return Notification ready for display via NotificationManager
     */
    fun build(
        type: NotificationType,
        lessonWithStudent: LessonWithStudent
    ): Notification
}
