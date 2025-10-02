package com.barutdev.kora.di

import com.barutdev.kora.data.repository.LessonRepositoryImpl
import com.barutdev.kora.data.repository.StudentRepositoryImpl
import com.barutdev.kora.domain.repository.LessonRepository
import com.barutdev.kora.domain.repository.StudentRepository
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
}
