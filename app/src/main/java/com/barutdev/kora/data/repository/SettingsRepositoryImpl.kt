package com.barutdev.kora.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.barutdev.kora.domain.model.ReminderSettings
import com.barutdev.kora.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of SettingsRepository using DataStore Preferences.
 *
 * Manages user notification timing preferences with reactive updates via Flow.
 * Provides default values if preferences are not configured or parsing fails.
 *
 * NOTE: This reads from the same DataStore keys as UserPreferencesRepository to ensure
 * that changes made in the Settings UI are properly reflected in notification scheduling.
 */
@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    companion object {
        // Use the same keys as UserPreferencesRepository to read user settings
        private val LESSON_REMINDER_HOUR_KEY = intPreferencesKey("lesson_reminder_hour")
        private val LESSON_REMINDER_MINUTE_KEY = intPreferencesKey("lesson_reminder_minute")
        private val LOG_REMINDER_HOUR_KEY = intPreferencesKey("log_reminder_hour")
        private val LOG_REMINDER_MINUTE_KEY = intPreferencesKey("log_reminder_minute")

        private const val DEFAULT_LESSON_REMINDER_HOUR = 9
        private const val DEFAULT_REMINDER_MINUTE = 0
        private const val DEFAULT_LOG_REMINDER_HOUR = 20
    }

    override fun getReminderSettings(): Flow<ReminderSettings> {
        return dataStore.data.map { preferences ->
            val lessonHour = preferences[LESSON_REMINDER_HOUR_KEY] ?: DEFAULT_LESSON_REMINDER_HOUR
            val lessonMinute = preferences[LESSON_REMINDER_MINUTE_KEY] ?: DEFAULT_REMINDER_MINUTE
            val logHour = preferences[LOG_REMINDER_HOUR_KEY] ?: DEFAULT_LOG_REMINDER_HOUR
            val logMinute = preferences[LOG_REMINDER_MINUTE_KEY] ?: DEFAULT_REMINDER_MINUTE

            ReminderSettings(
                lessonReminderTime = LocalTime.of(lessonHour, lessonMinute),
                logReminderTime = LocalTime.of(logHour, logMinute)
            )
        }
    }

    override suspend fun updateLessonReminderTime(time: LocalTime) {
        dataStore.edit { preferences ->
            preferences[LESSON_REMINDER_HOUR_KEY] = time.hour
            preferences[LESSON_REMINDER_MINUTE_KEY] = time.minute
        }
    }

    override suspend fun updateLogReminderTime(time: LocalTime) {
        dataStore.edit { preferences ->
            preferences[LOG_REMINDER_HOUR_KEY] = time.hour
            preferences[LOG_REMINDER_MINUTE_KEY] = time.minute
        }
    }
}
