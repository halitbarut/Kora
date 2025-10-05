package com.barutdev.kora.data.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.barutdev.kora.domain.model.UserPreferences
import com.barutdev.kora.domain.repository.UserPreferencesRepository
import java.util.Currency
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
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
    }

    override val userPreferences: Flow<UserPreferences> = dataStore.data
        .catch {
            emit(emptyPreferences())
        }
        .map { preferences ->
            val locale = Locale.getDefault()
            val defaultCurrency = runCatching { Currency.getInstance(locale).currencyCode }
                .getOrDefault("USD")
            UserPreferences(
                isDarkMode = preferences[DARK_MODE_KEY] ?: false,
                languageCode = preferences[LANGUAGE_KEY] ?: locale.language,
                currencyCode = preferences[CURRENCY_KEY] ?: defaultCurrency,
                defaultHourlyRate = preferences[DEFAULT_HOURLY_RATE_KEY] ?: 0.0,
                lessonRemindersEnabled = preferences[LESSON_REMINDERS_ENABLED_KEY] ?: false,
                logReminderEnabled = preferences[LOG_REMINDER_ENABLED_KEY] ?: false
            )
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
}
