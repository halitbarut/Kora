package com.barutdev.kora.domain.repository

import com.barutdev.kora.domain.model.Lesson
import kotlinx.coroutines.flow.Flow

interface LessonRepository {
    fun getLessonsForStudent(studentId: Int): Flow<List<Lesson>>

    suspend fun insertLesson(lesson: Lesson)
}
