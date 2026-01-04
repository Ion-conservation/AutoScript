package auto.script.feature.scheduler.di

import android.content.Context
import auto.script.feature.scheduler.db.TaskDatabase
import auto.script.feature.scheduler.db.TaskRepository
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
    ): TaskDatabase {
        return TaskDatabase.getInstance(context)
    }

    @Singleton
    @Provides
    fun provideTaskRepository(database: TaskDatabase): TaskRepository {
        return TaskRepository(database.taskDao())
    }
}
