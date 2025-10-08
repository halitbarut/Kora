package com.barutdev.kora.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "ai_insights",
    primaryKeys = ["student_id", "focus", "locale_tag"]
)
data class AiInsightEntity(
    @ColumnInfo(name = "student_id") val studentId: Int,
    @ColumnInfo(name = "focus") val focus: String,
    @ColumnInfo(name = "locale_tag") val localeTag: String,
    @ColumnInfo(name = "insight") val insight: String,
    @ColumnInfo(name = "signature") val signature: String,
    @ColumnInfo(name = "updated_at") val updatedAt: Long
)
