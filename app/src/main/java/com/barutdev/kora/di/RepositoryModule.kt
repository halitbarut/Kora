package com.barutdev.kora.di

import com.barutdev.kora.data.repository.AiInsightsCacheRepositoryImpl
import com.barutdev.kora.data.repository.AiRepositoryImpl
import com.barutdev.kora.data.repository.AiInsightsGenerationTrackerImpl
import com.barutdev.kora.data.repository.HomeworkRepositoryImpl
import com.barutdev.kora.data.repository.LessonRepositoryImpl
import com.barutdev.kora.data.repository.StudentRepositoryImpl
import com.barutdev.kora.data.repository.UserPreferencesRepository as DataUserPreferencesRepository
import com.barutdev.kora.domain.repository.AiInsightsCacheRepository
import com.barutdev.kora.domain.repository.AiRepository
import com.barutdev.kora.domain.repository.HomeworkRepository
import com.barutdev.kora.domain.repository.LessonRepository
import com.barutdev.kora.domain.repository.StudentRepository
import com.barutdev.kora.domain.repository.UserPreferencesRepository
import com.barutdev.kora.domain.repository.AiInsightsGenerationTracker
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindStudentRepository(
        impl: StudentRepositoryImpl
    ): StudentRepository

    @Binds
    @Singleton
    abstract fun bindLessonRepository(
        impl: LessonRepositoryImpl
    ): LessonRepository

    @Binds
    @Singleton
    abstract fun bindHomeworkRepository(
        impl: HomeworkRepositoryImpl
    ): HomeworkRepository

    @Binds
    @Singleton
    abstract fun bindAiRepository(
        impl: AiRepositoryImpl
    ): AiRepository

    @Binds
    @Singleton
    abstract fun bindAiInsightsCacheRepository(
        impl: AiInsightsCacheRepositoryImpl
    ): AiInsightsCacheRepository

    @Binds
    @Singleton
    abstract fun bindAiInsightsGenerationTracker(
        impl: AiInsightsGenerationTrackerImpl
    ): AiInsightsGenerationTracker

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(
        impl: DataUserPreferencesRepository
    ): UserPreferencesRepository
}
