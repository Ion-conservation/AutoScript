package com.yike.jarvis.feature.beverage.di

import com.yike.jarvis.database.AppDatabase
import com.yike.jarvis.feature.beverage.db.repository.BeverageRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BeverageModule {

    @Singleton
    @Provides
    fun provideBeverageRepository(appDatabase: AppDatabase): BeverageRepository {
        return BeverageRepository(
            appDatabase.beverageDao(),
            appDatabase.beverageCatalogDao()
        )
    }
}
