package com.barutdev.kora.di

import android.app.AlarmManager
import android.content.Context
import com.barutdev.kora.data.notification.AlarmSchedulerImpl
import com.barutdev.kora.data.notification.NotificationBuilderImpl
import com.barutdev.kora.data.repository.SettingsRepositoryImpl
import com.barutdev.kora.domain.repository.AlarmScheduler
import com.barutdev.kora.domain.repository.NotificationBuilder
import com.barutdev.kora.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing notification system dependencies.
 *
 * Binds:
 * - AlarmScheduler implementation
 * - NotificationBuilder implementation
 * - SettingsRepository implementation (uses existing DataStore from PreferencesModule)
 *
 * Note: DataStore<Preferences> is provided by PreferencesModule and shared across the app.
 * AlarmManager is provided by the companion object module.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationModule {

    @Binds
    @Singleton
    abstract fun bindAlarmScheduler(
        alarmSchedulerImpl: AlarmSchedulerImpl
    ): AlarmScheduler

    @Binds
    @Singleton
    abstract fun bindNotificationBuilder(
        notificationBuilderImpl: NotificationBuilderImpl
    ): NotificationBuilder

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        settingsRepositoryImpl: SettingsRepositoryImpl
    ): SettingsRepository
}

/**
 * Separate module for providing system services.
 * Must be an object module (not abstract) for @Provides methods.
 */
@Module
@InstallIn(SingletonComponent::class)
object NotificationDataModule {

    @Provides
    @Singleton
    fun provideAlarmManager(
        @ApplicationContext context: Context
    ): AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
}
