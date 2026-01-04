package auto.script.feature.scheduler.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import auto.script.BuildConfig

@Database(entities = [TaskEntity::class], version = 1)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var instance: TaskDatabase? = null

        fun getInstance(context: Context): TaskDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    TaskDatabase::class.java,
                    "task_database_" + BuildConfig.BUILD_TYPE
                ).build().also { instance = it }
            }
        }
    }
}
