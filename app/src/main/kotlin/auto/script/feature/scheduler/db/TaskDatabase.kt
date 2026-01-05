package auto.script.feature.scheduler.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import auto.script.BuildConfig

@Database(entities = [TaskEntity::class], version = 2)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var instance: TaskDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 增加 category 字段，默认为 'LIFE'
                database.execSQL("ALTER TABLE tasks ADD COLUMN category TEXT NOT NULL DEFAULT 'LIFE'")
            }
        }

        fun getInstance(context: Context): TaskDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    TaskDatabase::class.java,
                    "task_database_" + BuildConfig.BUILD_TYPE
                ).addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration(true) // 开发版本自动重建
                    .build().also { instance = it }
            }
        }
    }
}
