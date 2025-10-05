package com.barutdev.kora.domain.model

data class Lesson(
    val id: Int,
    val studentId: Int,
    val date: Long,
    val status: LessonStatus,
    val durationInHours: Double?,
    val notes: String?,
    val paymentTimestamp: Long? = null
)
