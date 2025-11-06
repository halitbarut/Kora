package com.barutdev.kora.domain.repository

import com.barutdev.kora.domain.model.AlarmScheduleRequest
import com.barutdev.kora.domain.model.NotificationType

/**
 * Repository interface abstracting AlarmManager operations.
 *
 * Provides Clean Architecture abstraction over Android's AlarmManager for
 * scheduling exact-time notification alarms. Uses setExactAndAllowWhileIdle()
 * for precise delivery even in Doze mode.
 */
interface AlarmScheduler {
    /**
     * Schedules an exact alarm for a notification.
     * Uses setExactAndAllowWhileIdle() for Doze compatibility.
     *
     * Creates a unique PendingIntent with request code based on lesson ID and notification type.
     * If an alarm with the same request code exists, it will be updated.
     *
     * @param request Alarm scheduling details including trigger time and notification data
     */
    suspend fun scheduleAlarm(request: AlarmScheduleRequest)

    /**
     * Cancels a specific alarm by lesson and notification type.
     *
     * Calculates the request code from lesson ID and type, then cancels the
     * associated PendingIntent. Idempotent - safe to call even if alarm doesn't exist.
     *
     * @param lessonId Lesson identifier
     * @param type Notification type (lesson reminder or note reminder)
     */
    suspend fun cancelAlarm(lessonId: String, type: NotificationType)

    /**
     * Cancels all notification alarms.
     * Used when rescheduling all alarms or during testing cleanup.
     *
     * Note: Implementation depends on tracking active alarms or using known patterns.
     */
    suspend fun cancelAllAlarms()
}
