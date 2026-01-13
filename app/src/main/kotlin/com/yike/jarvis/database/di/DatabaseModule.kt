package com.yike.jarvis.database.di

import android.content.Context
import com.yike.jarvis.database.AppDatabase
import com.yike.jarvis.database.BackupManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Singleton
    @Provides
    fun provideBackupManager(
        @ApplicationContext context: Context
    ): BackupManager {
        return BackupManager(context)
    }
}
