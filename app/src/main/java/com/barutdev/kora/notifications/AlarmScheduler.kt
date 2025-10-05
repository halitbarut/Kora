package com.barutdev.kora.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.LocaleList
import android.util.Log
import com.barutdev.kora.R
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject

enum class LessonReminderType(val requestCodeOffset: Int) {
    DAY_BEFORE(1),
    DAY_OF(2)
}

class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val alarmManager: AlarmManager
) {

    fun scheduleLessonReminder(
        lessonId: Int,
        type: LessonReminderType,
        triggerAtMillis: Long,
        studentName: String,
        languageCode: String
    ) {
        if (triggerAtMillis <= System.currentTimeMillis()) {
            return
        }

        val localizedContext = createLocalizedContext(languageCode)

        val title = localizedContext.getString(R.string.notification_lesson_reminder_title)
        val message = when (type) {
            LessonReminderType.DAY_BEFORE ->
                localizedContext.getString(R.string.notification_lesson_reminder_day_before, studentName)
            LessonReminderType.DAY_OF ->
                localizedContext.getString(R.string.notification_lesson_reminder_day_of, studentName)
        }

        val pendingIntent = buildLessonReminderPendingIntent(
            lessonId = lessonId,
            type = type,
            title = title,
            message = message,
            languageCode = languageCode,
            flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            includeContentExtras = true
        ) ?: return

        if (!canScheduleExactAlarms()) {
            Log.e(TAG, "Cannot schedule exact alarms. Permission not granted.")
            return
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
    }

    fun cancelLessonReminder(lessonId: Int, type: LessonReminderType) {
        val pendingIntent = buildLessonReminderPendingIntent(
            lessonId = lessonId,
            type = type,
            title = null,
            message = null,
            flags = PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
            includeContentExtras = false
        )
        pendingIntent?.let { alarmManager.cancel(it) }
    }

    fun cancelAllLessonReminders(lessonId: Int) {
        LessonReminderType.values().forEach { type ->
            cancelLessonReminder(lessonId, type)
        }
    }

    fun scheduleDailyLogReminder(triggerAtMillis: Long, languageCode: String) {
        val localizedContext = createLocalizedContext(languageCode)
        val title = localizedContext.getString(R.string.notification_lesson_reminder_title)
        val message = localizedContext.getString(R.string.notification_log_reminder_message)

        val pendingIntent = buildLogReminderPendingIntent(
            title = title,
            message = message,
            languageCode = languageCode,
            flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            includeContentExtras = true
        ) ?: return

        if (!canScheduleExactAlarms()) {
            Log.e(TAG, "Cannot schedule exact alarms. Permission not granted.")
            return
        }

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    fun cancelDailyLogReminder() {
        val pendingIntent = buildLogReminderPendingIntent(
            title = null,
            message = null,
            languageCode = null,
            flags = PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
            includeContentExtras = false
        )
        pendingIntent?.let { alarmManager.cancel(it) }
    }

    private fun buildLessonReminderPendingIntent(
        lessonId: Int,
        type: LessonReminderType,
        title: String?,
        message: String?,
        languageCode: String? = null,
        flags: Int,
        includeContentExtras: Boolean
    ): PendingIntent? {
        val requestCode = lessonRequestCode(lessonId, type)
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = lessonReminderAction(lessonId, type)
            putExtra(
                NotificationReceiver.EXTRA_NOTIFICATION_ID,
                notificationIdForLesson(lessonId, type)
            )
            if (includeContentExtras) {
                title?.let { putExtra(NotificationReceiver.EXTRA_NOTIFICATION_TITLE, it) }
                message?.let { putExtra(NotificationReceiver.EXTRA_NOTIFICATION_MESSAGE, it) }
                languageCode?.let { putExtra(NotificationReceiver.EXTRA_LANGUAGE_CODE, it) }
            }
        }

        return PendingIntent.getBroadcast(context, requestCode, intent, flags)
    }

    private fun buildLogReminderPendingIntent(
        title: String?,
        message: String?,
        languageCode: String?,
        flags: Int,
        includeContentExtras: Boolean
    ): PendingIntent? {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = ACTION_LOG_REMINDER
            putExtra(NotificationReceiver.EXTRA_NOTIFICATION_ID, LOG_REMINDER_NOTIFICATION_ID)
            if (includeContentExtras) {
                title?.let { putExtra(NotificationReceiver.EXTRA_NOTIFICATION_TITLE, it) }
                message?.let { putExtra(NotificationReceiver.EXTRA_NOTIFICATION_MESSAGE, it) }
                languageCode?.let { putExtra(NotificationReceiver.EXTRA_LANGUAGE_CODE, it) }
            }
        }

        return PendingIntent.getBroadcast(context, LOG_REMINDER_REQUEST_CODE, intent, flags)
    }

    private fun lessonRequestCode(lessonId: Int, type: LessonReminderType): Int {
        return lessonId * LESSON_REQUEST_CODE_MULTIPLIER + type.requestCodeOffset
    }

    private fun notificationIdForLesson(lessonId: Int, type: LessonReminderType): Int {
        return lessonRequestCode(lessonId, type)
    }

    companion object {
        private const val TAG = "AlarmScheduler"
        private const val ACTION_LESSON_REMINDER = "com.barutdev.kora.NOTIFICATION_LESSON_REMINDER"
        private const val ACTION_LOG_REMINDER = "com.barutdev.kora.NOTIFICATION_LOG_REMINDER"
        private const val LESSON_REQUEST_CODE_MULTIPLIER = 10
        private const val LOG_REMINDER_REQUEST_CODE = 100_000
        private const val LOG_REMINDER_NOTIFICATION_ID = 200_000

        private fun lessonReminderAction(lessonId: Int, type: LessonReminderType): String {
            return "$ACTION_LESSON_REMINDER:$lessonId:${type.name}"
        }
    }

    private fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    private fun createLocalizedContext(languageCode: String?): Context {
        if (languageCode.isNullOrBlank()) {
            return context
        }
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val resources = context.resources
        val configuration = resources.configuration
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocales(LocaleList(locale))
            context.createConfigurationContext(configuration)
        } else {
            configuration.setLocale(locale)
            context.createConfigurationContext(configuration)
        }
    }
}
