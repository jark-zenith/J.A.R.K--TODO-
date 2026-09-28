package com.example.data.repository

import com.example.data.local.TaskDao
import com.example.data.local.TaskEntity
import com.example.data.model.Task
import com.example.data.model.TaskDraft
import com.example.data.model.TaskPriority
import com.example.data.model.TaskStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.UUID

class TaskRepository(private val taskDao: TaskDao) {

    val allTasks: Flow<List<Task>> = taskDao.getAllTasks().map { entities ->
        entities.map { it.toTask() }
    }

    suspend fun getTasksSnapshot(): List<Task> {
        return taskDao.getTasksSnapshot().map { it.toTask() }
    }

    suspend fun seedStarterTasksIfEmpty() {
        if (taskDao.getTaskCount() == 0) {
            val now = Instant.now().toString()
            val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            val tomorrow = LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
            val yesterday = LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)

            val starters = listOf(
                Task(
                    id = "starter-1",
                    title = "Map the next J.A.R.K. milestone",
                    description = "Turn the product vision into three shippable outcomes.",
                    status = TaskStatus.IN_PROGRESS,
                    priority = TaskPriority.HIGH,
                    category = "Projects",
                    dueDate = today,
                    createdAt = now,
                    updatedAt = now,
                    completedAt = null,
                    tags = listOf("planning", "jark")
                ),
                Task(
                    id = "starter-2",
                    title = "Study TypeScript patterns",
                    description = "Review discriminated unions and service boundaries.",
                    status = TaskStatus.TODO,
                    priority = TaskPriority.MEDIUM,
                    category = "Study",
                    dueDate = tomorrow,
                    createdAt = now,
                    updatedAt = now,
                    completedAt = null,
                    tags = listOf("learning")
                ),
                Task(
                    id = "starter-3",
                    title = "Archive completed notes",
                    description = "",
                    status = TaskStatus.COMPLETED,
                    priority = TaskPriority.LOW,
                    category = "Personal",
                    dueDate = "",
                    createdAt = now,
                    updatedAt = now,
                    completedAt = yesterday,
                    tags = emptyList()
                )
            )
            taskDao.insertAll(starters.map { TaskEntity.fromTask(it) })
        }
    }

    suspend fun resetToStarters() {
        taskDao.deleteAllTasks()
        seedStarterTasksIfEmpty()
    }

    suspend fun createTask(draft: TaskDraft, aiGenerated: Boolean = false): Task {
        val now = Instant.now().toString()
        val task = Task(
            id = UUID.randomUUID().toString(),
            title = draft.title.trim(),
            description = draft.description.trim(),
            status = TaskStatus.TODO,
            priority = draft.priority,
            category = draft.category.ifBlank { "Personal" },
            dueDate = draft.dueDate,
            createdAt = now,
            updatedAt = now,
            completedAt = null,
            tags = draft.tags.map { it.trim() }.filter { it.isNotBlank() },
            aiGenerated = aiGenerated
        )
        taskDao.insertTask(TaskEntity.fromTask(task))
        return task
    }

    suspend fun updateTask(task: Task, changes: TaskDraft): Task {
        val now = Instant.now().toString()
        val updated = task.copy(
            title = changes.title.trim().ifBlank { task.title },
            description = changes.description.trim(),
            priority = changes.priority,
            category = changes.category.ifBlank { task.category },
            dueDate = changes.dueDate,
            tags = changes.tags.map { it.trim() }.filter { it.isNotBlank() },
            updatedAt = now
        )
        taskDao.updateTask(TaskEntity.fromTask(updated))
        return updated
    }

    suspend fun updateTaskDirect(task: Task): Task {
        val now = Instant.now().toString()
        val updated = task.copy(updatedAt = now)
        taskDao.updateTask(TaskEntity.fromTask(updated))
        return updated
    }

    suspend fun toggleTaskStatus(task: Task): Task {
        val now = Instant.now().toString()
        val newStatus = if (task.status == TaskStatus.COMPLETED) TaskStatus.TODO else TaskStatus.COMPLETED
        val completedAt = if (newStatus == TaskStatus.COMPLETED) now else null
        val updated = task.copy(
            status = newStatus,
            completedAt = completedAt,
            updatedAt = now
        )
        taskDao.updateTask(TaskEntity.fromTask(updated))
        return updated
    }

    suspend fun deleteTask(taskId: String) {
        taskDao.deleteTaskById(taskId)
    }

    companion object {
        fun searchTasks(tasks: List<Task>, query: String): List<Task> {
            val q = query.trim().lowercase()
            if (q.isEmpty()) return tasks
            return tasks.filter { task ->
                val combined = listOf(
                    task.title,
                    task.description,
                    task.category,
                    task.tags.joinToString(" ")
                ).joinToString(" ").lowercase()
                combined.contains(q)
            }
        }

        fun getTodayTasks(tasks: List<Task>, today: String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)): List<Task> {
            return tasks.filter { it.dueDate == today && it.status != TaskStatus.COMPLETED }
        }

        fun getUpcomingTasks(tasks: List<Task>, today: String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)): List<Task> {
            return tasks.filter { it.dueDate.isNotBlank() && it.dueDate > today && it.status != TaskStatus.COMPLETED }
        }

        fun getOverdueTasks(tasks: List<Task>, today: String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)): List<Task> {
            return tasks.filter { it.dueDate.isNotBlank() && it.dueDate < today && it.status != TaskStatus.COMPLETED }
        }

        fun getPriorityTasks(tasks: List<Task>): List<Task> {
            return tasks.filter { (it.priority == TaskPriority.HIGH || it.priority == TaskPriority.URGENT) && it.status != TaskStatus.COMPLETED }
        }
    }
}
