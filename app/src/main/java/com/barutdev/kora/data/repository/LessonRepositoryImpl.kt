package com.barutdev.kora.data.repository

import androidx.room.withTransaction
import com.barutdev.kora.data.local.KoraDatabase
import com.barutdev.kora.data.local.LessonDao
import com.barutdev.kora.data.local.StudentDao
import com.barutdev.kora.data.mapper.toDomain
import com.barutdev.kora.data.mapper.toEntity
import com.barutdev.kora.domain.model.Lesson
import com.barutdev.kora.domain.repository.LessonRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LessonRepositoryImpl @Inject constructor(
    private val lessonDao: LessonDao,
    private val studentDao: StudentDao,
    private val database: KoraDatabase
) : LessonRepository {

    override fun getAllLessons(): Flow<List<Lesson>> =
        lessonDao.getAllLessons().map { entities ->
            entities.toDomain()
        }

    override fun getLessonsForStudent(studentId: Int): Flow<List<Lesson>> =
        lessonDao.getLessonsForStudent(studentId).map { entities ->
            entities.toDomain()
        }

    override suspend fun insertLesson(lesson: Lesson): Int {
        return lessonDao.insert(lesson.toEntity()).toInt()
    }

    override suspend fun updateLesson(lesson: Lesson) {
        lessonDao.update(lesson.toEntity())
    }

    override suspend fun markCompletedLessonsAsPaid(studentId: Int) {
        val timestamp = System.currentTimeMillis()
        database.withTransaction {
            lessonDao.markCompletedLessonsAsPaid(
                studentId = studentId,
                paymentTimestamp = timestamp
            )
            studentDao.updateLastPaymentDate(
                studentId = studentId,
                lastPaymentDate = timestamp
            )
        }
    }
}
