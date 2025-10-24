package com.barutdev.kora.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.barutdev.kora.data.local.entity.AiInsightEntity
import com.barutdev.kora.data.local.entity.HomeworkEntity
import com.barutdev.kora.data.local.entity.LessonEntity
import com.barutdev.kora.data.local.entity.StudentEntity

@Database(
    entities = [StudentEntity::class, LessonEntity::class, HomeworkEntity::class, AiInsightEntity::class, com.barutdev.kora.data.local.entity.PaymentRecordEntity::class],
    version = 8,
    exportSchema = false
)
@TypeConverters(LessonStatusConverter::class, HomeworkStatusConverter::class)
abstract class KoraDatabase : RoomDatabase() {

    abstract fun studentDao(): StudentDao

    abstract fun lessonDao(): LessonDao

    abstract fun homeworkDao(): HomeworkDao

    abstract fun aiInsightDao(): AiInsightDao

    abstract fun paymentRecordDao(): PaymentRecordDao
}
