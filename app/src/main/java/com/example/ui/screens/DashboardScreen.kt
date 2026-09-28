package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Task
import com.example.data.model.TaskPriority
import com.example.data.model.TaskStatus
import com.example.data.model.ViewMode
import com.example.ui.components.CommandPanel
import com.example.ui.components.MomentumRingPanel
import com.example.ui.components.StatCard
import com.example.ui.components.TaskRow
import com.example.ui.theme.JarkAmber
import com.example.ui.theme.JarkAmberContainer
import com.example.ui.theme.JarkBackground
import com.example.ui.theme.JarkBlue
import com.example.ui.theme.JarkBlueContainer
import com.example.ui.theme.JarkBorder
import com.example.ui.theme.JarkCardGradientEnd
import com.example.ui.theme.JarkCardGradientStart
import com.example.ui.theme.JarkGreen
import com.example.ui.theme.JarkGreenContainer
import com.example.ui.theme.JarkRed
import com.example.ui.theme.JarkRedContainer
import com.example.ui.theme.JarkSurface
import com.example.ui.theme.JarkTextMuted
import com.example.ui.theme.JarkTextPrimary
import com.example.ui.theme.JarkTextSecondary
import com.example.ui.viewmodel.CommandStatus
import com.example.ui.viewmodel.TaskUiState
import java.time.LocalTime

@Composable
fun DashboardScreen(
    uiState: TaskUiState,
    onNavigate: (ViewMode) -> Unit,
    onToggleTask: (Task) -> Unit,
    onEditTask: (Task) -> Unit,
    onCreateTask: () -> Unit,
    onCommandChange: (String) -> Unit,
    onCommandSubmit: () -> Unit,
    onVoiceClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val greeting = when (LocalTime.now().hour) {
        in 5..11 -> "Good morning, Jordan"
        in 12..17 -> "Good afternoon, Jordan"
        else -> "Good evening, Jordan"
    }

    val focusTasks = (uiState.todayTasks + uiState.overdueTasks)
        .distinctBy { it.id }
        .sortedBy {
            when (it.priority) {
                TaskPriority.URGENT -> 0
                TaskPriority.HIGH -> 1
                TaskPriority.MEDIUM -> 2
                TaskPriority.LOW -> 3
            }
        }

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Page Heading
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(JarkGreen)
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    Text(
                        text = "PERSONAL OPERATING SYSTEM",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.3.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = JarkTextMuted
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = greeting,
                                style = MaterialTheme.typography.headlineLarge,
                                color = JarkTextPrimary
                            )
                            Text(
                                text = ".",
                                style = MaterialTheme.typography.headlineLarge,
                                color = JarkBlue
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Here’s the signal for your day. Keep the important work moving.",
                            style = MaterialTheme.typography.bodySmall,
                            color = JarkTextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onCreateTask,
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = JarkBlue,
                            contentColor = JarkBackground
                        ),
                        modifier = Modifier.testTag("dashboard_new_task_btn")
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

        // Stats Grid: 2 rows of 2 cards
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatCard(
                        label = "Open tasks",
                        value = "${uiState.openTasksCount}",
                        detail = "Across workspace",
                        icon = Icons.Default.FormatListBulleted,
                        iconColor = JarkBlue,
                        iconBgColor = JarkBlueContainer,
                        modifier = Modifier.weight(1f)
                    )

                    StatCard(
                        label = "Completed",
                        value = "${uiState.completedCount}",
                        detail = "${uiState.completionRate}% completion rate",
                        icon = Icons.Default.CheckCircle,
                        iconColor = JarkGreen,
                        iconBgColor = JarkGreenContainer,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatCard(
                        label = "Due today",
                        value = "${uiState.todayTasks.size}",
                        detail = if (uiState.overdueTasks.isNotEmpty()) "${uiState.overdueTasks.size} need attention" else "You are on track",
                        icon = Icons.Default.CalendarToday,
                        iconColor = JarkAmber,
                        iconBgColor = JarkAmberContainer,
                        modifier = Modifier.weight(1f)
                    )

                    StatCard(
                        label = "Focus score",
                        value = "${uiState.focusScore}%",
                        detail = "Recent momentum",
                        icon = Icons.Default.AutoAwesome,
                        iconColor = JarkRed,
                        iconBgColor = JarkRedContainer,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Priority Queue Panel ("What should I work on?")
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(JarkCardGradientStart, JarkCardGradientEnd)
                        )
                    )
                    .border(1.dp, JarkBorder, RoundedCornerShape(8.dp))
                    .padding(18.dp)
                    .testTag("priority_queue_panel")
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = "PRIORITY QUEUE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.1.sp
                                ),
                                color = JarkTextMuted
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "What should I work on?",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = JarkTextPrimary
                            )
                        }

                        TextButton(
                            onClick = { onNavigate(ViewMode.PRIORITY) },
                            modifier = Modifier.testTag("view_all_priority_btn")
                        ) {
                            Text("View all", color = JarkBlue, style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = JarkBlue,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (focusTasks.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            focusTasks.take(4).forEach { task ->
                                TaskRow(
                                    task = task,
                                    onToggle = onToggleTask,
                                    onEdit = onEditTask
                                )
                            }
                        }
                    } else {
                        // Empty queue state
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF18232D)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = JarkBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Your priority queue is clear.",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = JarkTextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "All urgent and today items completed.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = JarkTextMuted
                                )
                            }
                        }
                    }
                }
            }
        }

        // Momentum / System Health Panel
        item {
            MomentumRingPanel(
                completionRate = uiState.completionRate,
                dueTodayCount = uiState.todayTasks.size,
                inProgressCount = uiState.allTasks.count { it.status == TaskStatus.IN_PROGRESS }
            )
        }

        // J.A.R.K. Command Layer
        item {
            CommandPanel(
                command = uiState.commandText,
                commandStatus = uiState.commandStatus,
                commandReply = uiState.commandReply,
                onCommandChange = onCommandChange,
                onSubmit = onCommandSubmit,
                onVoiceClick = onVoiceClick
            )
        }
    }
}
