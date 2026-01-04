package auto.script.feature.scheduler.db

import auto.script.feature.scheduler.TaskSchedulerViewModel

/**
 * TaskDao 使用示例
 * 这个文件演示如何使用 TaskDao 进行各种数据库操作
 */

// ============= 增 (Insert) =============

/**
 * 示例：添加单个任务
 */
suspend fun exampleInsertTask(dao: TaskDao) {
    val task = TaskEntity(
        time = "10:00 AM",
        name = "Morning Meeting",
        isActive = true
    )
    val taskId = dao.insertTask(task)
    println("插入的任务 ID: $taskId")
}

/**
 * 示例：批量添加任务
 */
suspend fun exampleInsertMultipleTasks(dao: TaskDao) {
    val tasks = listOf(
        TaskEntity(time = "08:00 AM", name = "Workout", isActive = true),
        TaskEntity(time = "12:00 PM", name = "Lunch", isActive = false),
        TaskEntity(time = "06:00 PM", name = "Dinner", isActive = true)
    )
    tasks.forEach { task ->
        dao.insertTask(task)
    }
    println("已插入 ${tasks.size} 个任务")
}

// ============= 删 (Delete) =============

/**
 * 示例：删除指定任务
 */
suspend fun exampleDeleteTask(dao: TaskDao) {
    val task = TaskEntity(
        id = 1,
        time = "10:00 AM",
        name = "Meeting",
        isActive = true
    )
    dao.deleteTask(task)
    println("已删除任务")
}

/**
 * 示例：根据 ID 删除任务
 */
suspend fun exampleDeleteTaskById(dao: TaskDao, taskId: Int) {
    dao.deleteTaskById(taskId)
    println("已删除 ID=$taskId 的任务")
}

/**
 * 示例：删除所有任务
 */
suspend fun exampleDeleteAllTasks(dao: TaskDao) {
    dao.deleteAllTasks()
    println("已删除所有任务")
}

// ============= 改 (Update) =============

/**
 * 示例：更新任务
 */
suspend fun exampleUpdateTask(dao: TaskDao) {
    val task = TaskEntity(
        id = 1,
        time = "11:00 AM",
        name = "Updated Meeting",
        isActive = false
    )
    dao.updateTask(task)
    println("已更新任务")
}

/**
 * 示例：切换任务的活跃状态
 */
suspend fun exampleToggleTaskStatus(dao: TaskDao, taskId: Int) {
    val task = dao.getTaskById(taskId)
    if (task != null) {
        val updated = task.copy(isActive = !task.isActive)
        dao.updateTask(updated)
        println("任务活跃状态已切换: ${updated.isActive}")
    }
}

// ============= 查 (Read) =============

/**
 * 示例：获取所有任务
 */
suspend fun exampleGetAllTasks(dao: TaskDao) {
    dao.getAllTasks().collect { tasks ->
        tasks.forEach { task ->
            println("任务: ID=${task.id}, 时间=${task.time}, 名称=${task.name}, 活跃=${task.isActive}")
        }
    }
}

/**
 * 示例：根据 ID 获取任务
 */
suspend fun exampleGetTaskById(dao: TaskDao, taskId: Int) {
    val task = dao.getTaskById(taskId)
    if (task != null) {
        println("找到任务: ${task.name} at ${task.time}")
    } else {
        println("任务不存在")
    }
}

/**
 * 示例：获取活跃任务数量
 */
suspend fun exampleGetActiveTaskCount(dao: TaskDao) {
    val count = dao.getActiveTaskCount()
    println("活跃任务数: $count")
}

// ============= Repository 层使用示例 =============

/**
 * 示例：使用 Repository 进行操作
 */
suspend fun exampleRepositoryUsage(repository: TaskRepository) {
    // 添加任务
    val task = TaskEntity(time = "14:00 PM", name = "Code Review", isActive = true)
    repository.addTask(task)

    // 获取所有任务
    repository.getAllTasks().collect { tasks ->
        println("总共有 ${tasks.size} 个任务")
    }

    // 获取活跃任务数
    val activeCount = repository.getActiveTaskCount()
    println("活跃任务: $activeCount 个")

    // 更新任务
    repository.updateTask(task.copy(id = 1, isActive = false))

    // 删除任务
    repository.deleteTaskById(1)
}

// ============= ViewModel 使用示例 =============

/**
 * 示例：在 ViewModel 中使用
 *
 * @see TaskSchedulerViewModel
 */
suspend fun exampleViewModelUsage(viewModel: TaskSchedulerViewModel) {
    // 观察任务列表
    viewModel.tasks.collect { tasks ->
        println("当前任务: ${tasks.map { it.name }}")
    }

    // 添加新任务
    viewModel.addTask("15:00 PM", "Presentation", true)

    // 获取第一个任务并切换其状态
    val firstTask = viewModel.tasks.value.firstOrNull()
    if (firstTask != null) {
        viewModel.toggleTaskStatus(firstTask)
    }

    // 删除任务
    viewModel.deleteTaskById(1)
}
