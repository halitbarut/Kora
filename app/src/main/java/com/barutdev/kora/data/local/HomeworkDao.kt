package com.barutdev.kora.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.barutdev.kora.data.local.entity.HomeworkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HomeworkDao {

    @Query("SELECT * FROM homework WHERE studentId = :studentId ORDER BY dueDate")
    fun getHomeworkForStudent(studentId: Int): Flow<List<HomeworkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(homework: HomeworkEntity)

    @Query("SELECT * FROM homework")
    suspend fun getHomeworkSnapshot(): List<HomeworkEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(homework: List<HomeworkEntity>)

    @Query("DELETE FROM homework")
    suspend fun deleteAll()
}
