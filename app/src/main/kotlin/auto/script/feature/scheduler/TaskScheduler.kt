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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import auto.script.ui.theme.DashboardColors

@Composable
fun TaskScheduler() {
    Box(
        modifier = Modifier.Companion
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.Companion
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            border = BorderStroke(1.dp, DashboardColors.BorderColor)
        ) {
            Column(
                modifier = Modifier.Companion.padding(16.dp)
            ) {
                Text(
                    text = "⏰ Task Scheduler",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.Companion.padding(bottom = 16.dp)
                )

                val tasks = listOf(
                    TaskItemData("08:00 AM", "Morning Workout", true),
                    TaskItemData("09:30 AM", "Team Standup", true),
                    TaskItemData("12:00 PM", "Lunch Break", false),
                    TaskItemData("03:00 PM", "Code Review", true),
                    TaskItemData("06:00 PM", "Evening Walk", false)
                )

                tasks.forEach { task ->
                    TaskItemRow(task)
                }

                Spacer(modifier = Modifier.Companion.height(12.dp))

                Text(
                    text = "Active Tasks  3 / 5",
                    fontSize = 12.sp,
                    color = DashboardColors.TextSecondary,
                    modifier = Modifier.Companion.align(Alignment.Companion.CenterHorizontally)
                )
            }
        }
    }
}

data class TaskItemData(
    val time: String,
    val name: String,
    val isActive: Boolean
)

@Composable
fun TaskItemRow(task: TaskItemData) {
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
            Switch(
                checked = task.isActive,
                onCheckedChange = { },
                modifier = Modifier.Companion.scale(0.8f)
            )
        }
    }
}