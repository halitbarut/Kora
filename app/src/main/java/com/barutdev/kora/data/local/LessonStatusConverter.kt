package com.barutdev.kora.data.local

import androidx.room.TypeConverter
import com.barutdev.kora.domain.model.LessonStatus

class LessonStatusConverter {

    @TypeConverter
    fun fromStatus(status: LessonStatus): String = status.name

    @TypeConverter
    fun toStatus(value: String): LessonStatus = LessonStatus.valueOf(value)
}
