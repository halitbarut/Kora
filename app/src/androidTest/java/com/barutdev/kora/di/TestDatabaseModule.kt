package com.barutdev.kora.di

import android.content.Context
import androidx.room.Room
import com.barutdev.kora.data.local.AiInsightDao
import com.barutdev.kora.data.local.HomeworkDao
import com.barutdev.kora.data.local.KoraDatabase
import com.barutdev.kora.data.local.LessonDao
import com.barutdev.kora.data.local.PaymentRecordDao
import com.barutdev.kora.data.local.StudentDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DatabaseModule::class]
)
object TestDatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): KoraDatabase = Room.inMemoryDatabaseBuilder(
        context,
        KoraDatabase::class.java
    )
        .allowMainThreadQueries()
        .build()

    @Provides
    @Singleton
    fun provideStudentDao(database: KoraDatabase): StudentDao = database.studentDao()

    @Provides
    @Singleton
    fun provideLessonDao(database: KoraDatabase): LessonDao = database.lessonDao()

    @Provides
    @Singleton
    fun provideHomeworkDao(database: KoraDatabase): HomeworkDao = database.homeworkDao()

    @Provides
    @Singleton
    fun provideAiInsightDao(database: KoraDatabase): AiInsightDao = database.aiInsightDao()

    @Provides
    @Singleton
    fun providePaymentRecordDao(database: KoraDatabase): PaymentRecordDao = database.paymentRecordDao()
}
