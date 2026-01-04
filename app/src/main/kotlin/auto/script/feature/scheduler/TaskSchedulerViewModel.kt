package auto.script.feature.scheduler

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import auto.script.feature.scheduler.db.TaskEntity
import auto.script.feature.scheduler.db.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskSchedulerViewModel @Inject constructor(private val repository: TaskRepository) : ViewModel() {
    
    // 获取所有任务流
    val tasks: StateFlow<List<TaskEntity>> = repository.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

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
}
