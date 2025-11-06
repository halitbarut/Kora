package com.barutdev.kora.domain.usecase.notification

import com.barutdev.kora.domain.model.Lesson
import com.barutdev.kora.domain.repository.LessonRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

/**
 * UseCase for retrieving completed lessons for a specific date.
 *
 * Returns a Flow of lessons with status COMPLETED for the given date.
 * Used for scheduling note reminder alarms for completed lessons.
 */
class GetCompletedLessonsForDateUseCase @Inject constructor(
    private val lessonRepository: LessonRepository
) {
    operator fun invoke(date: LocalDate): Flow<List<Lesson>> {
        return lessonRepository.getCompletedLessonsForDate(date)
    }
}
