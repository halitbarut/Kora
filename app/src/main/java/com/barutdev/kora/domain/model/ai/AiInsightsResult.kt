package com.barutdev.kora.domain.model.ai

sealed interface AiInsightsResult {
    data class Success(val insight: String) : AiInsightsResult
    data object MissingApiKey : AiInsightsResult
    data object NotEnoughData : AiInsightsResult
    data object EmptyResponse : AiInsightsResult
    data class Error(val cause: Throwable) : AiInsightsResult
}
