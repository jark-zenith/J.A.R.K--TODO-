package com.example.ui.components

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.window.Dialog
import com.example.data.model.DEFAULT_CATEGORIES
import com.example.data.model.Task
import com.example.data.model.TaskDraft
import com.example.data.model.TaskPriority
import com.example.ui.theme.JarkAmber
import com.example.ui.theme.JarkBackground
import com.example.ui.theme.JarkBlue
import com.example.ui.theme.JarkBorder
import com.example.ui.theme.JarkLowSlate
import com.example.ui.theme.JarkRed
import com.example.ui.theme.JarkSurface
import com.example.ui.theme.JarkSurfaceVariant
import com.example.ui.theme.JarkTextMuted
import com.example.ui.theme.JarkTextPrimary
import com.example.ui.theme.JarkTextSecondary
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TaskModalDialog(
    task: Task?,
    onDismiss: () -> Unit,
    onSubmit: (TaskDraft) -> Unit
) {
    var title by remember(task) { mutableStateOf(task?.title ?: "") }
    var description by remember(task) { mutableStateOf(task?.description ?: "") }
    var priority by remember(task) { mutableStateOf(task?.priority ?: TaskPriority.MEDIUM) }
    var category by remember(task) { mutableStateOf(task?.category ?: "Personal") }
    var dueDate by remember(task) { mutableStateOf(task?.dueDate ?: "") }
    var tagsString by remember(task) { mutableStateOf(task?.tags?.joinToString(", ") ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var categoryExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val isEditMode = task != null

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = JarkSurface,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF334352), RoundedCornerShape(12.dp))
                .testTag("task_modal_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "TASK REGISTRY",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.2.sp
                            ),
                            color = JarkTextMuted
                        )
                        Text(
                            text = if (isEditMode) "Edit task" else "Create a task",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = JarkTextPrimary
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = JarkTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title field
                Text(
                    text = "Title *",
                    style = MaterialTheme.typography.labelMedium,
                    color = JarkTextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (errorMessage != null) errorMessage = null
                    },
                    placeholder = { Text("What needs your attention?", color = JarkTextMuted) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_title_input"),
                    shape = RoundedCornerShape(6.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = JarkBackground,
                        unfocusedContainerColor = JarkBackground,
                        focusedBorderColor = JarkBlue,
                        unfocusedBorderColor = JarkBorder,
                        focusedTextColor = JarkTextPrimary,
                        unfocusedTextColor = JarkTextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Description field
                Text(
                    text = "Description",
                    style = MaterialTheme.typography.labelMedium,
                    color = JarkTextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text("Add useful context or steps...", color = JarkTextMuted) },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_desc_input"),
                    shape = RoundedCornerShape(6.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = JarkBackground,
                        unfocusedContainerColor = JarkBackground,
                        focusedBorderColor = JarkBlue,
                        unfocusedBorderColor = JarkBorder,
                        focusedTextColor = JarkTextPrimary,
                        unfocusedTextColor = JarkTextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Priority selector chips
                Text(
                    text = "Priority",
                    style = MaterialTheme.typography.labelMedium,
                    color = JarkTextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        TaskPriority.URGENT to JarkRed,
                        TaskPriority.HIGH to JarkAmber,
                        TaskPriority.MEDIUM to JarkBlue,
                        TaskPriority.LOW to JarkLowSlate
                    ).forEach { (p, col) ->
                        val isSelected = priority == p
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) col.copy(alpha = 0.2f) else JarkBackground)
                                .border(1.dp, if (isSelected) col else JarkBorder, RoundedCornerShape(6.dp))
                                .clickable { priority = p }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = p.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) col else JarkTextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Category dropdown
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium,
                    color = JarkTextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(6.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = JarkBackground,
                            unfocusedContainerColor = JarkBackground,
                            focusedBorderColor = JarkBlue,
                            unfocusedBorderColor = JarkBorder,
                            focusedTextColor = JarkTextPrimary,
                            unfocusedTextColor = JarkTextPrimary
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false },
                        modifier = Modifier.background(JarkSurfaceVariant)
                    ) {
                        DEFAULT_CATEGORIES.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat, color = JarkTextPrimary) },
                                onClick = {
                                    category = cat
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Due date selector
                Text(
                    text = "Deadline",
                    style = MaterialTheme.typography.labelMedium,
                    color = JarkTextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = dueDate.ifBlank { "No deadline" },
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showDatePicker = true },
                        shape = RoundedCornerShape(6.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = JarkBackground,
                            unfocusedContainerColor = JarkBackground,
                            focusedBorderColor = JarkBlue,
                            unfocusedBorderColor = JarkBorder,
                            focusedTextColor = if (dueDate.isNotBlank()) JarkTextPrimary else JarkTextMuted,
                            unfocusedTextColor = if (dueDate.isNotBlank()) JarkTextPrimary else JarkTextMuted
                        )
                    )

                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = JarkBlue)
                    ) {
                        Text("Pick")
                    }

                    if (dueDate.isNotBlank()) {
                        TextButton(
                            onClick = { dueDate = "" }
                        ) {
                            Text("Clear", color = JarkRed)
                        }
                    }
                }

                // Quick date presets
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                    val tomorrow = LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)

                    Surface(
                        color = Color(0xFF16212D),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .clickable { dueDate = today }
                            .border(1.dp, Color(0xFF233547), RoundedCornerShape(4.dp))
                    ) {
                        Text(
                            "Today",
                            style = MaterialTheme.typography.labelSmall,
                            color = JarkBlue,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        color = Color(0xFF16212D),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .clickable { dueDate = tomorrow }
                            .border(1.dp, Color(0xFF233547), RoundedCornerShape(4.dp))
                    ) {
                        Text(
                            "Tomorrow",
                            style = MaterialTheme.typography.labelSmall,
                            color = JarkBlue,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tags field
                Text(
                    text = "Tags (comma separated)",
                    style = MaterialTheme.typography.labelMedium,
                    color = JarkTextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = tagsString,
                    onValueChange = { tagsString = it },
                    placeholder = { Text("planning, focus, sprint-1", color = JarkTextMuted) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = JarkBackground,
                        unfocusedContainerColor = JarkBackground,
                        focusedBorderColor = JarkBlue,
                        unfocusedBorderColor = JarkBorder,
                        focusedTextColor = JarkTextPrimary,
                        unfocusedTextColor = JarkTextPrimary
                    )
                )

                // Error message
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = JarkRed
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action buttons
                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("cancel_task_modal")
                    ) {
                        Text("Cancel", color = JarkTextSecondary)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                errorMessage = "A task title is required."
                                return@Button
                            }
                            val parsedTags = tagsString
                                .split(",")
                                .map { it.trim() }
                                .filter { it.isNotBlank() }

                            onSubmit(
                                TaskDraft(
                                    title = title.trim(),
                                    description = description.trim(),
                                    priority = priority,
                                    category = category.trim().ifBlank { "Personal" },
                                    dueDate = dueDate,
                                    tags = parsedTags
                                )
                            )
                        },
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = JarkBlue,
                            contentColor = JarkBackground
                        ),
                        modifier = Modifier.testTag("submit_task_modal")
                    ) {
                        Text(
                            text = if (isEditMode) "Save changes" else "Create task",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedLocalDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.of("UTC"))
                                .toLocalDate()
                            dueDate = selectedLocalDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK", color = JarkBlue)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = JarkTextSecondary)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
