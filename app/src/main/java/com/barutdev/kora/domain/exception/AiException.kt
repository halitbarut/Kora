package com.barutdev.kora.domain.exception

sealed class AiException(message: String? = null, cause: Throwable? = null) : Exception(message, cause)

class MissingAiApiKeyException : AiException()

class EmptyAiResponseException : AiException()

class AiServiceUnavailableException(cause: Throwable) : AiException(cause = cause)
