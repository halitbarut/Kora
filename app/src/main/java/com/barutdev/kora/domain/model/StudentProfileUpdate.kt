package com.barutdev.kora.domain.model

data class StudentProfileUpdate(
    val id: Int,
    val fullName: String,
    val parentName: String?,
    val parentContact: String?,
    val notes: String?,
    val customHourlyRate: Double?
)
