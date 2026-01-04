package auto.script.feature.scheduler.db

import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {
    // 获取所有任务
    fun getAllTasks(): Flow<List<TaskEntity>> = taskDao.getAllTasks()

    // 根据 ID 获取任务
    suspend fun getTaskById(taskId: Int): TaskEntity? = taskDao.getTaskById(taskId)

    // 添加任务
    suspend fun addTask(task: TaskEntity): Long = taskDao.insertTask(task)

    // 更新任务
    suspend fun updateTask(task: TaskEntity) = taskDao.updateTask(task)

    // 删除任务
    suspend fun deleteTask(task: TaskEntity) = taskDao.deleteTask(task)

    // 根据 ID 删除任务
    suspend fun deleteTaskById(taskId: Int) = taskDao.deleteTaskById(taskId)

    // 删除所有任务
    suspend fun deleteAllTasks() = taskDao.deleteAllTasks()

    // 获取活跃任务数量
    suspend fun getActiveTaskCount(): Int = taskDao.getActiveTaskCount()
}
