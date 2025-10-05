package com.barutdev.kora.ui.preferences

import androidx.compose.runtime.staticCompositionLocalOf
import com.barutdev.kora.domain.model.UserPreferences

val LocalUserPreferences = staticCompositionLocalOf {
    UserPreferences(
        isDarkMode = false,
        languageCode = "en",
        currencyCode = "USD",
        defaultHourlyRate = 0.0,
        lessonRemindersEnabled = false,
        logReminderEnabled = false
    )
}
