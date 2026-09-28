package com.example.ai

import com.example.data.model.JarkAction
import com.example.data.model.JarkActionName
import com.example.data.model.TaskPriority
import com.example.data.model.TaskStatus
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object JarkCommandParser {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun normalizeDueDate(input: String?): String {
        if (input.isNullOrBlank()) return ""
        val text = input.trim().lowercase(Locale.ROOT)

        val today = LocalDate.now()
        if (text == "today") return today.format(dateFormatter)
        if (text == "tomorrow") return today.plusDays(1).format(dateFormatter)

        val inDaysMatch = Regex("in (\\d+) days?").find(text)
        if (inDaysMatch != null) {
            val days = inDaysMatch.groupValues[1].toLongOrNull() ?: 0L
            return today.plusDays(days).format(dateFormatter)
        }

        if (text.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$"))) {
            return try {
                LocalDate.parse(text, dateFormatter).format(dateFormatter)
            } catch (_: Exception) {
                ""
            }
        }
        return ""
    }

    fun parseOffline(command: String): JarkAction {
        val trimmed = command.trim()
        val lower = trimmed.lowercase(Locale.ROOT)

        // 1. Check questions / signals
        if (lower.contains("what should i work on") || lower.contains("next task") || lower.contains("top priority")) {
            return JarkAction(action = JarkActionName.GET_TODAY_TASKS)
        }
        if (lower.contains("what is due today") || lower.contains("due today") || lower.contains("today's task") || lower.contains("today tasks")) {
            return JarkAction(action = JarkActionName.GET_TODAY_TASKS)
        }
        if (lower.contains("overdue") || lower.contains("past due") || lower.contains("late tasks")) {
            return JarkAction(action = JarkActionName.GET_OVERDUE_TASKS)
        }
        if (lower.contains("upcoming") || lower.contains("due this week") || lower.contains("future tasks")) {
            return JarkAction(action = JarkActionName.GET_UPCOMING_TASKS)
        }
        if (lower.contains("show all") || lower.contains("list open") || lower.contains("list tasks") || lower.contains("open tasks")) {
            return JarkAction(action = JarkActionName.GET_TASKS)
        }

        // 2. Complete / Finish / Check
        val completeMatch = Regex("^(?:complete|finish|check off|done with|mark done)\\s+(?:task\\s+)?(.+)", RegexOption.IGNORE_CASE).find(trimmed)
        if (completeMatch != null) {
            val taskQuery = completeMatch.groupValues[1].trim()
            return JarkAction(action = JarkActionName.COMPLETE_TASK, query = taskQuery)
        }

        // 3. Delete / Remove
        val deleteMatch = Regex("^(?:delete|remove|discard|trash)\\s+(?:task\\s+)?(.+)", RegexOption.IGNORE_CASE).find(trimmed)
        if (deleteMatch != null) {
            val taskQuery = deleteMatch.groupValues[1].trim()
            return JarkAction(action = JarkActionName.DELETE_TASK, query = taskQuery)
        }

        // 4. Search
        val searchMatch = Regex("^(?:search|find|lookup|look for)\\s+(?:tasks?\\s+)?(?:for\\s+)?(.+)", RegexOption.IGNORE_CASE).find(trimmed)
        if (searchMatch != null) {
            val q = searchMatch.groupValues[1].trim()
            return JarkAction(action = JarkActionName.SEARCH_TASKS, query = q)
        }

        // 5. Create / Add / Remind
        val createPrefix = Regex("^(?:create|add|new|remind me to|schedule|make)\\s+(?:a\\s+)?(?:task\\s+)?(?:to\\s+)?", RegexOption.IGNORE_CASE)
        val isExplicitCreate = createPrefix.containsMatchIn(trimmed)
        val content = if (isExplicitCreate) trimmed.replace(createPrefix, "").trim() else trimmed

        if (content.isNotBlank()) {
            var extractedTitle = content
            var detectedPriority = TaskPriority.MEDIUM
            var detectedCategory = "Personal"
            var detectedDueDate = ""
            val tags = mutableListOf<String>()

            // Detect priority keywords
            if (lower.contains("urgent priority") || lower.contains("priority urgent") || lower.contains("asap")) {
                detectedPriority = TaskPriority.URGENT
                extractedTitle = extractedTitle.replace(Regex("(?i)\\b(with\\s+)?(urgent priority|priority urgent|asap)\\b"), "")
            } else if (lower.contains("high priority") || lower.contains("priority high") || lower.contains("important")) {
                detectedPriority = TaskPriority.HIGH
                extractedTitle = extractedTitle.replace(Regex("(?i)\\b(with\\s+)?(high priority|priority high|important)\\b"), "")
            } else if (lower.contains("low priority") || lower.contains("priority low")) {
                detectedPriority = TaskPriority.LOW
                extractedTitle = extractedTitle.replace(Regex("(?i)\\b(with\\s+)?(low priority|priority low)\\b"), "")
            }

            // Detect due date
            if (lower.contains("due today") || lower.contains("for today")) {
                detectedDueDate = normalizeDueDate("today")
                extractedTitle = extractedTitle.replace(Regex("(?i)\\b(due today|for today|today)\\b"), "")
            } else if (lower.contains("due tomorrow") || lower.contains("for tomorrow")) {
                detectedDueDate = normalizeDueDate("tomorrow")
                extractedTitle = extractedTitle.replace(Regex("(?i)\\b(due tomorrow|for tomorrow|tomorrow)\\b"), "")
            }

            // Detect category
            val categories = listOf("Study", "Development", "Business", "Projects", "Other", "Personal")
            for (cat in categories) {
                if (lower.contains("in ${cat.lowercase(Locale.ROOT)}") || lower.contains("category ${cat.lowercase(Locale.ROOT)}")) {
                    detectedCategory = cat
                    extractedTitle = extractedTitle.replace(Regex("(?i)\\b(in|category)\\s+${cat}\\b"), "")
                    break
                }
            }

            // Clean title
            extractedTitle = extractedTitle.replace(Regex("^[\\s,.-]+|[\\s,.-]+$"), "").trim()
            if (extractedTitle.isNotBlank()) {
                return JarkAction(
                    action = JarkActionName.CREATE_TASK,
                    title = extractedTitle,
                    priority = detectedPriority,
                    category = detectedCategory,
                    dueDate = detectedDueDate,
                    tags = tags
                )
            }
        }

        return JarkAction(action = JarkActionName.NONE)
    }
}
