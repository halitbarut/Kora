package com.barutdev.kora.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.barutdev.kora.data.local.entity.LessonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonDao {

    @Insert
    suspend fun insert(lesson: LessonEntity)

    @Query("SELECT * FROM lessons WHERE studentId = :studentId")
    fun getLessonsForStudent(studentId: Int): Flow<List<LessonEntity>>
}
