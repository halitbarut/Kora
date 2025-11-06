package com.barutdev.kora.data.notification

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.barutdev.kora.domain.model.NotificationType
import com.barutdev.kora.domain.repository.NotificationBuilder
import com.barutdev.kora.domain.usecase.notification.GetLessonWithStudentUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * BroadcastReceiver that handles notification alarm triggers.
 *
 * When an alarm fires, this receiver:
 * 1. Extracts lesson ID and notification type from Intent extras
 * 2. Fetches fresh lesson and student data via GetLessonWithStudentUseCase
 * 3. Builds the notification via NotificationBuilder
 * 4. Displays the notification via NotificationManager
 *
 * Uses Hilt for dependency injection (@AndroidEntryPoint).
 * Runs notification logic in a coroutine with goAsync() to prevent ANR.
 */
@AndroidEntryPoint
class NotificationAlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var getLessonWithStudentUseCase: GetLessonWithStudentUseCase

    @Inject
    lateinit var notificationBuilder: NotificationBuilder

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        // Log the alarm trigger for debugging
        val requestCode = intent.getIntExtra("request_code", -1)
        Log.d("NotificationDebug", "NotificationAlarmReceiver: onReceive triggered for requestCode: $requestCode")

        // Use goAsync() to allow coroutine work without blocking
        val pendingResult = goAsync()

        scope.launch {
            try {
                // Extract data from intent
                val lessonIdStr = intent.getStringExtra("lesson_id") ?: return@launch
                val lessonId = lessonIdStr.toIntOrNull() ?: return@launch
                val notificationTypeStr = intent.getStringExtra("notification_type") ?: return@launch
                val notificationType = try {
                    NotificationType.valueOf(notificationTypeStr)
                } catch (e: IllegalArgumentException) {
                    return@launch
                }

                // Fetch fresh lesson and student data
                val lessonWithStudent = getLessonWithStudentUseCase(lessonId) ?: return@launch

                // Build notification
                val notification = notificationBuilder.build(notificationType, lessonWithStudent)

                // Display notification
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager
                val notificationId = generateNotificationId(lessonId, notificationType)
                notificationManager.notify(notificationId, notification)
            } finally {
                pendingResult.finish()
            }
        }
    }

    /**
     * Generates a unique notification ID for each lesson-notification type pair.
     * This allows separate notifications for lesson reminders and note reminders.
     */
    private fun generateNotificationId(lessonId: Int, notificationType: NotificationType): Int {
        return lessonId * 100 + notificationType.ordinal
    }
}
