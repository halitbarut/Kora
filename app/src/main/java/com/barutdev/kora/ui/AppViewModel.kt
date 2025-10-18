package com.barutdev.kora.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barutdev.kora.domain.model.UserPreferences
import com.barutdev.kora.domain.repository.UserPreferencesRepository
import com.barutdev.kora.domain.usecase.InitializeSmartDefaultsUseCase
import com.barutdev.kora.util.SmartLocaleDefaults
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class AppViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val initializeSmartDefaults: InitializeSmartDefaultsUseCase
) : ViewModel() {

    private val smartDefaults = SmartLocaleDefaults.resolveSmartDefaultsFromDevice()

    private val defaultPreferences = UserPreferences(
        isDarkMode = false,
        languageCode = smartDefaults.languageCode,
        currencyCode = smartDefaults.currencyCode,
        defaultHourlyRate = 0.0,
        lessonRemindersEnabled = false,
        logReminderEnabled = false,
        lessonReminderHour = 9,
        lessonReminderMinute = 0,
        logReminderHour = 20,
        logReminderMinute = 0
    )

    val userPreferences: StateFlow<UserPreferences> = userPreferencesRepository.userPreferences
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = defaultPreferences
        )

    init {
        viewModelScope.launch {
            initializeSmartDefaults()
        }
    }
}
