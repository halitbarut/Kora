package com.barutdev.kora.domain.repository

import com.barutdev.kora.domain.model.Lesson
import com.barutdev.kora.domain.model.LessonWithStudent
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface LessonRepository {
    fun getAllLessons(): Flow<List<Lesson>>

    fun getLessonsForStudent(studentId: Int): Flow<List<Lesson>>

    suspend fun insertLesson(lesson: Lesson): Int

    suspend fun updateLesson(lesson: Lesson)

    suspend fun deleteLesson(lessonId: Int)

    suspend fun markCompletedLessonsAsPaid(studentId: Int)

    // New methods for context-aware notifications feature

    /**
     * Returns a Flow of lessons scheduled for a specific date.
     * Includes all statuses (PENDING, COMPLETED, CANCELLED).
     *
     * @param date Date to query
     * @return Flow<List<Lesson>> Lessons on the specified date
     */
    fun getLessonsForDate(date: LocalDate): Flow<List<Lesson>>

    /**
     * Returns a Flow of completed lessons for a specific date.
     * Filters to only status == COMPLETED.
     *
     * @param date Date to query
     * @return Flow<List<Lesson>> Completed lessons on the specified date
     */
    fun getCompletedLessonsForDate(date: LocalDate): Flow<List<Lesson>>

    /**
     * Retrieves a lesson with its associated student data.
     * Returns null if lesson or student not found.
     *
     * @param lessonId Lesson identifier
     * @return LessonWithStudent if found, null otherwise
     */
    suspend fun getLessonWithStudent(lessonId: Int): LessonWithStudent?

    /**
     * Returns a Flow of all active lessons (not cancelled, scheduled >= today).
     * Used for rescheduling alarms.
     *
     * @return Flow<List<Lesson>> Active future lessons
     */
    fun getActiveLessons(): Flow<List<Lesson>>
}
