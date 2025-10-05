package com.barutdev.kora.domain.repository

import com.barutdev.kora.domain.model.Homework
import kotlinx.coroutines.flow.Flow

interface HomeworkRepository {

    fun getHomeworkForStudent(studentId: Int): Flow<List<Homework>>

    suspend fun insertHomework(homework: Homework)

    suspend fun updateHomework(homework: Homework)
}
