package auto.script.feature.scheduler

import android.content.Context
import android.util.Log
import auto.script.database.AppDatabase
import auto.script.feature.scheduler.db.entity.TaskEntity

object InitTaskDatabase {
    private const val PREFS_NAME = "task_db_init"
    private const val KEY_INITIALIZED = "is_initialized"

    suspend fun initializeWithSampleData(context: Context) {
        Log.i("InitTaskDatabase", "initializeWithSampleData")

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_INITIALIZED, false)) {
            return // 已初始化，不再重复
        }


        val appDatabase = AppDatabase.getInstance(context)
        val dao = appDatabase.taskDao()

        // 添加示例任务
        val sampleTasks = listOf(
            TaskEntity(time = "08:00 AM", name = "Morning Workout", isActive = true),
            TaskEntity(time = "09:30 AM", name = "Team Standup", isActive = true),
            TaskEntity(time = "12:00 PM", name = "Lunch Break", isActive = false),
            TaskEntity(time = "03:00 PM", name = "Code Review", isActive = true),
            TaskEntity(time = "06:00 PM", name = "Evening Walk", isActive = false)
        )

        sampleTasks.forEach { task ->
            dao.insertTask(task)
        }

        // 标记为已初始化
        prefs.edit().putBoolean(KEY_INITIALIZED, true).apply()


    }
}
