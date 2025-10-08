package com.barutdev.kora.domain.repository

/**
 * Repository contract for delegating AI content generation requests.
 */
interface AiRepository {

    /**
     * Generates insight text for the supplied prompt.
     *
     * Implementations should throw a domain-level AiException when the request cannot
     * be completed (e.g. missing API key, empty response, network failure).
     */
    suspend fun generateInsights(prompt: String): String
}
