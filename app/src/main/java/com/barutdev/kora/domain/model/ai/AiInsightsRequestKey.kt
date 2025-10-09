package com.barutdev.kora.domain.model.ai

data class AiInsightsRequestKey(
    val studentId: Int,
    val focus: AiInsightsFocus,
    val localeTag: String
)
