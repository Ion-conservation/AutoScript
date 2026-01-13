package com.yike.jarvis.feature.scheduler.di

import android.content.Context
import com.yike.jarvis.database.AppDatabase
import com.yike.jarvis.feature.scheduler.db.repository.TaskRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TaskModule {

    @Singleton
    @Provides
    fun provideTaskRepository(appDatabase: AppDatabase): TaskRepository {
        return TaskRepository(appDatabase.taskDao())
    }
}
