package com.barutdev.kora.data.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.barutdev.kora.domain.usecase.notification.RescheduleAllNotificationAlarmsUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * BroadcastReceiver that handles device boot completion.
 *
 * When the device reboots, all AlarmManager alarms are cleared by the system.
 * This receiver restores all notification alarms by:
 * 1. Receiving BOOT_COMPLETED broadcast
 * 2. Calling RescheduleAllNotificationAlarmsUseCase to reschedule all active lesson alarms
 *
 * Requires RECEIVE_BOOT_COMPLETED permission in AndroidManifest.xml.
 * Uses Hilt for dependency injection (@AndroidEntryPoint).
 */
@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {

    @Inject
    lateinit var rescheduleAllNotificationAlarmsUseCase: RescheduleAllNotificationAlarmsUseCase

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Use goAsync() to allow coroutine work without blocking
            val pendingResult = goAsync()

            scope.launch {
                try {
                    rescheduleAllNotificationAlarmsUseCase()
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
