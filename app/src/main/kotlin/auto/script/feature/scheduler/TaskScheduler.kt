package auto.script.feature.scheduler

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import auto.script.feature.scheduler.db.TaskCategory
import auto.script.feature.scheduler.db.TaskEntity
import auto.script.feature.scheduler.db.TaskImportance
import auto.script.ui.theme.DashboardColors

// 辅助函数：格式化时间为 "08:00 AM" 格式
private fun formatTime(hour: Int, minute: Int): String {
    val amPm = if (hour < 12) "AM" else "PM"
    val adjustedHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return String.format("%02d:%02d %s", adjustedHour, minute, amPm)
}

// 辅助函数：解析 "08:00 AM" 格式为 24 小时制的小时和分钟
private fun parseTime(timeStr: String): Pair<Int, Int> {
    return try {
        val parts = timeStr.replace(":", " ").split(" ")
        var hour = parts[0].toInt()
        val minute = parts[1].toInt()
        val amPm = parts[2].uppercase()
        if (amPm == "PM" && hour < 12) hour += 12
        if (amPm == "AM" && hour == 12) hour = 0
        Pair(hour, minute)
    } catch (e: Exception) {
        Pair(0, 0)
    }
}

@Composable
fun TaskScheduler(viewModel: TaskSchedulerViewModel? = null) {
    val tasks = viewModel?.tasks?.collectAsState()?.value ?: emptyList()
    val selectedCategory = viewModel?.selectedCategory?.collectAsState()?.value
    val activeTaskCount = tasks.count { it.isActive }
    val totalTaskCount = tasks.size

    val showAddDialog = remember { mutableStateOf(false) }
    val showEditDialog = remember { mutableStateOf(false) }
    val editingTask = remember { mutableStateOf<TaskEntity?>(null) }
    val newTaskTime = remember { mutableStateOf("") }
    val newTaskName = remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            border = BorderStroke(1.dp, DashboardColors.BorderColor)
        ) {
            LazyColumn(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        text = "⏰ Task Scheduler",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    // 类别过滤
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedCategory == null,
                                onClick = { viewModel?.setFilterCategory(null) },
                                label = { Text("全部") }
                            )
                        }
                        items(TaskCategory.values()) { category ->
                            FilterChip(
                                selected = selectedCategory == category,
                                onClick = { viewModel?.setFilterCategory(category) },
                                label = { Text(category.displayName) }
                            )
                        }
                    }
                }

                items(tasks, key = { it.id }) { task ->
                    TaskItemRow(
                        task = task,
                        onStatusChange = { viewModel?.toggleTaskStatus(task) },
                        onLockToggle = { viewModel?.toggleTaskLock(task) },
                        onDelete = { viewModel?.deleteTask(task) },
                        onEdit = {
                            editingTask.value = task
                            newTaskTime.value = task.time
                            newTaskName.value = task.name
                            showEditDialog.value = true
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Active Tasks  $activeTaskCount / $totalTaskCount",
                        fontSize = 12.sp,
                        color = DashboardColors.TextSecondary,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }

        // FloatingActionButton 用于添加任务
        FloatingActionButton(
            onClick = {
                newTaskTime.value = ""
                newTaskName.value = ""
                editingTask.value = null
                showAddDialog.value = true
            },
            modifier = Modifier.Companion
                .align(Alignment.Companion.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Add Task",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }

    // 添加任务对话框
    if (showAddDialog.value) {
        AddTaskDialog(
            onDismiss = { showAddDialog.value = false },
            onConfirm = { time, name, importance, category ->
                viewModel?.addTask(time, name, true, importance, false, category)
                showAddDialog.value = false
            }
        )
    }

    // 编辑任务对话框
    if (showEditDialog.value && editingTask.value != null) {
        EditTaskDialog(
            task = editingTask.value!!,
            initialTime = newTaskTime.value,
            initialName = newTaskName.value,
            onDismiss = { showEditDialog.value = false },
            onConfirm = { time, name, importance, category ->
                viewModel?.updateTask(
                    editingTask.value!!.id,
                    time,
                    name,
                    editingTask.value!!.isActive,
                    importance,
                    editingTask.value!!.isLocked,
                    category
                )
                showEditDialog.value = false
            }
        )
    }
}

@Composable
fun TaskItemRow(
    task: TaskEntity,
    onStatusChange: () -> Unit = {},
    onLockToggle: () -> Unit = {},
    onDelete: () -> Unit = {},
    onEdit: () -> Unit = {}
) {
    val backgroundColor = when (task.importance) {
        TaskImportance.IMPORTANT -> Color(0xFF4D2C2C)
        else -> DashboardColors.CardBackground
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(
            1.dp,
            if (task.importance == TaskImportance.IMPORTANT) Color(0xFFFF4444) else DashboardColors.BorderColor
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (task.importance == TaskImportance.IMPORTANT) "🔥" else "⏱",
                    fontSize = 16.sp,
                    color = if (task.importance == TaskImportance.IMPORTANT) Color.Red else Color(
                        0xFF9966FF
                    ),
                    modifier = Modifier.padding(end = 8.dp)
                )
                Column {
                    Text(text = task.time, fontSize = 12.sp, color = DashboardColors.TextSecondary)
                    Text(
                        text = task.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = DashboardColors.TextPrimary
                    )
                    Text(
                        text = " · ${task.category.displayName}",
                        fontSize = 11.sp,
                        color = DashboardColors.AccentGreen.copy(alpha = 0.7f)
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 锁定按钮（始终显示）
                IconButton(
                    onClick = onLockToggle,
                    modifier = Modifier.scale(0.8f)
                ) {
                    Icon(
                        imageVector = if (task.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = "Toggle Lock",
                        tint = if (task.isLocked) Color.Red else DashboardColors.AccentGreen
                    )
                }

                if (!task.isLocked) {
                    // 编辑按钮
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.scale(0.8f)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit Task",
                            tint = Color(0xFF9966FF)
                        )
                    }

                    // 删除按钮
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.scale(0.8f)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete Task",
                            tint = Color(0xFFFF6B6B)
                        )
                    }
                }

                // 切换按钮
                Switch(
                    checked = task.isActive,
                    onCheckedChange = { onStatusChange() },
                    modifier = Modifier.scale(0.8f),
                    enabled = !task.isLocked
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (time: String, name: String, importance: TaskImportance, category: TaskCategory) -> Unit
) {
    val name = remember { mutableStateOf("") }
    val importance = remember { mutableStateOf(TaskImportance.NORMAL) }
    val category = remember { mutableStateOf(TaskCategory.LIFE) }
    val isCategoryExpanded = remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(
        initialHour = 8,
        initialMinute = 0,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Task") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 滑动选择时间
                TimeInput(state = timePickerState)

                TextField(
                    value = name.value,
                    onValueChange = { name.value = it },
                    label = { Text("Task Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Importance", style = MaterialTheme.typography.bodyMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = importance.value == TaskImportance.NORMAL,
                            onClick = { importance.value = TaskImportance.NORMAL }
                        )
                        Text(text = "Normal")
                        Spacer(modifier = Modifier.padding(start = 8.dp))
                        RadioButton(
                            selected = importance.value == TaskImportance.IMPORTANT,
                            onClick = { importance.value = TaskImportance.IMPORTANT }
                        )
                        Text(text = "Important")
                    }
                }

                // 类别选择
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Category", style = MaterialTheme.typography.bodyMedium)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { isCategoryExpanded.value = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = category.value.displayName)
                        }
                        DropdownMenu(
                            expanded = isCategoryExpanded.value,
                            onDismissRequest = { isCategoryExpanded.value = false },
                            modifier = Modifier.fillMaxWidth(0.7f)
                        ) {
                            TaskCategory.values().forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.displayName) },
                                    onClick = {
                                        category.value = cat
                                        isCategoryExpanded.value = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.value.isNotEmpty()) {
                        val formattedTime = formatTime(timePickerState.hour, timePickerState.minute)
                        onConfirm(formattedTime, name.value, importance.value, category.value)
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskDialog(
    task: TaskEntity,
    initialTime: String,
    initialName: String,
    onDismiss: () -> Unit,
    onConfirm: (time: String, name: String, importance: TaskImportance, category: TaskCategory) -> Unit
) {
    val name = remember { mutableStateOf(initialName) }
    val importance = remember { mutableStateOf(task.importance) }
    val category = remember { mutableStateOf(task.category) }
    val isCategoryExpanded = remember { mutableStateOf(false) }
    val (parsedHour, parsedMinute) = remember(initialTime) { parseTime(initialTime) }
    val timePickerState = rememberTimePickerState(
        initialHour = parsedHour,
        initialMinute = parsedMinute,
        is24Hour = false
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Task") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 滑动选择时间
                TimePicker(state = timePickerState)

                TextField(
                    value = name.value,
                    onValueChange = { name.value = it },
                    label = { Text("Task Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Importance", style = MaterialTheme.typography.bodyMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = importance.value == TaskImportance.NORMAL,
                            onClick = { importance.value = TaskImportance.NORMAL }
                        )
                        Text(text = "Normal")
                        Spacer(modifier = Modifier.padding(start = 8.dp))
                        RadioButton(
                            selected = importance.value == TaskImportance.IMPORTANT,
                            onClick = { importance.value = TaskImportance.IMPORTANT }
                        )
                        Text(text = "Important")
                    }
                }

                // 类别选择
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Category", style = MaterialTheme.typography.bodyMedium)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { isCategoryExpanded.value = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = category.value.displayName)
                        }
                        DropdownMenu(
                            expanded = isCategoryExpanded.value,
                            onDismissRequest = { isCategoryExpanded.value = false },
                            modifier = Modifier.fillMaxWidth(0.7f)
                        ) {
                            TaskCategory.values().forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.displayName) },
                                    onClick = {
                                        category.value = cat
                                        isCategoryExpanded.value = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.value.isNotEmpty()) {
                        val formattedTime = formatTime(timePickerState.hour, timePickerState.minute)
                        onConfirm(formattedTime, name.value, importance.value, category.value)
                    }
                }
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}