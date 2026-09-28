package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.Task
import com.example.data.model.TaskPriority
import com.example.data.model.TaskStatus

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String = "",
    val status: TaskStatus = TaskStatus.TODO,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val category: String = "Personal",
    val dueDate: String = "",
    val createdAt: String = "",
    val updatedAt: String = "",
    val completedAt: String? = null,
    val tags: List<String> = emptyList(),
    val projectId: String? = null,
    val aiGenerated: Boolean = false
) {
    fun toTask(): Task = Task(
        id = id,
        title = title,
        description = description,
        status = status,
        priority = priority,
        category = category,
        dueDate = dueDate,
        createdAt = createdAt,
        updatedAt = updatedAt,
        completedAt = completedAt,
        tags = tags,
        projectId = projectId,
        aiGenerated = aiGenerated
    )

    companion object {
        fun fromTask(task: Task): TaskEntity = TaskEntity(
            id = task.id,
            title = task.title,
            description = task.description,
            status = task.status,
            priority = task.priority,
            category = task.category,
            dueDate = task.dueDate,
            createdAt = task.createdAt,
            updatedAt = task.updatedAt,
            completedAt = task.completedAt,
            tags = task.tags,
            projectId = task.projectId,
            aiGenerated = task.aiGenerated
        )
    }
}
