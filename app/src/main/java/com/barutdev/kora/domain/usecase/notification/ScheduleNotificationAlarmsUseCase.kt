package com.barutdev.kora.domain.usecase.notification

import android.util.Log
import com.barutdev.kora.domain.model.AlarmScheduleRequest
import com.barutdev.kora.domain.model.Lesson
import com.barutdev.kora.domain.model.NotificationData
import com.barutdev.kora.domain.model.NotificationType
import com.barutdev.kora.domain.repository.AlarmScheduler
import com.barutdev.kora.domain.repository.LessonRepository
import com.barutdev.kora.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject

/**
 * UseCase for scheduling notification alarms for a lesson.
 *
 * Schedules two types of alarms:
 * 1. LESSON_REMINDER: Morning alarm at lessonReminderTime on lesson day
 * 2. NOTE_REMINDER: Evening alarm at logReminderTime on lesson day
 *
 * Skips scheduling if:
 * - Lesson is cancelled
 * - Calculated trigger time is in the past
 */
class ScheduleNotificationAlarmsUseCase @Inject constructor(
    private val alarmScheduler: AlarmScheduler,
    private val lessonRepository: LessonRepository,
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(lessonId: Int) {
        // Get lesson with student data
        val lessonWithStudent = lessonRepository.getLessonWithStudent(lessonId) ?: return
        val lesson = lessonWithStudent.lesson
        val student = lessonWithStudent.student

        // Don't schedule alarms for cancelled lessons
        if (lesson.status == com.barutdev.kora.domain.model.LessonStatus.CANCELLED) {
            return
        }

        // Get reminder settings
        val settings = settingsRepository.getReminderSettings().first()
        Log.d("NotificationDebug", "ScheduleNotificationAlarmsUseCase: Received settings - Lesson time: ${settings.lessonReminderTime}, Log time: ${settings.logReminderTime}")

        // Convert lesson timestamp to LocalDate and LocalTime
        val lessonDateTime = Instant.ofEpochMilli(lesson.date)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
        val lessonDate = lessonDateTime.toLocalDate()
        val lessonTime = lessonDateTime.toLocalTime()

        // Create notification data
        val notificationData = NotificationData(
            type = NotificationType.LESSON_REMINDER, // Will be overridden per alarm
            lessonId = lesson.id.toString(),
            studentId = student.id.toString(),
            studentName = student.fullName,
            lessonTime = lessonTime,
            lessonDate = lessonDate
        )

        // Schedule lesson reminder (morning alarm)
        val lessonReminderTrigger = LocalDateTime.of(lessonDate, settings.lessonReminderTime)
        if (lessonReminderTrigger.isAfter(LocalDateTime.now())) {
            val triggerTimestamp = lessonReminderTrigger.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            Log.d("NotificationDebug", "Scheduling LESSON_REMINDER for lessonId: ${lesson.id} at calculated trigger time: $triggerTimestamp ($lessonReminderTrigger)")
            val lessonReminderRequest = AlarmScheduleRequest(
                lessonId = lesson.id.toString(),
                notificationType = NotificationType.LESSON_REMINDER,
                triggerTime = lessonReminderTrigger,
                notificationData = notificationData.copy(type = NotificationType.LESSON_REMINDER)
            )
            alarmScheduler.scheduleAlarm(lessonReminderRequest)
        }

        // Schedule note reminder (evening alarm)
        val noteReminderTrigger = LocalDateTime.of(lessonDate, settings.logReminderTime)
        if (noteReminderTrigger.isAfter(LocalDateTime.now())) {
            val triggerTimestamp = noteReminderTrigger.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            Log.d("NotificationDebug", "Scheduling NOTE_REMINDER for lessonId: ${lesson.id} at calculated trigger time: $triggerTimestamp ($noteReminderTrigger)")
            val noteReminderRequest = AlarmScheduleRequest(
                lessonId = lesson.id.toString(),
                notificationType = NotificationType.NOTE_REMINDER,
                triggerTime = noteReminderTrigger,
                notificationData = notificationData.copy(type = NotificationType.NOTE_REMINDER)
            )
            alarmScheduler.scheduleAlarm(noteReminderRequest)
        }
    }
}
