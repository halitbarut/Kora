package com.barutdev.kora.domain.usecase.notification

import com.barutdev.kora.domain.model.NotificationType
import com.barutdev.kora.domain.repository.AlarmScheduler
import javax.inject.Inject

/**
 * UseCase for cancelling notification alarms for a lesson.
 *
 * Cancels both LESSON_REMINDER and NOTE_REMINDER alarms for the given lesson.
 * Used when:
 * - A lesson is deleted
 * - A lesson is rescheduled (cancel old alarms before scheduling new ones)
 * - A lesson is cancelled
 *
 * Safe to call even if alarms don't exist (idempotent).
 */
class CancelNotificationAlarmsUseCase @Inject constructor(
    private val alarmScheduler: AlarmScheduler
) {
    suspend operator fun invoke(lessonId: Int) {
        val lessonIdStr = lessonId.toString()
        alarmScheduler.cancelAlarm(lessonIdStr, NotificationType.LESSON_REMINDER)
        alarmScheduler.cancelAlarm(lessonIdStr, NotificationType.NOTE_REMINDER)
    }
}
