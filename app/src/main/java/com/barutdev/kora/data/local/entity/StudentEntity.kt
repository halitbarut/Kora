package com.barutdev.kora.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val fullName: String,
    val hourlyRate: Double,
    val lastPaymentDate: Long? = null,
    val parentName: String? = null,
    val parentContact: String? = null,
    val notes: String? = null,
    val customHourlyRate: Double? = null
)
