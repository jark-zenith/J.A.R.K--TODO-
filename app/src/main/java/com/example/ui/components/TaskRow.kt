package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Task
import com.example.data.model.TaskPriority
import com.example.data.model.TaskStatus
import com.example.ui.theme.JarkAmber
import com.example.ui.theme.JarkAmberContainer
import com.example.ui.theme.JarkBlue
import com.example.ui.theme.JarkBlueContainer
import com.example.ui.theme.JarkBorder
import com.example.ui.theme.JarkGreen
import com.example.ui.theme.JarkGreenContainer
import com.example.ui.theme.JarkLowContainer
import com.example.ui.theme.JarkLowSlate
import com.example.ui.theme.JarkRed
import com.example.ui.theme.JarkRedContainer
import com.example.ui.theme.JarkSurface
import com.example.ui.theme.JarkTextMuted
import com.example.ui.theme.JarkTextPrimary
import com.example.ui.theme.JarkTextSecondary
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TaskRow(
    task: Task,
    onToggle: (Task) -> Unit,
    onEdit: (Task) -> Unit,
    onDelete: ((Task) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isCompleted = task.status == TaskStatus.COMPLETED
    val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    val isOverdue = !isCompleted && task.dueDate.isNotBlank() && task.dueDate < today

    Surface(
        color = JarkSurface,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, JarkBorder, RoundedCornerShape(8.dp))
            .testTag("task_item_${task.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            // Checkbox button
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isCompleted) JarkGreenContainer else Color.Transparent)
                    .clickable { onToggle(task) }
                    .testTag("toggle_task_${task.id}"),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = JarkGreen,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Circle,
                        contentDescription = "Mark complete",
                        tint = JarkTextMuted,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Main task content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onEdit(task) }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                            color = if (isCompleted) JarkTextMuted else JarkTextPrimary,
                            fontWeight = FontWeight.SemiBold
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    PriorityBadge(priority = task.priority)
                }

                if (task.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = JarkTextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Meta line: category, due date, tags
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Category indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        CategoryDot(category = task.category)
                        Text(
                            text = task.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = JarkTextSecondary
                        )
                    }

                    if (task.dueDate.isNotBlank()) {
                        Text(text = "•", color = JarkBorder)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = if (isOverdue) JarkRed else JarkTextMuted,
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = formatDueDateString(task.dueDate, today),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isOverdue) JarkRed else JarkTextSecondary,
                                fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }

                    if (task.tags.isNotEmpty()) {
                        Text(text = "•", color = JarkBorder)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalOffer,
                                contentDescription = null,
                                tint = JarkTextMuted,
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = task.tags.joinToString(", "),
                                style = MaterialTheme.typography.labelSmall,
                                color = JarkTextMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Edit & Delete actions
            IconButton(
                onClick = { onEdit(task) },
                modifier = Modifier
                    .size(36.dp)
                    .testTag("edit_task_${task.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit task",
                    tint = JarkTextMuted,
                    modifier = Modifier.size(17.dp)
                )
            }

            if (onDelete != null) {
                IconButton(
                    onClick = { onDelete(task) },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("delete_task_${task.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete task",
                        tint = JarkTextMuted,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PriorityBadge(priority: TaskPriority) {
    val (bgColor, textColor) = when (priority) {
        TaskPriority.URGENT -> Pair(JarkRedContainer, JarkRed)
        TaskPriority.HIGH -> Pair(JarkAmberContainer, JarkAmber)
        TaskPriority.MEDIUM -> Pair(JarkBlueContainer, JarkBlue)
        TaskPriority.LOW -> Pair(JarkLowContainer, JarkLowSlate)
    }

    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = priority.value.uppercase(Locale.ROOT),
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            ),
            color = textColor
        )
    }
}

@Composable
fun CategoryDot(category: String) {
    val color = when (category.lowercase(Locale.ROOT)) {
        "study" -> JarkGreen
        "development" -> JarkRed
        "business" -> JarkAmber
        "projects" -> JarkBlue
        else -> JarkBlue
    }
    Box(
        modifier = Modifier
            .size(6.dp)
            .background(color, CircleShape)
    )
}

fun formatDueDateString(date: String, today: String): String {
    if (date == today) return "Today"
    return try {
        val parsed = LocalDate.parse(date)
        parsed.format(DateTimeFormatter.ofPattern("MMM d"))
    } catch (_: Exception) {
        date
    }
}
