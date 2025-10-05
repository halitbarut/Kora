package com.barutdev.kora.data.repository

import com.barutdev.kora.data.local.HomeworkDao
import com.barutdev.kora.data.mapper.toDomain
import com.barutdev.kora.data.mapper.toEntity
import com.barutdev.kora.domain.model.Homework
import com.barutdev.kora.domain.repository.HomeworkRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HomeworkRepositoryImpl @Inject constructor(
    private val homeworkDao: HomeworkDao
) : HomeworkRepository {

    override fun getHomeworkForStudent(studentId: Int): Flow<List<Homework>> =
        homeworkDao.getHomeworkForStudent(studentId).map { entities ->
            entities.toDomain()
        }

    override suspend fun insertHomework(homework: Homework) {
        homeworkDao.insert(homework.toEntity())
    }

    override suspend fun updateHomework(homework: Homework) {
        homeworkDao.insert(homework.toEntity())
    }
}
