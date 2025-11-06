package com.barutdev.kora.domain.model

import java.time.LocalDateTime

/**
 * Represents a request to schedule an alarm for a notification.
 *
 * This immutable data class is passed to AlarmScheduler to schedule exact-time
 * alarms using AlarmManager. Contains all information needed to create a unique
 * PendingIntent and schedule the alarm at the correct time.
 *
 * @property lessonId Unique identifier used for alarm cancellation and request code calculation
 * @property notificationType Type of notification (lesson reminder or note reminder)
 * @property triggerTime Exact date and time when the alarm should trigger
 * @property notificationData Data to serialize into Intent extras for BroadcastReceiver
 */
data class AlarmScheduleRequest(
    val lessonId: String,
    val notificationType: NotificationType,
    val triggerTime: LocalDateTime,
    val notificationData: NotificationData
)
