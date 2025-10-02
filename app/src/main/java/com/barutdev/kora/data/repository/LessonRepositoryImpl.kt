package com.barutdev.kora.data.repository

import com.barutdev.kora.data.local.LessonDao
import com.barutdev.kora.data.mapper.toDomain
import com.barutdev.kora.data.mapper.toEntity
import com.barutdev.kora.domain.model.Lesson
import com.barutdev.kora.domain.repository.LessonRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LessonRepositoryImpl @Inject constructor(
    private val lessonDao: LessonDao
) : LessonRepository {

    override fun getLessonsForStudent(studentId: Int): Flow<List<Lesson>> =
        lessonDao.getLessonsForStudent(studentId).map { entities ->
            entities.toDomain()
        }

    override suspend fun insertLesson(lesson: Lesson) {
        lessonDao.insert(lesson.toEntity())
    }
}
