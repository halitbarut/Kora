package com.barutdev.kora.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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

    @Query("SELECT * FROM lessons")
    suspend fun getLessonsSnapshot(): List<LessonEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(lessons: List<LessonEntity>)

    @Query("DELETE FROM lessons")
    suspend fun deleteAll()

    @Query("DELETE FROM lessons WHERE id = :lessonId")
    suspend fun deleteLesson(lessonId: Int)

    // Context-aware notification queries

    /**
     * Returns lessons for a specific date range (day start to day end).
     * @param startOfDay Timestamp for start of day (00:00:00)
     * @param endOfDay Timestamp for end of day (23:59:59)
     */
    @Query("SELECT * FROM lessons WHERE date >= :startOfDay AND date <= :endOfDay")
    fun getLessonsForDateRange(startOfDay: Long, endOfDay: Long): Flow<List<LessonEntity>>

    /**
     * Returns completed lessons for a specific date range.
     */
    @Query(
        "SELECT * FROM lessons WHERE date >= :startOfDay AND date <= :endOfDay " +
            "AND status = :completedStatus"
    )
    fun getCompletedLessonsForDateRange(
        startOfDay: Long,
        endOfDay: Long,
        completedStatus: LessonStatus = LessonStatus.COMPLETED
    ): Flow<List<LessonEntity>>

    /**
     * Returns a single lesson by ID.
     */
    @Query("SELECT * FROM lessons WHERE id = :lessonId")
    suspend fun getLessonById(lessonId: Int): LessonEntity?

    /**
     * Returns active lessons (not cancelled, scheduled >= today).
     * @param currentDate Current date timestamp
     */
    @Query(
        "SELECT * FROM lessons WHERE date >= :currentDate " +
            "AND status != :cancelledStatus"
    )
    fun getActiveLessons(
        currentDate: Long,
        cancelledStatus: LessonStatus = LessonStatus.CANCELLED
    ): Flow<List<LessonEntity>>
}
