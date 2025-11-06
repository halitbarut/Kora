package com.barutdev.kora.domain.usecase.notification

import com.barutdev.kora.domain.model.Lesson
import com.barutdev.kora.domain.repository.LessonRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

/**
 * UseCase for retrieving today's lessons.
 *
 * Returns a Flow of lessons scheduled for today (all statuses).
 * Used for scheduling lesson reminder alarms.
 */
class GetTodayLessonsUseCase @Inject constructor(
    private val lessonRepository: LessonRepository
) {
    operator fun invoke(): Flow<List<Lesson>> {
        return lessonRepository.getLessonsForDate(LocalDate.now())
    }
}
