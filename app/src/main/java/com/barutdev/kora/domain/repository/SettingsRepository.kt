package com.barutdev.kora.domain.repository

import com.barutdev.kora.domain.model.ReminderSettings
import kotlinx.coroutines.flow.Flow
import java.time.LocalTime

/**
 * Repository interface for managing user notification preferences.
 *
 * Provides access to notification timing settings stored in DataStore Preferences.
 * Settings are exposed as reactive Flows that emit updates whenever preferences change.
 */
interface SettingsRepository {
    /**
     * Returns a Flow of reminder settings that emits whenever settings change.
     * Never returns null - provides defaults if not configured.
     *
     * Default values:
     * - lessonReminderTime: 08:00 (8:00 AM)
     * - logReminderTime: 20:00 (8:00 PM)
     *
     * @return Flow<ReminderSettings> Current notification timing preferences
     */
    fun getReminderSettings(): Flow<ReminderSettings>

    /**
     * Updates the time for morning lesson reminders.
     *
     * @param time New lesson reminder time (24-hour format)
     */
    suspend fun updateLessonReminderTime(time: LocalTime)

    /**
     * Updates the time for evening note reminders.
     *
     * @param time New log reminder time (24-hour format)
     */
    suspend fun updateLogReminderTime(time: LocalTime)
}
