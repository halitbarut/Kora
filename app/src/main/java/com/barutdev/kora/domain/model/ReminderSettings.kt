package com.barutdev.kora.domain.model

import java.time.LocalTime

/**
 * User's notification timing preferences.
 *
 * This immutable data class represents the times when the user wants to receive
 * different types of notifications, configured in the Settings screen.
 *
 * @property lessonReminderTime Daily time to send lesson reminder notifications (e.g., 8:00 AM).
 *                              Corresponds to "Settings -> Lesson reminder time" configuration.
 * @property logReminderTime Daily time to send note reminder notifications (e.g., 8:00 PM).
 *                           Corresponds to "Settings -> Log reminder time" configuration.
 */
data class ReminderSettings(
    val lessonReminderTime: LocalTime = LocalTime.of(8, 0),  // Default: 8:00 AM
    val logReminderTime: LocalTime = LocalTime.of(20, 0)      // Default: 8:00 PM
)
