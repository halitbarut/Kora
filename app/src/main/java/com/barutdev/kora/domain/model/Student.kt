package com.barutdev.kora.domain.model

data class Student(
    val id: Int,
    val fullName: String,
    val hourlyRate: Double,
    val lastPaymentDate: Long? = null,
    val parentName: String? = null,
    val parentContact: String? = null,
    val notes: String? = null,
    val customHourlyRate: Double? = null
)
