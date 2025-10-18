package com.barutdev.kora.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.barutdev.kora.data.local.entity.PaymentRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: PaymentRecordEntity)

    @Query(
        "SELECT * FROM payment_records WHERE studentId = :studentId ORDER BY paidAtEpochMs DESC"
    )
    fun observeByStudent(studentId: Int): Flow<List<PaymentRecordEntity>>
}