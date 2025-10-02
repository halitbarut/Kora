package com.barutdev.kora.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.barutdev.kora.data.local.entity.StudentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {

    @Insert
    suspend fun insert(student: StudentEntity)

    @Update
    suspend fun update(student: StudentEntity)

    @Delete
    suspend fun delete(student: StudentEntity)

    @Query("SELECT * FROM students WHERE id = :id")
    fun getStudentById(id: Int): Flow<StudentEntity?>

    @Query("SELECT * FROM students")
    fun getAllStudents(): Flow<List<StudentEntity>>
}
