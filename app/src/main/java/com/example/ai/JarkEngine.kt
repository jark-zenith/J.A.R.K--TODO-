package com.example.ai

import com.example.data.model.JarkAction
import com.example.data.model.JarkActionName
import com.example.data.model.JarkExecutionResult
import com.example.data.model.Task
import com.example.data.model.TaskDraft
import com.example.data.model.TaskPriority
import com.example.data.model.TaskStatus
import com.example.data.repository.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class JarkEngine(private val repository: TaskRepository) {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    suspend fun executeCommand(command: String): JarkExecutionResult = withContext(Dispatchers.IO) {
        val trimmed = command.trim()
        if (trimmed.isBlank()) {
            return@withContext JarkExecutionResult("Command cannot be empty.")
        }

        var action: JarkAction? = null

        // If Gemini API Key is configured, attempt Gemini smart intent interpretation
        if (GeminiClient.hasApiKey()) {
            try {
                action = parseWithGemini(trimmed)
            } catch (_: Exception) {
                // Fallback to offline parser
            }
        }

        if (action == null || action.action == JarkActionName.NONE) {
            action = JarkCommandParser.parseOffline(trimmed)
        }

        val tasks = repository.getTasksSnapshot()
        executeAction(action, tasks)
    }

    private suspend fun parseWithGemini(commandText: String): JarkAction? {
        val systemPrompt = """
            You are J.A.R.K., a concise task-management intent parser. Return JSON only with fields:
            - action: "CREATE_TASK" | "UPDATE_TASK" | "DELETE_TASK" | "COMPLETE_TASK" | "GET_TASKS" | "SEARCH_TASKS" | "GET_TODAY_TASKS" | "GET_UPCOMING_TASKS" | "GET_OVERDUE_TASKS" | "NONE"
            - title: string (for new task title)
            - query: string (search keyword or existing task title reference)
            - priority: "low" | "medium" | "high" | "urgent"
            - category: string ("Personal" | "Study" | "Development" | "Business" | "Projects" | "Other")
            - dueDate: string ("today", "tomorrow", "in N days", or "YYYY-MM-DD")
            - tags: string array
            - reply: brief confirmation string
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(parts = listOf(GeminiPart(text = commandText)))
            ),
            generationConfig = GeminiGenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.2f
            ),
            systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPrompt)))
        )

        val response = GeminiClient.service.generateContent(GeminiClient.getApiKey(), request)
        val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: return null
        val payload = json.decodeFromString<JarkAiActionPayload>(text)

        val actionName = when (payload.action.uppercase()) {
            "CREATE_TASK", "CREATE" -> JarkActionName.CREATE_TASK
            "UPDATE_TASK", "UPDATE" -> JarkActionName.UPDATE_TASK
            "DELETE_TASK", "DELETE" -> JarkActionName.DELETE_TASK
            "COMPLETE_TASK", "COMPLETE" -> JarkActionName.COMPLETE_TASK
            "GET_TASKS", "LIST" -> JarkActionName.GET_TASKS
            "SEARCH_TASKS", "SEARCH" -> JarkActionName.SEARCH_TASKS
            "GET_TODAY_TASKS", "TODAY" -> JarkActionName.GET_TODAY_TASKS
            "GET_UPCOMING_TASKS", "UPCOMING" -> JarkActionName.GET_UPCOMING_TASKS
            "GET_OVERDUE_TASKS", "OVERDUE" -> JarkActionName.GET_OVERDUE_TASKS
            else -> JarkActionName.NONE
        }

        return JarkAction(
            action = actionName,
            title = payload.title,
            description = payload.description,
            priority = payload.priority?.let { TaskPriority.fromString(it) },
            category = payload.category,
            dueDate = JarkCommandParser.normalizeDueDate(payload.dueDate),
            tags = payload.tags,
            query = payload.query
        )
    }

    private suspend fun executeAction(action: JarkAction, tasks: List<Task>): JarkExecutionResult {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

        return when (action.action) {
            JarkActionName.CREATE_TASK -> {
                val title = action.title?.trim()
                if (title.isNullOrBlank()) {
                    return JarkExecutionResult("I need a task title before I can create it.")
                }
                val draft = TaskDraft(
                    title = title,
                    description = action.description?.trim() ?: "",
                    priority = action.priority ?: TaskPriority.MEDIUM,
                    category = action.category?.ifBlank { "Personal" } ?: "Personal",
                    dueDate = action.dueDate ?: "",
                    tags = action.tags ?: emptyList()
                )
                val created = repository.createTask(draft, aiGenerated = true)
                val dueNote = if (created.dueDate.isNotBlank()) " for ${created.dueDate}" else ""
                JarkExecutionResult(
                    reply = "Done. I added “${created.title}”$dueNote [${created.priority.label} priority].",
                    changed = true,
                    affectedTaskTitle = created.title
                )
            }
            JarkActionName.COMPLETE_TASK -> {
                val match = findMatchingTask(tasks, action.query ?: action.title)
                    ?: return JarkExecutionResult("I could not find a task matching “${action.query ?: action.title}”.")

                if (match.status == TaskStatus.COMPLETED) {
                    JarkExecutionResult("“${match.title}” is already marked as completed.")
                } else {
                    repository.toggleTaskStatus(match)
                    JarkExecutionResult(
                        reply = "Done. “${match.title}” is now marked completed.",
                        changed = true,
                        affectedTaskTitle = match.title
                    )
                }
            }
            JarkActionName.DELETE_TASK -> {
                val match = findMatchingTask(tasks, action.query ?: action.title)
                    ?: return JarkExecutionResult("I could not find a task matching “${action.query ?: action.title}”.")

                repository.deleteTask(match.id)
                JarkExecutionResult(
                    reply = "Done. I deleted “${match.title}”.",
                    changed = true,
                    affectedTaskTitle = match.title
                )
            }
            JarkActionName.GET_TODAY_TASKS -> {
                val todayTasks = TaskRepository.getTodayTasks(tasks, today)
                if (todayTasks.isEmpty()) {
                    JarkExecutionResult("You have no tasks due today. You are all clear.")
                } else {
                    val titles = todayTasks.joinToString(", ") { "“${it.title}”" }
                    JarkExecutionResult("You have ${todayTasks.size} ${if (todayTasks.size == 1) "task" else "tasks"} due today: $titles.")
                }
            }
            JarkActionName.GET_OVERDUE_TASKS -> {
                val overdue = TaskRepository.getOverdueTasks(tasks, today)
                if (overdue.isEmpty()) {
                    JarkExecutionResult("Great news! You have no overdue tasks.")
                } else {
                    val titles = overdue.joinToString(", ") { "“${it.title}” (${it.dueDate})" }
                    JarkExecutionResult("Attention needed: ${overdue.size} overdue ${if (overdue.size == 1) "task" else "tasks"}: $titles.")
                }
            }
            JarkActionName.GET_UPCOMING_TASKS -> {
                val upcoming = TaskRepository.getUpcomingTasks(tasks, today)
                if (upcoming.isEmpty()) {
                    JarkExecutionResult("No upcoming tasks scheduled for future dates.")
                } else {
                    val titles = upcoming.take(4).joinToString(", ") { "“${it.title}” (${it.dueDate})" }
                    JarkExecutionResult("${upcoming.size} upcoming ${if (upcoming.size == 1) "task" else "tasks"}: $titles.")
                }
            }
            JarkActionName.GET_TASKS -> {
                val open = tasks.filter { it.status != TaskStatus.COMPLETED }
                if (open.isEmpty()) {
                    JarkExecutionResult("You have 0 open tasks. All done!")
                } else {
                    val titles = open.take(5).joinToString("; ") { it.title }
                    JarkExecutionResult("You have ${open.size} open ${if (open.size == 1) "task" else "tasks"}: $titles.")
                }
            }
            JarkActionName.SEARCH_TASKS -> {
                val q = action.query ?: action.title ?: ""
                val matches = TaskRepository.searchTasks(tasks, q)
                if (matches.isEmpty()) {
                    JarkExecutionResult("No tasks found matching “$q”.")
                } else {
                    val titles = matches.take(5).joinToString(", ") { "“${it.title}”" }
                    JarkExecutionResult("Found ${matches.size} matching ${if (matches.size == 1) "task" else "tasks"}: $titles.")
                }
            }
            JarkActionName.UPDATE_TASK -> {
                val match = findMatchingTask(tasks, action.query ?: action.title)
                    ?: return JarkExecutionResult("I could not find the task to update.")

                val updatedDraft = TaskDraft(
                    title = action.title ?: match.title,
                    description = action.description ?: match.description,
                    priority = action.priority ?: match.priority,
                    category = action.category ?: match.category,
                    dueDate = action.dueDate ?: match.dueDate,
                    tags = action.tags ?: match.tags
                )
                val updated = repository.updateTask(match, updatedDraft)
                JarkExecutionResult(
                    reply = "Done. I updated “${updated.title}”.",
                    changed = true,
                    affectedTaskTitle = updated.title
                )
            }
            JarkActionName.NONE -> {
                JarkExecutionResult("I am standing by. Try: “Add task Review code due tomorrow”, “Complete TypeScript”, or “What should I work on next?”")
            }
        }
    }

    private fun findMatchingTask(tasks: List<Task>, query: String?): Task? {
        if (query.isNullOrBlank()) return null
        val q = query.trim().lowercase()
        val exact = tasks.firstOrNull { it.title.trim().lowercase() == q }
        if (exact != null) return exact
        return tasks.firstOrNull { it.title.lowercase().contains(q) }
    }
}
