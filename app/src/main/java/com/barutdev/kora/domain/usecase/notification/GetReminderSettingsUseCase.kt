package com.barutdev.kora.domain.usecase.notification

import android.util.Log
import com.barutdev.kora.domain.model.ReminderSettings
import com.barutdev.kora.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

/**
 * UseCase for retrieving notification reminder settings.
 *
 * Returns a Flow of ReminderSettings containing:
 * - lessonReminderTime: Time for morning lesson reminders
 * - logReminderTime: Time for evening note reminders
 *
 * Used by ScheduleNotificationAlarmsUseCase to calculate alarm trigger times.
 */
class GetReminderSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(): Flow<ReminderSettings> {
        return settingsRepository.getReminderSettings().onEach { settings ->
            Log.d("NotificationDebug", "GetReminderSettingsUseCase: Returning lesson reminder time: ${settings.lessonReminderTime}, log reminder time: ${settings.logReminderTime}")
        }
    }
}
