package com.barutdev.kora.domain.repository

import com.barutdev.kora.domain.model.Student
import kotlinx.coroutines.flow.Flow

interface StudentRepository {
    fun getAllStudents(): Flow<List<Student>>

    fun getStudentById(id: Int): Flow<Student?>

    suspend fun addStudent(student: Student)
}
