package auto.script.feature.scheduler

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import auto.script.common.NotificationService
import auto.script.feature.scheduler.db.TaskEntity
import auto.script.feature.scheduler.db.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class TaskSchedulerViewModel @Inject constructor(private val repository: TaskRepository) : ViewModel() {
    
    // 应用上下文（通过 Hilt 注入或手动设置）
    var applicationContext: Context? = null
    
    // 获取所有任务流
    val tasks: StateFlow<List<TaskEntity>> = repository.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    // 任务监视器启动标志
    private var isTaskMonitoringStarted = false

    // 添加任务
    fun addTask(time: String, name: String, isActive: Boolean) {
        viewModelScope.launch {
            val task = TaskEntity(time = time, name = name, isActive = isActive)
            repository.addTask(task)
        }
    }

    // 更新任务
    fun updateTask(id: Int, time: String, name: String, isActive: Boolean) {
        viewModelScope.launch {
            val task = TaskEntity(id = id, time = time, name = name, isActive = isActive)
            repository.updateTask(task)
        }
    }

    // 切换任务活跃状态
    fun toggleTaskStatus(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task.copy(isActive = !task.isActive))
        }
    }

    // 删除任务
    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    // 删除任务（通过 ID）
    fun deleteTaskById(taskId: Int) {
        viewModelScope.launch {
            repository.deleteTaskById(taskId)
        }
    }

    // 删除所有任务
    fun deleteAllTasks() {
        viewModelScope.launch {
            repository.deleteAllTasks()
        }
    }
    
    /**
     * 启动任务时间监视器
     * 定期检查活跃任务是否到达指定时间，到达时发送通知
     * 建议在 Application 或 MainActivity 中调用
     */
    fun startTaskMonitoring() {
        if (isTaskMonitoringStarted) return
        isTaskMonitoringStarted = true
        
        viewModelScope.launch {
            while (isTaskMonitoringStarted) {
                try {
                    val currentTasks = tasks.value
                    val now = Calendar.getInstance()
                    val currentHour = now.get(Calendar.HOUR_OF_DAY)
                    val currentMinute = now.get(Calendar.MINUTE)
                    
                    // 检查每个活跃任务
                    for (task in currentTasks) {
                        if (task.isActive) {
                            val (taskHour, taskMinute) = parseTimeString(task.time)
                            // 当前时间与任务时间匹配时发送通知
                            if (taskHour == currentHour && taskMinute == currentMinute) {
                                sendTaskNotification(task)
                            }
                        }
                    }
                    
                    // 每分钟检查一次
                    delay(60_000)
                } catch (e: Exception) {
                    android.util.Log.e("TaskScheduler", "Task monitoring error", e)
                    // 发送错误通知
                    applicationContext?.let {
                        NotificationService.showErrorNotification(
                            context = it,
                            errorTitle = "任务监视器错误",
                            errorMessage = "任务监视过程中出错: ${e.message}"
                        )
                    }
                }
            }
        }
    }
    
    /**
     * 停止任务时间监视器
     */
    fun stopTaskMonitoring() {
        isTaskMonitoringStarted = false
    }
    
    /**
     * 解析时间字符串 (例如 "08:00 AM") 为 24 小时制的小时和分钟
     * @param timeStr 时间字符串，格式为 "HH:MM AM/PM"
     * @return 返回 Pair(hour, minute)，其中 hour 是 24 小时制
     */
    private fun parseTimeString(timeStr: String): Pair<Int, Int> {
        return try {
            val parts = timeStr.replace(":", " ").split(" ")
            var hour = parts[0].toInt()
            val minute = parts[1].toInt()
            val amPm = parts[2].uppercase()
            if (amPm == "PM" && hour < 12) hour += 12
            if (amPm == "AM" && hour == 12) hour = 0
            Pair(hour, minute)
        } catch (e: Exception) {
            android.util.Log.e("TaskScheduler", "Failed to parse time: $timeStr", e)
            Pair(0, 0)
        }
    }
    
    /**
     * 发送任务通知
     * @param task 要发送通知的任务
     */
    private fun sendTaskNotification(task: TaskEntity) {
        applicationContext?.let { context ->
            NotificationService.showTaskNotification(
                context = context,
                taskName = task.name,
                message = "任务「${task.name}」时间已到（${task.time}）"
            )
        }
    }
}
