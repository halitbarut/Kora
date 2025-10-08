package com.barutdev.kora.data.repository

import com.barutdev.kora.data.local.AiInsightDao
import com.barutdev.kora.data.local.entity.AiInsightEntity
import com.barutdev.kora.domain.model.ai.AiInsightsFocus
import com.barutdev.kora.domain.model.ai.CachedAiInsight
import com.barutdev.kora.domain.repository.AiInsightsCacheRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AiInsightsCacheRepositoryImpl @Inject constructor(
    private val aiInsightDao: AiInsightDao
) : AiInsightsCacheRepository {

    override fun observeInsight(
        studentId: Int,
        focus: AiInsightsFocus,
        localeTag: String
    ): Flow<CachedAiInsight?> = aiInsightDao.observeInsight(
        studentId = studentId,
        focus = focus.name,
        localeTag = localeTag
    ).map { entity -> entity?.toDomain() }

    override suspend fun getInsight(
        studentId: Int,
        focus: AiInsightsFocus,
        localeTag: String
    ): CachedAiInsight? = aiInsightDao.getInsight(
        studentId = studentId,
        focus = focus.name,
        localeTag = localeTag
    )?.toDomain()

    override suspend fun saveInsight(insight: CachedAiInsight) {
        aiInsightDao.upsertInsight(insight.toEntity())
    }

    override suspend fun clearInsight(
        studentId: Int,
        focus: AiInsightsFocus,
        localeTag: String
    ) {
        aiInsightDao.deleteInsight(
            studentId = studentId,
            focus = focus.name,
            localeTag = localeTag
        )
    }

    private fun AiInsightEntity.toDomain(): CachedAiInsight = CachedAiInsight(
        studentId = studentId,
        focus = AiInsightsFocus.valueOf(focus),
        localeTag = localeTag,
        insight = insight,
        signature = signature,
        updatedAt = updatedAt
    )

    private fun CachedAiInsight.toEntity(): AiInsightEntity = AiInsightEntity(
        studentId = studentId,
        focus = focus.name,
        localeTag = localeTag,
        insight = insight,
        signature = signature,
        updatedAt = updatedAt
    )
}
