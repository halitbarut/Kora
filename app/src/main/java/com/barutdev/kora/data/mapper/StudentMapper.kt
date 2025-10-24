package com.barutdev.kora.data.mapper

import com.barutdev.kora.data.local.entity.StudentEntity
import com.barutdev.kora.domain.model.Student

fun StudentEntity.toDomain(): Student = Student(
    id = id,
    fullName = fullName,
    hourlyRate = hourlyRate,
    lastPaymentDate = lastPaymentDate,
    parentName = parentName,
    parentContact = parentContact,
    notes = notes,
    customHourlyRate = customHourlyRate
)

fun List<StudentEntity>.toDomain(): List<Student> = map(StudentEntity::toDomain)

fun Student.toEntity(): StudentEntity = StudentEntity(
    id = id,
    fullName = fullName,
    hourlyRate = hourlyRate,
    lastPaymentDate = lastPaymentDate,
    parentName = parentName,
    parentContact = parentContact,
    notes = notes,
    customHourlyRate = customHourlyRate
)
