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

@AndroidEntryPoint
class NotificationReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationManager: KoraNotificationManager

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
    }

    companion object {
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
        const val EXTRA_NOTIFICATION_TITLE = "extra_notification_title"
        const val EXTRA_NOTIFICATION_MESSAGE = "extra_notification_message"
        const val EXTRA_LANGUAGE_CODE = "extra_language_code"
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
