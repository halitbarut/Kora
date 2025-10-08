package com.barutdev.kora.domain.repository

import com.barutdev.kora.domain.model.ai.AiInsightsFocus
import com.barutdev.kora.domain.model.ai.CachedAiInsight
import kotlinx.coroutines.flow.Flow

interface AiInsightsCacheRepository {
    fun observeInsight(studentId: Int, focus: AiInsightsFocus, localeTag: String): Flow<CachedAiInsight?>

    suspend fun getInsight(studentId: Int, focus: AiInsightsFocus, localeTag: String): CachedAiInsight?

    suspend fun saveInsight(insight: CachedAiInsight)

    suspend fun clearInsight(studentId: Int, focus: AiInsightsFocus, localeTag: String)
}
