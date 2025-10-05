package com.barutdev.kora.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Update
import androidx.room.Query
import com.barutdev.kora.data.local.entity.LessonEntity
import com.barutdev.kora.domain.model.LessonStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonDao {

    @Insert
    suspend fun insert(lesson: LessonEntity): Long

    @Query("SELECT * FROM lessons")
    fun getAllLessons(): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE studentId = :studentId")
    fun getLessonsForStudent(studentId: Int): Flow<List<LessonEntity>>

    @Update
    suspend fun update(lesson: LessonEntity)

    @Query(
        "UPDATE lessons SET status = :paidStatus, paymentTimestamp = :paymentTimestamp " +
            "WHERE studentId = :studentId AND status = :completedStatus"
    )
    suspend fun markCompletedLessonsAsPaid(
        studentId: Int,
        paymentTimestamp: Long,
        completedStatus: LessonStatus = LessonStatus.COMPLETED,
        paidStatus: LessonStatus = LessonStatus.PAID
    )
}
