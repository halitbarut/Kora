package com.barutdev.kora.data.repository

import com.barutdev.kora.domain.exception.AiServiceUnavailableException
import com.barutdev.kora.domain.exception.EmptyAiResponseException
import com.barutdev.kora.domain.exception.MissingAiApiKeyException
import com.barutdev.kora.domain.repository.AiRepository
import com.google.ai.client.generativeai.GenerativeModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AiRepositoryImpl @Inject constructor(
    private val generativeModel: GenerativeModel?
) : AiRepository {

    override suspend fun generateInsights(prompt: String): String {
        val model = generativeModel ?: throw MissingAiApiKeyException()
        val response = try {
            withContext(Dispatchers.IO) {
                model.generateContent(prompt)
            }
        } catch (exception: Exception) {
            if (exception is CancellationException) throw exception
            throw AiServiceUnavailableException(exception)
        }
        val text = response.text?.trim()
        if (text.isNullOrBlank()) {
            throw EmptyAiResponseException()
        }
        return text
    }
}
