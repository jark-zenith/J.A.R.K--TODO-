package com.example.data.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
enum class TaskStatus(val value: String, val label: String) {
    TODO("todo", "To do"),
    IN_PROGRESS("in-progress", "In progress"),
    COMPLETED("completed", "Completed");

    companion object {
        fun fromString(value: String): TaskStatus {
            return entries.firstOrNull { it.value.equals(value, ignoreCase = true) || it.name.equals(value, ignoreCase = true) } ?: TODO
        }
    }
}

@Serializable
enum class TaskPriority(val value: String, val label: String) {
    LOW("low", "Low"),
    MEDIUM("medium", "Medium"),
    HIGH("high", "High"),
    URGENT("urgent", "Urgent");

    companion object {
        fun fromString(value: String): TaskPriority {
            return entries.firstOrNull { it.value.equals(value, ignoreCase = true) || it.name.equals(value, ignoreCase = true) } ?: MEDIUM
        }
    }
}

@Serializable
data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val status: TaskStatus = TaskStatus.TODO,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val category: String = "Personal",
    val dueDate: String = "", // ISO date YYYY-MM-DD
    val createdAt: String = "",
    val updatedAt: String = "",
    val completedAt: String? = null,
    val tags: List<String> = emptyList(),
    val projectId: String? = null,
    val aiGenerated: Boolean = false
)

@Serializable
data class TaskDraft(
    val title: String,
    val description: String = "",
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val category: String = "Personal",
    val dueDate: String = "",
    val tags: List<String> = emptyList()
)

val DEFAULT_CATEGORIES = listOf(
    "Personal",
    "Study",
    "Development",
    "Business",
    "Projects",
    "Other"
)

enum class ViewMode(val label: String) {
    DASHBOARD("Command center"),
    ALL("All tasks"),
    TODAY("Today"),
    UPCOMING("Upcoming"),
    OVERVIEW("Overview"),
    OVERDUE("Overdue"),
    COMPLETED("Completed"),
    PRIORITY("High priority")
}

@Serializable
enum class JarkActionName {
    CREATE_TASK,
    UPDATE_TASK,
    DELETE_TASK,
    COMPLETE_TASK,
    GET_TASKS,
    SEARCH_TASKS,
    GET_TODAY_TASKS,
    GET_UPCOMING_TASKS,
    GET_OVERDUE_TASKS,
    NONE
}

@Serializable
data class JarkAction(
    val action: JarkActionName,
    val taskId: String? = null,
    val title: String? = null,
    val description: String? = null,
    val priority: TaskPriority? = null,
    val category: String? = null,
    val dueDate: String? = null,
    val status: TaskStatus? = null,
    val tags: List<String>? = null,
    val query: String? = null
)

data class JarkExecutionResult(
    val reply: String,
    val changed: Boolean = false,
    val affectedTaskTitle: String? = null
)
