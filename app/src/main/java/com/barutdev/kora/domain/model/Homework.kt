package com.barutdev.kora.domain.model

data class Homework(
    val id: Int,
    val studentId: Int,
    val title: String,
    val description: String,
    val creationDate: Long,
    val dueDate: Long,
    val status: HomeworkStatus,
    val performanceNotes: String?
)
