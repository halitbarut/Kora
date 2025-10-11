package com.barutdev.kora.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.LocaleList
import com.barutdev.kora.R
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject
import java.time.ZonedDateTime
import java.time.ZoneId

@AndroidEntryPoint
class NotificationReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationManager: KoraNotificationManager

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    override fun onReceive(context: Context, intent: Intent?) {
        val fallbackNotificationId = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
        val notificationId = intent?.getIntExtra(EXTRA_NOTIFICATION_ID, fallbackNotificationId)
            ?: fallbackNotificationId

        val languageCode = intent?.getStringExtra(EXTRA_LANGUAGE_CODE)
        val localizedContext = languageCode?.takeIf { it.isNotBlank() }
            ?.let { createLocalizedContext(context, it) }
            ?: context

        val title = intent?.getStringExtra(EXTRA_NOTIFICATION_TITLE)
            ?: localizedContext.getString(R.string.app_name)
        val message = intent?.getStringExtra(EXTRA_NOTIFICATION_MESSAGE)
            ?: localizedContext.getString(R.string.notification_default_message)

        val notification = notificationManager.buildNotification(
            title = title,
            message = message,
            context = localizedContext
        )
        notificationManager.notify(notificationId, notification)

        if (intent?.action == ACTION_LOG_REMINDER) {
            val languageCode = intent.getStringExtra(EXTRA_LANGUAGE_CODE)
            val hour = intent?.getIntExtra(EXTRA_REMINDER_HOUR, DEFAULT_REMINDER_HOUR) ?: DEFAULT_REMINDER_HOUR
            val minute = intent?.getIntExtra(EXTRA_REMINDER_MINUTE, DEFAULT_REMINDER_MINUTE) ?: DEFAULT_REMINDER_MINUTE
            val zoneId = ZoneId.systemDefault()
            val nextTrigger = ZonedDateTime.now(zoneId)
                .plusDays(1)
                .withHour(hour)
                .withMinute(minute)
                .withSecond(0)
                .withNano(0)
                .toInstant()
                .toEpochMilli()
            alarmScheduler.scheduleDailyLogReminder(
                triggerAtMillis = nextTrigger,
                languageCode = languageCode ?: Locale.getDefault().language,
                hour = hour,
                minute = minute
            )
        }
    }

    companion object {
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
        const val EXTRA_NOTIFICATION_TITLE = "extra_notification_title"
        const val EXTRA_NOTIFICATION_MESSAGE = "extra_notification_message"
        const val EXTRA_LANGUAGE_CODE = "extra_language_code"
        const val EXTRA_REMINDER_HOUR = "extra_reminder_hour"
        const val EXTRA_REMINDER_MINUTE = "extra_reminder_minute"
        private const val DEFAULT_REMINDER_HOUR = 20
        private const val DEFAULT_REMINDER_MINUTE = 0
        private const val ACTION_LOG_REMINDER = "com.barutdev.kora.NOTIFICATION_LOG_REMINDER"
    }

    private fun createLocalizedContext(baseContext: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val resources = baseContext.resources
        val configuration = resources.configuration
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocales(LocaleList(locale))
            baseContext.createConfigurationContext(configuration)
        } else {
            configuration.setLocale(locale)
            baseContext.createConfigurationContext(configuration)
        }
    }
}
