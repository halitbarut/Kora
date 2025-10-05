package com.barutdev.kora.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.barutdev.kora.domain.model.HomeworkStatus

@Entity(
    tableName = "homework",
    foreignKeys = [
        ForeignKey(
            entity = StudentEntity::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["studentId"])]
)
data class HomeworkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val studentId: Int,
    val title: String,
    val description: String,
    val creationDate: Long,
    val dueDate: Long,
    val status: HomeworkStatus,
    val performanceNotes: String?
)
