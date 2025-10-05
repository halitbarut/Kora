package com.barutdev.kora.data.mapper

import com.barutdev.kora.data.local.entity.HomeworkEntity
import com.barutdev.kora.domain.model.Homework

fun HomeworkEntity.toDomain(): Homework = Homework(
    id = id,
    studentId = studentId,
    title = title,
    description = description,
    creationDate = creationDate,
    dueDate = dueDate,
    status = status,
    performanceNotes = performanceNotes
)

fun List<HomeworkEntity>.toDomain(): List<Homework> = map(HomeworkEntity::toDomain)

fun Homework.toEntity(): HomeworkEntity = HomeworkEntity(
    id = id,
    studentId = studentId,
    title = title,
    description = description,
    creationDate = creationDate,
    dueDate = dueDate,
    status = status,
    performanceNotes = performanceNotes
)
