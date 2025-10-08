package com.barutdev.kora.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.barutdev.kora.data.local.entity.AiInsightEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AiInsightDao {

    @Query(
        """
        SELECT * FROM ai_insights
        WHERE student_id = :studentId AND focus = :focus AND locale_tag = :localeTag
        LIMIT 1
        """
    )
    fun observeInsight(
        studentId: Int,
        focus: String,
        localeTag: String
    ): Flow<AiInsightEntity?>

    @Query(
        """
        SELECT * FROM ai_insights
        WHERE student_id = :studentId AND focus = :focus AND locale_tag = :localeTag
        LIMIT 1
        """
    )
    suspend fun getInsight(
        studentId: Int,
        focus: String,
        localeTag: String
    ): AiInsightEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertInsight(entity: AiInsightEntity)

    @Query(
        """
        DELETE FROM ai_insights
        WHERE student_id = :studentId AND focus = :focus AND locale_tag = :localeTag
        """
    )
    suspend fun deleteInsight(
        studentId: Int,
        focus: String,
        localeTag: String
    )
}
