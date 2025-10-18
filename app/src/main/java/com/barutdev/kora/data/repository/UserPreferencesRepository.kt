package com.barutdev.kora.data.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.barutdev.kora.domain.model.UserPreferences
import com.barutdev.kora.domain.repository.UserPreferencesRepository
import com.barutdev.kora.util.SmartLocaleDefaults
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class UserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : UserPreferencesRepository {

    private companion object {
        const val TAG = "LanguageSwitchDebug"
        val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        val LANGUAGE_KEY = stringPreferencesKey("language")
        val CURRENCY_KEY = stringPreferencesKey("currency")
        val DEFAULT_HOURLY_RATE_KEY = doublePreferencesKey("default_hourly_rate")
        val LESSON_REMINDERS_ENABLED_KEY = booleanPreferencesKey("lesson_reminders_enabled")
        val LOG_REMINDER_ENABLED_KEY = booleanPreferencesKey("log_reminder_enabled")
        val LESSON_REMINDER_HOUR_KEY = intPreferencesKey("lesson_reminder_hour")
        val LESSON_REMINDER_MINUTE_KEY = intPreferencesKey("lesson_reminder_minute")
        val LOG_REMINDER_HOUR_KEY = intPreferencesKey("log_reminder_hour")
        val LOG_REMINDER_MINUTE_KEY = intPreferencesKey("log_reminder_minute")
        val FIRST_RUN_COMPLETED_KEY = booleanPreferencesKey("first_run_completed")
        const val DEFAULT_LESSON_REMINDER_HOUR = 9
        const val DEFAULT_REMINDER_MINUTE = 0
        const val DEFAULT_LOG_REMINDER_HOUR = 20
    }

    override val userPreferences: Flow<UserPreferences> = dataStore.data
        .catch {
            emit(emptyPreferences())
        }
        .map { preferences ->
            val defaults = SmartLocaleDefaults.resolveSmartDefaultsFromDevice()
            UserPreferences(
                isDarkMode = preferences[DARK_MODE_KEY] ?: false,
                languageCode = preferences[LANGUAGE_KEY] ?: defaults.languageCode,
                currencyCode = preferences[CURRENCY_KEY] ?: defaults.currencyCode,
                defaultHourlyRate = preferences[DEFAULT_HOURLY_RATE_KEY] ?: 0.0,
                lessonRemindersEnabled = preferences[LESSON_REMINDERS_ENABLED_KEY] ?: false,
                logReminderEnabled = preferences[LOG_REMINDER_ENABLED_KEY] ?: false,
                lessonReminderHour = preferences[LESSON_REMINDER_HOUR_KEY] ?: DEFAULT_LESSON_REMINDER_HOUR,
                lessonReminderMinute = preferences[LESSON_REMINDER_MINUTE_KEY] ?: DEFAULT_REMINDER_MINUTE,
                logReminderHour = preferences[LOG_REMINDER_HOUR_KEY] ?: DEFAULT_LOG_REMINDER_HOUR,
                logReminderMinute = preferences[LOG_REMINDER_MINUTE_KEY] ?: DEFAULT_REMINDER_MINUTE
            )
        }

    // First-run helpers
    override suspend fun isFirstRunCompleted(): Boolean {
        val prefs = dataStore.data.first()
        return prefs[FIRST_RUN_COMPLETED_KEY] ?: false
    }

    override suspend fun setFirstRunCompleted() {
        dataStore.edit { preferences ->
            preferences[FIRST_RUN_COMPLETED_KEY] = true
        }
    }

    override suspend fun getSavedLanguageOrNull(): String? {
        val prefs = dataStore.data.first()
        return prefs[LANGUAGE_KEY]
    }

    override suspend fun getSavedCurrencyOrNull(): String? {
        val prefs = dataStore.data.first()
        return prefs[CURRENCY_KEY]
    }

    override suspend fun updateTheme(isDarkMode: Boolean) {
        dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = isDarkMode
        }
    }

    override suspend fun updateLanguage(languageCode: String) {
        dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = languageCode
        }
        Log.d(TAG, "Successfully saved language code to DataStore: $languageCode")
    }

    override suspend fun updateCurrency(currencyCode: String) {
        dataStore.edit { preferences ->
            preferences[CURRENCY_KEY] = currencyCode
        }
    }

    override suspend fun updateDefaultHourlyRate(hourlyRate: Double) {
        dataStore.edit { preferences ->
            preferences[DEFAULT_HOURLY_RATE_KEY] = hourlyRate
        }
    }

    override suspend fun updateLessonRemindersEnabled(isEnabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[LESSON_REMINDERS_ENABLED_KEY] = isEnabled
        }
    }

    override suspend fun updateLogReminderEnabled(isEnabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[LOG_REMINDER_ENABLED_KEY] = isEnabled
        }
    }

    override suspend fun updateLessonReminderTime(hour: Int, minute: Int) {
        dataStore.edit { preferences ->
            preferences[LESSON_REMINDER_HOUR_KEY] = hour
            preferences[LESSON_REMINDER_MINUTE_KEY] = minute
        }
    }

    override suspend fun updateLogReminderTime(hour: Int, minute: Int) {
        dataStore.edit { preferences ->
            preferences[LOG_REMINDER_HOUR_KEY] = hour
            preferences[LOG_REMINDER_MINUTE_KEY] = minute
        }
    }

    override suspend fun resetPreferences() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
