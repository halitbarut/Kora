package com.barutdev.kora.domain.repository

import com.barutdev.kora.domain.model.Lesson
import kotlinx.coroutines.flow.Flow

interface LessonRepository {
    fun getAllLessons(): Flow<List<Lesson>>

    fun getLessonsForStudent(studentId: Int): Flow<List<Lesson>>

    suspend fun insertLesson(lesson: Lesson): Int

    suspend fun updateLesson(lesson: Lesson)

    suspend fun markCompletedLessonsAsPaid(studentId: Int)
}
