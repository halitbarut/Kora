package com.barutdev.kora.domain.repository

import com.barutdev.kora.domain.model.ai.AiInsightsRequestKey
import kotlinx.coroutines.flow.StateFlow

interface AiInsightsGenerationTracker {

    val runningGenerations: StateFlow<Map<AiInsightsRequestKey, String>>

    fun markGenerationStarted(
        key: AiInsightsRequestKey,
        signature: String
    )

    fun markGenerationFinished(key: AiInsightsRequestKey)

    fun getRunningSignature(key: AiInsightsRequestKey): String?
}
