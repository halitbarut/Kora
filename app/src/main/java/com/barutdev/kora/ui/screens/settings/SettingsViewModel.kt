package com.barutdev.kora.ui.screens.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barutdev.kora.domain.model.UserPreferences
import com.barutdev.kora.domain.repository.UserPreferencesRepository
import com.barutdev.kora.notifications.AlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Currency
import java.util.Locale
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    companion object {
        private const val TAG = "LanguageSwitchDebug"
        private const val LOG_REMINDER_HOUR = 20
    }

    private val locale = Locale.getDefault()
    private val defaultCurrency = runCatching { Currency.getInstance(locale).currencyCode }
        .getOrDefault("USD")

    private val defaultPreferences = UserPreferences(
        isDarkMode = false,
        languageCode = locale.language,
        currencyCode = defaultCurrency,
        defaultHourlyRate = 0.0,
        lessonRemindersEnabled = false,
        logReminderEnabled = false
    )

    val userPreferences: StateFlow<UserPreferences> = userPreferencesRepository.userPreferences
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = defaultPreferences
        )

    private val _isLanguageDialogVisible = MutableStateFlow(false)
    val isLanguageDialogVisible: StateFlow<Boolean> = _isLanguageDialogVisible

    private val _isCurrencyDialogVisible = MutableStateFlow(false)
    val isCurrencyDialogVisible: StateFlow<Boolean> = _isCurrencyDialogVisible

    private val _isHourlyRateDialogVisible = MutableStateFlow(false)
    val isHourlyRateDialogVisible: StateFlow<Boolean> = _isHourlyRateDialogVisible

    val availableLanguages: List<String> = listOf("en", "tr", "de")
    val availableCurrencies: List<String> = listOf("USD", "EUR", "TRY")

    fun showLanguageDialog() {
        _isLanguageDialogVisible.value = true
    }

    fun dismissLanguageDialog() {
        _isLanguageDialogVisible.value = false
    }

    fun showCurrencyDialog() {
        _isCurrencyDialogVisible.value = true
    }

    fun dismissCurrencyDialog() {
        _isCurrencyDialogVisible.value = false
    }

    fun showHourlyRateDialog() {
        _isHourlyRateDialogVisible.value = true
    }

    fun dismissHourlyRateDialog() {
        _isHourlyRateDialogVisible.value = false
    }

    fun updateDarkMode(isDarkMode: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.updateTheme(isDarkMode)
        }
    }

    fun updateLanguage(languageCode: String) {
        viewModelScope.launch {
            Log.d(TAG, "Attempting to save new language code: $languageCode")
            userPreferencesRepository.updateLanguage(languageCode.lowercase(Locale.ROOT))
            dismissLanguageDialog()
        }
    }

    fun updateCurrency(currencyCode: String) {
        viewModelScope.launch {
            userPreferencesRepository.updateCurrency(currencyCode.uppercase(Locale.ROOT))
            dismissCurrencyDialog()
        }
    }

    fun updateHourlyRate(hourlyRate: Double) {
        viewModelScope.launch {
            userPreferencesRepository.updateDefaultHourlyRate(hourlyRate)
            dismissHourlyRateDialog()
        }
    }

    fun updateLessonRemindersEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.updateLessonRemindersEnabled(isEnabled)
        }
    }

    fun updateLogReminderEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.updateLogReminderEnabled(isEnabled)
            if (isEnabled) {
                val nextTrigger = nextLogReminderTrigger()
                val languageCode = userPreferences.value.languageCode
                alarmScheduler.scheduleDailyLogReminder(nextTrigger, languageCode)
            } else {
                alarmScheduler.cancelDailyLogReminder()
            }
        }
    }

    private fun nextLogReminderTrigger(): Long {
        val zoneId = ZoneId.systemDefault()
        val now = ZonedDateTime.now(zoneId)
        var triggerDateTime = now.withHour(LOG_REMINDER_HOUR)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)
        if (triggerDateTime.isBefore(now)) {
            triggerDateTime = triggerDateTime.plusDays(1)
        }
        return triggerDateTime.toInstant().toEpochMilli()
    }

}
