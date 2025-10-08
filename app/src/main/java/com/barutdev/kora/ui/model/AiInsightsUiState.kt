package com.barutdev.kora.ui.model

import androidx.annotation.StringRes

enum class AiStatus {
    Idle,
    Loading,
    Success,
    Error,
    NoData
}

data class AiInsightsUiState(
    val status: AiStatus = AiStatus.Idle,
    val insight: String? = null,
    @StringRes val messageRes: Int? = null
)
