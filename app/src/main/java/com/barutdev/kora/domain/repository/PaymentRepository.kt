package com.barutdev.kora.domain.repository

import com.barutdev.kora.domain.model.PaymentRecord
import kotlinx.coroutines.flow.Flow

interface PaymentRepository {
    fun observePaymentHistory(studentId: Int): Flow<List<PaymentRecord>>

    suspend fun markStudentAsPaid(studentId: Int)
}