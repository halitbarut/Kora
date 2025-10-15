package com.barutdev.kora.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
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

    @Query("UPDATE students SET lastPaymentDate = :lastPaymentDate WHERE id = :studentId")
    suspend fun updateLastPaymentDate(studentId: Int, lastPaymentDate: Long)

    @Query("SELECT * FROM students WHERE id = :id")
    fun getStudentById(id: Int): Flow<StudentEntity?>

    @Query("SELECT * FROM students")
    fun getAllStudents(): Flow<List<StudentEntity>>

    @Query("UPDATE students SET hourlyRate = :newRate WHERE id = :studentId")
    suspend fun updateStudentHourlyRate(studentId: Int, newRate: Double)

    @Query("UPDATE students SET fullName = :fullName, hourlyRate = :hourlyRate WHERE id = :studentId")
    suspend fun updateStudent(studentId: Int, fullName: String, hourlyRate: Double)

    @Query("SELECT * FROM students")
    suspend fun getStudentsSnapshot(): List<StudentEntity>

    @Query("DELETE FROM students WHERE id = :studentId")
    suspend fun deleteById(studentId: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(students: List<StudentEntity>)

    @Query("DELETE FROM students")
    suspend fun deleteAll()
}
