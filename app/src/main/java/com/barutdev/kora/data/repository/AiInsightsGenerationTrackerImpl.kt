package com.barutdev.kora.data.repository

import com.barutdev.kora.domain.model.ai.AiInsightsRequestKey
import com.barutdev.kora.domain.repository.AiInsightsGenerationTracker
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@Singleton
class AiInsightsGenerationTrackerImpl @Inject constructor() : AiInsightsGenerationTracker {

    private val running = MutableStateFlow<Map<AiInsightsRequestKey, String>>(emptyMap())

    override val runningGenerations: StateFlow<Map<AiInsightsRequestKey, String>> =
        running.asStateFlow()

    override fun markGenerationStarted(key: AiInsightsRequestKey, signature: String) {
        running.update { current ->
            if (current[key] == signature) {
                current
            } else {
                current.toMutableMap().apply { put(key, signature) }
            }
        }
    }

    override fun markGenerationFinished(key: AiInsightsRequestKey) {
        running.update { current ->
            if (key in current) {
                current.toMutableMap().apply { remove(key) }
            } else {
                current
            }
        }
    }

    override fun getRunningSignature(key: AiInsightsRequestKey): String? = running.value[key]
}
