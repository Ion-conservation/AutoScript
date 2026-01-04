package auto.script.feature.scheduler.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    // 查询所有任务
    @Query("SELECT * FROM tasks ORDER BY time ASC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    // 根据 ID 查询任务
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Int): TaskEntity?

    // 添加任务
    @Insert
    suspend fun insertTask(task: TaskEntity): Long

    // 更新任务
    @Update
    suspend fun updateTask(task: TaskEntity)

    // 删除任务
    @Delete
    suspend fun deleteTask(task: TaskEntity)

    // 根据 ID 删除任务
    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: Int)

    // 删除所有任务
    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()

    // 获取活跃任务数量
    @Query("SELECT COUNT(*) FROM tasks WHERE isActive = 1")
    suspend fun getActiveTaskCount(): Int
}
