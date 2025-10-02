package com.barutdev.kora.data.repository

import com.barutdev.kora.data.local.StudentDao
import com.barutdev.kora.data.mapper.toDomain
import com.barutdev.kora.data.mapper.toEntity
import com.barutdev.kora.domain.model.Student
import com.barutdev.kora.domain.repository.StudentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class StudentRepositoryImpl @Inject constructor(
    private val studentDao: StudentDao
) : StudentRepository {

    override fun getAllStudents(): Flow<List<Student>> =
        studentDao.getAllStudents().map { entities ->
            entities.toDomain()
        }

    override fun getStudentById(id: Int): Flow<Student?> =
        studentDao.getStudentById(id).map { entity ->
            entity?.toDomain()
        }

    override suspend fun addStudent(student: Student) {
        studentDao.insert(student.toEntity())
    }
}
