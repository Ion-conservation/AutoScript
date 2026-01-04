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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import auto.script.feature.scheduler.db.TaskEntity
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
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                items(tasks, key = { it.id }) { task ->
                    TaskItemRow(
                        task = task,
                        onStatusChange = { viewModel?.toggleTaskStatus(task) },
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
            onConfirm = { time, name ->
                viewModel?.addTask(time, name, true)
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
            onConfirm = { time, name ->
                viewModel?.updateTask(
                    editingTask.value!!.id,
                    time,
                    name,
                    editingTask.value!!.isActive
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
    onDelete: () -> Unit = {},
    onEdit: () -> Unit = {}
) {
    Box(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .background(
                color = Color(0xFF1A1A1A),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            )
            .padding(12.dp)
            .padding(bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier.Companion.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Companion.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.Companion.CenterVertically,
                modifier = Modifier.Companion.weight(1f)
            ) {
                Text(
                    text = "⏱",
                    fontSize = 16.sp,
                    color = Color(0xFF9966FF),
                    modifier = Modifier.Companion.padding(end = 8.dp)
                )
                Column {
                    Text(text = task.time, fontSize = 12.sp, color = DashboardColors.TextSecondary)
                    Text(
                        text = task.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Companion.Bold,
                        color = DashboardColors.TextPrimary
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.Companion.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 编辑按鑵
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.Companion.scale(0.8f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit Task",
                        tint = Color(0xFF9966FF)
                    )
                }

                // 删除按鑵
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.Companion.scale(0.8f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete Task",
                        tint = Color(0xFFFF6B6B)
                    )
                }

                // 切换挡
                Switch(
                    checked = task.isActive,
                    onCheckedChange = { onStatusChange() },
                    modifier = Modifier.Companion.scale(0.8f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (time: String, name: String) -> Unit
) {
    val name = remember { mutableStateOf("") }
    val timePickerState = rememberTimePickerState(
        initialHour = 8,
        initialMinute = 0,
        is24Hour = false
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
                TimePicker(state = timePickerState)

                TextField(
                    value = name.value,
                    onValueChange = { name.value = it },
                    label = { Text("Task Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.value.isNotEmpty()) {
                        val formattedTime = formatTime(timePickerState.hour, timePickerState.minute)
                        onConfirm(formattedTime, name.value)
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
    onConfirm: (time: String, name: String) -> Unit
) {
    val name = remember { mutableStateOf(initialName) }
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
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.value.isNotEmpty()) {
                        val formattedTime = formatTime(timePickerState.hour, timePickerState.minute)
                        onConfirm(formattedTime, name.value)
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