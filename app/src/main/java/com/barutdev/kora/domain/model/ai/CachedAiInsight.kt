package com.barutdev.kora.domain.model.ai

data class CachedAiInsight(
    val studentId: Int,
    val focus: AiInsightsFocus,
    val localeTag: String,
    val insight: String,
    val signature: String,
    val updatedAt: Long
)
