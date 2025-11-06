package com.barutdev.kora.domain.usecase.notification

import com.barutdev.kora.domain.repository.AlarmScheduler
import com.barutdev.kora.domain.repository.LessonRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * UseCase for rescheduling all active lesson notification alarms.
 *
 * Cancels all existing alarms and reschedules them for all active lessons.
 * Used when:
 * - Reminder time settings change (morning/evening times)
 * - Device reboots (alarms are cleared by system)
 *
 * Process:
 * 1. Cancel all existing alarms
 * 2. Get all active lessons (not cancelled, date >= today)
 * 3. Schedule alarms for each active lesson
 *
 * This ensures alarms reflect current settings and lesson data.
 */
class RescheduleAllNotificationAlarmsUseCase @Inject constructor(
    private val alarmScheduler: AlarmScheduler,
    private val lessonRepository: LessonRepository,
    private val scheduleNotificationAlarmsUseCase: ScheduleNotificationAlarmsUseCase
) {
    suspend operator fun invoke() {
        // Cancel all existing alarms
        alarmScheduler.cancelAllAlarms()

        // Get all active lessons
        val activeLessons = lessonRepository.getActiveLessons().first()

        // Schedule alarms for each active lesson
        activeLessons.forEach { lesson ->
            scheduleNotificationAlarmsUseCase(lesson.id)
        }
    }
}
