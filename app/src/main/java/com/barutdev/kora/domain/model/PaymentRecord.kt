package com.barutdev.kora.domain.model

data class PaymentRecord(
    val id: Int,
    val studentId: Int,
    val amountMinor: Long,
    val paidAtEpochMs: Long
)