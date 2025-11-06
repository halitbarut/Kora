package com.barutdev.kora.data.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import com.barutdev.kora.R
import com.barutdev.kora.domain.model.LessonWithStudent
import com.barutdev.kora.domain.model.NotificationType
import com.barutdev.kora.domain.repository.NotificationBuilder
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Builds Android notifications for lesson reminders and note reminders.
 *
 * Creates notification channels on initialization and provides formatted notifications
 * with deep links to the student calendar screen. Uses NotificationCompat for
 * backward compatibility and Material Design styling.
 */
@Singleton
class NotificationBuilderImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : NotificationBuilder {

    companion object {
        private const val CHANNEL_LESSON_REMINDERS = "lesson_reminders"
        private const val CHANNEL_LESSON_NOTES = "lesson_notes"
        private const val DEEP_LINK_BASE = "app://kora/studentCalendar"
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannels()
    }

    override fun build(
        type: NotificationType,
        lessonWithStudent: LessonWithStudent
    ): Notification {
        return when (type) {
            NotificationType.LESSON_REMINDER -> buildLessonReminder(lessonWithStudent)
            NotificationType.NOTE_REMINDER -> buildNoteReminder(lessonWithStudent)
        }
    }

    /**
     * Builds a lesson reminder notification with student name and lesson time.
     * Deep links to student calendar screen on tap.
     */
    private fun buildLessonReminder(lessonWithStudent: LessonWithStudent): Notification {
        val lesson = lessonWithStudent.lesson
        val student = lessonWithStudent.student
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        // Convert timestamp to LocalTime
        val lessonTime = java.time.Instant.ofEpochMilli(lesson.date)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalTime()

        val title = context.getString(
            R.string.notification_lesson_reminder_title_new,
            student.fullName
        )
        val content = context.getString(
            R.string.notification_lesson_reminder_content,
            lessonTime.format(timeFormatter)
        )

        val deepLinkIntent = createDeepLinkIntent(student.id)

        return NotificationCompat.Builder(context, CHANNEL_LESSON_REMINDERS)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // TODO: Replace with proper notification icon
            .setContentTitle(title)
            .setContentText(content)
            .setContentIntent(deepLinkIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
    }

    /**
     * Builds a note reminder notification prompting to log lesson notes.
     * Deep links to student calendar screen on tap.
     */
    private fun buildNoteReminder(lessonWithStudent: LessonWithStudent): Notification {
        val lesson = lessonWithStudent.lesson
        val student = lessonWithStudent.student
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        // Convert timestamp to LocalTime
        val lessonTime = java.time.Instant.ofEpochMilli(lesson.date)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalTime()

        val title = context.getString(
            R.string.notification_note_reminder_title,
            student.fullName
        )
        val content = context.getString(
            R.string.notification_note_reminder_content,
            lessonTime.format(timeFormatter)
        )

        val deepLinkIntent = createDeepLinkIntent(student.id)

        return NotificationCompat.Builder(context, CHANNEL_LESSON_NOTES)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // TODO: Replace with proper notification icon
            .setContentTitle(title)
            .setContentText(content)
            .setContentIntent(deepLinkIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
    }

    /**
     * Creates a PendingIntent for deep linking to student calendar screen.
     * Format: app://kora/studentCalendar/{studentId}
     *
     * The Intent explicitly sets the component to MainActivity to ensure the app
     * launches correctly even when not running. The deep link URI is preserved
     * so Navigation Compose can handle routing to the correct screen.
     */
    private fun createDeepLinkIntent(studentId: Int): PendingIntent {
        val deepLinkUri = Uri.parse("$DEEP_LINK_BASE/$studentId")
        val intent = Intent(Intent.ACTION_VIEW, deepLinkUri).apply {
            setClassName(context.packageName, "com.barutdev.kora.MainActivity")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        return PendingIntent.getActivity(
            context,
            studentId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Creates notification channels for lesson reminders and note reminders.
     * Called on initialization to ensure channels exist before notifications are posted.
     */
    private fun createNotificationChannels() {
        val lessonReminderChannel = NotificationChannel(
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

        notificationManager.createNotificationChannel(lessonReminderChannel)
        notificationManager.createNotificationChannel(lessonNotesChannel)
    }
}
