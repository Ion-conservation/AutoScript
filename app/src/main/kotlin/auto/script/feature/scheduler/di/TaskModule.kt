package auto.script.feature.scheduler.di

import android.content.Context
import auto.script.database.AppDatabase
import auto.script.feature.scheduler.db.repository.TaskRepository
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
    fun provideTaskDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Singleton
    @Provides
    fun provideTaskRepository(appDatabase: AppDatabase): TaskRepository {
        return TaskRepository(appDatabase.taskDao())
    }
}
