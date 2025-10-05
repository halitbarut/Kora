package com.barutdev.kora.data.mapper

import com.barutdev.kora.data.local.entity.LessonEntity
import com.barutdev.kora.domain.model.Lesson

fun LessonEntity.toDomain(): Lesson = Lesson(
    id = id,
    studentId = studentId,
    date = date,
    status = status,
    durationInHours = durationInHours,
    notes = notes,
    paymentTimestamp = paymentTimestamp
)

fun List<LessonEntity>.toDomain(): List<Lesson> = map(LessonEntity::toDomain)

fun Lesson.toEntity(): LessonEntity = LessonEntity(
    id = id,
    studentId = studentId,
    date = date,
    status = status,
    durationInHours = durationInHours,
    notes = notes,
    paymentTimestamp = paymentTimestamp
)
