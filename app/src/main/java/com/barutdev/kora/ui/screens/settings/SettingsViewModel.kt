package com.barutdev.kora.ui.screens.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barutdev.kora.domain.model.UserPreferences
import com.barutdev.kora.data.backup.DataBackupManager
import com.barutdev.kora.domain.model.LessonStatus
import com.barutdev.kora.domain.repository.LessonRepository
import com.barutdev.kora.domain.repository.StudentRepository
import com.barutdev.kora.domain.repository.UserPreferencesRepository
import com.barutdev.kora.domain.usecase.notification.RescheduleAllNotificationAlarmsUseCase
import com.barutdev.kora.util.SmartLocaleDefaults
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val rescheduleAllNotificationAlarmsUseCase: RescheduleAllNotificationAlarmsUseCase,
    private val dataBackupManager: DataBackupManager,
    private val lessonRepository: LessonRepository,
    private val studentRepository: StudentRepository
) : ViewModel() {

    companion object {
        private const val TAG = "LanguageSwitchDebug"
        private const val DEFAULT_LESSON_REMINDER_HOUR = 9
        private const val DEFAULT_REMINDER_MINUTE = 0
        private const val DEFAULT_LOG_REMINDER_HOUR = 20
    }

    private val smartDefaults = SmartLocaleDefaults.resolveSmartDefaultsFromDevice()

    private val defaultPreferences = UserPreferences(
        isDarkMode = false,
        languageCode = smartDefaults.languageCode,
        currencyCode = smartDefaults.currencyCode,
        defaultHourlyRate = 0.0,
        lessonRemindersEnabled = false,
        logReminderEnabled = false,
        lessonReminderHour = DEFAULT_LESSON_REMINDER_HOUR,
        lessonReminderMinute = DEFAULT_REMINDER_MINUTE,
        logReminderHour = DEFAULT_LOG_REMINDER_HOUR,
        logReminderMinute = DEFAULT_REMINDER_MINUTE
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
            rescheduleAllNotificationAlarmsUseCase()
        }
    }

    fun updateLessonReminderTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            userPreferencesRepository.updateLessonReminderTime(hour, minute)
            rescheduleAllNotificationAlarmsUseCase()
        }
    }

    fun updateLogReminderTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            userPreferencesRepository.updateLogReminderTime(hour, minute)
            rescheduleAllNotificationAlarmsUseCase()
        }
    }

    suspend fun exportCsv(): Result<String> = runCatching {
        dataBackupManager.exportToCsv()
    }

    suspend fun importCsv(csv: String): Result<Unit> = runCatching {
        dataBackupManager.importFromCsv(csv)
        rescheduleAllNotificationAlarmsUseCase()
    }

    suspend fun resetAllData(): Result<Unit> = runCatching {
        dataBackupManager.clearAllData()
        userPreferencesRepository.resetPreferences()
        // All alarms are automatically cancelled when lessons are deleted
    }

}
