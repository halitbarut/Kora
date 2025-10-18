package com.barutdev.kora.domain.repository

import com.barutdev.kora.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {

    val userPreferences: Flow<UserPreferences>

    // First-run helpers
    suspend fun isFirstRunCompleted(): Boolean
    suspend fun setFirstRunCompleted()
    suspend fun getSavedLanguageOrNull(): String?
    suspend fun getSavedCurrencyOrNull(): String?

    suspend fun updateTheme(isDarkMode: Boolean)

    suspend fun updateLanguage(languageCode: String)

    suspend fun updateCurrency(currencyCode: String)

    suspend fun updateDefaultHourlyRate(hourlyRate: Double)

    suspend fun updateLessonRemindersEnabled(isEnabled: Boolean)

    suspend fun updateLogReminderEnabled(isEnabled: Boolean)

    suspend fun updateLessonReminderTime(hour: Int, minute: Int)

    suspend fun updateLogReminderTime(hour: Int, minute: Int)

    suspend fun resetPreferences()
}
