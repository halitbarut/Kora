package com.barutdev.kora.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.barutdev.kora.data.local.entity.LessonEntity
import com.barutdev.kora.data.local.entity.StudentEntity

@Database(
    entities = [StudentEntity::class, LessonEntity::class],
    version = 3,
    exportSchema = false
)
abstract class KoraDatabase : RoomDatabase() {

    abstract fun studentDao(): StudentDao

    abstract fun lessonDao(): LessonDao
}
