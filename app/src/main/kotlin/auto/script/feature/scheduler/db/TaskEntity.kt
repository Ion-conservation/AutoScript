package auto.script.feature.scheduler.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val time: String,
    val name: String,
    val isActive: Boolean
)
