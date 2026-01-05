package auto.script.feature.scheduler.db

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TaskImportance {
    NORMAL, IMPORTANT
}

enum class TaskCategory(val displayName: String) {
    LIFE("生活"),
    FITNESS("健身"),
    DAILY("日常事务"),
    OTHER("其他")
}

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val time: String,
    val name: String,
    val isActive: Boolean,
    val importance: TaskImportance = TaskImportance.NORMAL,
    val isLocked: Boolean = false,
    val category: TaskCategory = TaskCategory.LIFE
)
