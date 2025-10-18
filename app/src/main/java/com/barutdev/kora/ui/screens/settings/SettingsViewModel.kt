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
import com.barutdev.kora.notifications.AlarmScheduler
import com.barutdev.kora.notifications.LessonReminderType
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
    private val alarmScheduler: AlarmScheduler,
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
            if (isEnabled) {
                val nextTrigger = nextLogReminderTrigger()
                val preferences = userPreferences.value
                alarmScheduler.scheduleDailyLogReminder(
                    triggerAtMillis = nextTrigger,
                    languageCode = preferences.languageCode,
                    hour = preferences.logReminderHour,
                    minute = preferences.logReminderMinute
                )
            } else {
                alarmScheduler.cancelDailyLogReminder()
            }
        }
    }

    fun updateLessonReminderTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            userPreferencesRepository.updateLessonReminderTime(hour, minute)
            if (userPreferences.value.lessonRemindersEnabled) {
                rescheduleAllLessonReminders(hour, minute, userPreferences.value.languageCode)
            }
        }
    }

    fun updateLogReminderTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            userPreferencesRepository.updateLogReminderTime(hour, minute)
            if (userPreferences.value.logReminderEnabled) {
                alarmScheduler.cancelDailyLogReminder()
                val languageCode = userPreferences.value.languageCode
                val triggerAt = nextLogReminderTrigger(hour, minute)
                alarmScheduler.scheduleDailyLogReminder(
                    triggerAtMillis = triggerAt,
                    languageCode = languageCode,
                    hour = hour,
                    minute = minute
                )
            }
        }
    }

    suspend fun exportCsv(): Result<String> = runCatching {
        dataBackupManager.exportToCsv()
    }

    suspend fun importCsv(csv: String): Result<Unit> = runCatching {
        val existingLessons = lessonRepository.getAllLessons().first()
        existingLessons.forEach { lesson ->
            alarmScheduler.cancelAllLessonReminders(lesson.id)
        }
        dataBackupManager.importFromCsv(csv)
        val preferences = userPreferences.value
        if (preferences.lessonRemindersEnabled) {
            rescheduleAllLessonReminders(
                hour = preferences.lessonReminderHour,
                minute = preferences.lessonReminderMinute,
                languageCode = preferences.languageCode
            )
        }
        if (preferences.logReminderEnabled) {
            alarmScheduler.cancelDailyLogReminder()
            val triggerAt = nextLogReminderTrigger(
                hour = preferences.logReminderHour,
                minute = preferences.logReminderMinute
            )
            alarmScheduler.scheduleDailyLogReminder(
                triggerAtMillis = triggerAt,
                languageCode = preferences.languageCode,
                hour = preferences.logReminderHour,
                minute = preferences.logReminderMinute
            )
        }
    }

    suspend fun resetAllData(): Result<Unit> = runCatching {
        val lessons = lessonRepository.getAllLessons().first()
        lessons.forEach { lesson ->
            alarmScheduler.cancelAllLessonReminders(lesson.id)
        }
        alarmScheduler.cancelDailyLogReminder()
        dataBackupManager.clearAllData()
        userPreferencesRepository.resetPreferences()
    }

    private fun nextLogReminderTrigger(): Long {
        val preferences = userPreferences.value
        return nextLogReminderTrigger(preferences.logReminderHour, preferences.logReminderMinute)
    }

    private fun nextLogReminderTrigger(hour: Int, minute: Int): Long {
        val zoneId = ZoneId.systemDefault()
        val now = ZonedDateTime.now(zoneId)
        var triggerDateTime = now.withHour(hour)
            .withMinute(minute)
            .withSecond(0)
            .withNano(0)
        if (triggerDateTime.isBefore(now)) {
            triggerDateTime = triggerDateTime.plusDays(1)
        }
        return triggerDateTime.toInstant().toEpochMilli()
    }

    private suspend fun rescheduleAllLessonReminders(
        hour: Int,
        minute: Int,
        languageCode: String
    ) {
        val lessons = lessonRepository.getAllLessons().first()
        if (lessons.isEmpty()) return
        val students = studentRepository.getAllStudents().first()
            .associateBy { it.id }
        val zoneId = ZoneId.systemDefault()
        lessons.filter { it.status == LessonStatus.SCHEDULED }.forEach { lesson ->
            alarmScheduler.cancelAllLessonReminders(lesson.id)
            val studentName = students[lesson.studentId]?.fullName ?: return@forEach
            val lessonDateTime = Instant.ofEpochMilli(lesson.date).atZone(zoneId)
            val dayOfTrigger = lessonDateTime
                .withHour(hour)
                .withMinute(minute)
                .withSecond(0)
                .withNano(0)
                .toInstant()
                .toEpochMilli()
            alarmScheduler.scheduleLessonReminder(
                lessonId = lesson.id,
                type = LessonReminderType.DAY_OF,
                triggerAtMillis = dayOfTrigger,
                studentName = studentName,
                languageCode = languageCode
            )
            val dayBeforeTrigger = lessonDateTime.minusDays(1)
                .withHour(hour)
                .withMinute(minute)
                .withSecond(0)
                .withNano(0)
                .toInstant()
                .toEpochMilli()
            alarmScheduler.scheduleLessonReminder(
                lessonId = lesson.id,
                type = LessonReminderType.DAY_BEFORE,
                triggerAtMillis = dayBeforeTrigger,
                studentName = studentName,
                languageCode = languageCode
            )
        }
    }

}
