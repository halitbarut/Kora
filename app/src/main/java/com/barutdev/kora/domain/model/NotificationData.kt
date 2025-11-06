package com.barutdev.kora.domain.model

import java.time.LocalDate
import java.time.LocalTime

/**
 * Encapsulates all data needed to build and display a notification.
 *
 * This immutable data class is built by UseCases from Lesson + Student entities
 * and serialized into Intent extras for the BroadcastReceiver.
 *
 * @property type Which notification to display (lesson reminder vs note reminder)
 * @property lessonId Lesson identifier for deep linking to lesson edit screen
 * @property studentId Student identifier for deep linking to student calendar
 * @property studentName Student's name for notification title formatting
 * @property lessonTime Time of the lesson for notification content formatting
 * @property lessonDate Date of the lesson for context
 */
data class NotificationData(
    val type: NotificationType,
    val lessonId: String,
    val studentId: String,
    val studentName: String,
    val lessonTime: LocalTime,
    val lessonDate: LocalDate
) {
    init {
        require(studentName.isNotBlank()) { "Student name cannot be blank" }
    }
}
