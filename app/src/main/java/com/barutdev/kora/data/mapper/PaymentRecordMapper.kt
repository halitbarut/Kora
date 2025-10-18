package com.barutdev.kora.data.mapper

import com.barutdev.kora.data.local.entity.PaymentRecordEntity
import com.barutdev.kora.domain.model.PaymentRecord

fun PaymentRecordEntity.toDomain(): PaymentRecord = PaymentRecord(
    id = id,
    studentId = studentId,
    amountMinor = amountMinor,
    paidAtEpochMs = paidAtEpochMs
)

fun List<PaymentRecordEntity>.toDomain(): List<PaymentRecord> = map(PaymentRecordEntity::toDomain)

fun PaymentRecord.toEntity(): PaymentRecordEntity = PaymentRecordEntity(
    id = id,
    studentId = studentId,
    amountMinor = amountMinor,
    paidAtEpochMs = paidAtEpochMs
)