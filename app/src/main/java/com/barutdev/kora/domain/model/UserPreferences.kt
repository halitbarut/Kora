package com.barutdev.kora.domain.model

data class UserPreferences(
    val isDarkMode: Boolean,
    val languageCode: String,
    val currencyCode: String,
    val defaultHourlyRate: Double,
    val lessonRemindersEnabled: Boolean,
    val logReminderEnabled: Boolean,
    val lessonReminderHour: Int,
    val lessonReminderMinute: Int,
    val logReminderHour: Int,
    val logReminderMinute: Int
)
