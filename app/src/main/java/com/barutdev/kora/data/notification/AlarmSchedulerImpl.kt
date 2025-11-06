package com.barutdev.kora.data.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.barutdev.kora.domain.model.AlarmScheduleRequest
import com.barutdev.kora.domain.model.NotificationType
import com.barutdev.kora.domain.repository.AlarmScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AlarmManager implementation of AlarmScheduler.
 *
 * Uses setExactAndAllowWhileIdle() for precise alarm delivery even in Doze mode.
 * Creates unique PendingIntents per lesson and notification type to prevent conflicts.
 *
 * Request code format: lessonId.hashCode() + notificationType.ordinal
 * This ensures each lesson can have separate alarms for LESSON_REMINDER and NOTE_REMINDER.
 */
@Singleton
class AlarmSchedulerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AlarmScheduler {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override suspend fun scheduleAlarm(request: AlarmScheduleRequest) {
        val triggerTimeMillis = request.triggerTime
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val pendingIntent = createPendingIntent(
            lessonId = request.lessonId,
            notificationType = request.notificationType,
            notificationData = request.notificationData
        )

        // Use setExactAndAllowWhileIdle for Doze compatibility
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTimeMillis,
            pendingIntent
        )
    }

    override suspend fun cancelAlarm(lessonId: String, type: NotificationType) {
        val requestCode = generateRequestCode(lessonId, type)
        val intent = Intent(context, NotificationAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        // Cancel if alarm exists
        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
    }

    override suspend fun cancelAllAlarms() {
        // Note: This is a simplified implementation.
        // A production implementation would track active alarms or iterate through all lessons.
        // For now, this serves as a placeholder for the interface contract.
    }

    /**
     * Creates a PendingIntent for NotificationAlarmReceiver with notification data.
     * Uses FLAG_UPDATE_CURRENT to update existing alarms with same request code.
     */
    private fun createPendingIntent(
        lessonId: String,
        notificationType: NotificationType,
        notificationData: com.barutdev.kora.domain.model.NotificationData
    ): PendingIntent {
        val requestCode = generateRequestCode(lessonId, notificationType)
        val intent = Intent(context, NotificationAlarmReceiver::class.java).apply {
            putExtra("lesson_id", notificationData.lessonId)
            putExtra("student_id", notificationData.studentId)
            putExtra("student_name", notificationData.studentName)
            putExtra("lesson_time", notificationData.lessonTime.toString())
            putExtra("lesson_date", notificationData.lessonDate.toString())
            putExtra("notification_type", notificationType.name)
        }

        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Generates a unique request code for each lesson-notification pair.
     * Format: lessonId.hashCode() + notificationType.ordinal
     */
    private fun generateRequestCode(lessonId: String, notificationType: NotificationType): Int {
        return lessonId.hashCode() + notificationType.ordinal
    }
}
