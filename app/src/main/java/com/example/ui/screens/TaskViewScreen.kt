package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DEFAULT_CATEGORIES
import com.example.data.model.Task
import com.example.data.model.TaskPriority
import com.example.data.model.TaskStatus
import com.example.data.model.ViewMode
import com.example.ui.components.TaskRow
import com.example.ui.theme.JarkBackground
import com.example.ui.theme.JarkBlue
import com.example.ui.theme.JarkBlueContainer
import com.example.ui.theme.JarkBorder
import com.example.ui.theme.JarkRed
import com.example.ui.theme.JarkSurface
import com.example.ui.theme.JarkSurfaceVariant
import com.example.ui.theme.JarkTextMuted
import com.example.ui.theme.JarkTextPrimary
import com.example.ui.theme.JarkTextSecondary
import com.example.ui.viewmodel.TaskUiState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TaskViewScreen(
    uiState: TaskUiState,
    onToggleTask: (Task) -> Unit,
    onEditTask: (Task) -> Unit,
    onDeleteTask: (Task) -> Unit,
    onCreateTask: () -> Unit,
    onSearchChange: (String) -> Unit,
    onStatusFilterChange: (TaskStatus?) -> Unit,
    onPriorityFilterChange: (TaskPriority?) -> Unit,
    onCategoryFilterChange: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var taskToDelete by remember { mutableStateOf<Task?>(null) }

    val viewTitle = when (uiState.currentView) {
        ViewMode.ALL -> if (uiState.categoryFilter != null) uiState.categoryFilter!! else "All tasks"
        ViewMode.TODAY -> "Today"
        ViewMode.UPCOMING -> "Upcoming"
        ViewMode.OVERDUE -> "Overdue"
        ViewMode.COMPLETED -> "Completed"
        ViewMode.PRIORITY -> "High priority"
        else -> "Tasks"
    }

    val subtitle = "${uiState.visibleTasks.size} ${if (uiState.visibleTasks.size == 1) "task" else "tasks"} in this view"

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "TASK REGISTRY / ${viewTitle.uppercase()}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.3.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = JarkTextMuted
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = viewTitle,
                                style = MaterialTheme.typography.headlineLarge,
                                color = JarkTextPrimary
                            )
                            Text(
                                text = ".",
                                style = MaterialTheme.typography.headlineLarge,
                                color = JarkBlue
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (uiState.searchQuery.isNotBlank()) "$subtitle matching “${uiState.searchQuery}”" else subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = JarkTextSecondary
                        )
                    }

                    Button(
                        onClick = onCreateTask,
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = JarkBlue,
                            contentColor = JarkBackground
                        ),
                        modifier = Modifier.testTag("taskview_new_task_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New task", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search tasks by title, tag, or description...", color = JarkTextMuted, style = MaterialTheme.typography.bodyMedium) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = JarkTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("task_search_field"),
                shape = RoundedCornerShape(6.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = JarkSurface,
                    unfocusedContainerColor = JarkSurface,
                    focusedBorderColor = JarkBlue,
                    unfocusedBorderColor = JarkBorder,
                    focusedTextColor = JarkTextPrimary,
                    unfocusedTextColor = JarkTextPrimary
                )
            )
        }

        // Filter Bar (Chips row)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filters",
                        tint = JarkTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    // Status Filters
                    listOf(
                        null to "All Status",
                        TaskStatus.TODO to "To do",
                        TaskStatus.IN_PROGRESS to "In progress",
                        TaskStatus.COMPLETED to "Completed"
                    ).forEach { (status, label) ->
                        val isSelected = uiState.statusFilter == status
                        Surface(
                            color = if (isSelected) JarkBlueContainer else JarkSurface,
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier
                                .padding(end = 6.dp)
                                .border(1.dp, if (isSelected) JarkBlue else JarkBorder, RoundedCornerShape(4.dp))
                                .clickable { onStatusFilterChange(status) }
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) JarkBlue else JarkTextSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Priority Filters
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    listOf(
                        null to "All Priority",
                        TaskPriority.URGENT to "Urgent",
                        TaskPriority.HIGH to "High",
                        TaskPriority.MEDIUM to "Medium",
                        TaskPriority.LOW to "Low"
                    ).forEach { (priority, label) ->
                        val isSelected = uiState.priorityFilter == priority
                        Surface(
                            color = if (isSelected) JarkBlueContainer else JarkSurface,
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier
                                .padding(end = 6.dp)
                                .border(1.dp, if (isSelected) JarkBlue else JarkBorder, RoundedCornerShape(4.dp))
                                .clickable { onPriorityFilterChange(priority) }
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) JarkBlue else JarkTextSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Task Items List
        if (uiState.visibleTasks.isNotEmpty()) {
            items(uiState.visibleTasks, key = { it.id }) { task ->
                TaskRow(
                    task = task,
                    onToggle = onToggleTask,
                    onEdit = onEditTask,
                    onDelete = { taskToDelete = it }
                )
            }
        } else {
            // Empty State
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF18232D)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FormatListBulleted,
                                contentDescription = null,
                                tint = JarkBlue,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Nothing here yet",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = JarkTextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (uiState.searchQuery.isNotBlank()) "No tasks match your current search and filters." else "Create a task to populate this view.",
                            style = MaterialTheme.typography.bodySmall,
                            color = JarkTextMuted
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        TextButton(
                            onClick = onCreateTask,
                            modifier = Modifier.testTag("empty_state_create_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = JarkBlue, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Create a task", color = JarkBlue, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    if (taskToDelete != null) {
        AlertDialog(
            onDismissRequest = { taskToDelete = null },
            title = {
                Text("Delete Task", style = MaterialTheme.typography.titleMedium, color = JarkTextPrimary)
            },
            text = {
                Text(
                    text = "Are you sure you want to delete “${taskToDelete!!.title}”? This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = JarkTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteTask(taskToDelete!!)
                        taskToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JarkRed, contentColor = JarkBackground),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { taskToDelete = null }) {
                    Text("Cancel", color = JarkTextSecondary)
                }
            },
            containerColor = JarkSurface,
            shape = RoundedCornerShape(12.dp)
        )
    }
}
