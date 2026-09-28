package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DEFAULT_CATEGORIES
import com.example.data.model.ViewMode
import com.example.ui.theme.JarkBackground
import com.example.ui.theme.JarkBlue
import com.example.ui.theme.JarkBlueContainer
import com.example.ui.theme.JarkBorder
import com.example.ui.theme.JarkSurface
import com.example.ui.theme.JarkTextMuted
import com.example.ui.theme.JarkTextPrimary
import com.example.ui.theme.JarkTextSecondary
import com.example.ui.viewmodel.TaskUiState

@Composable
fun DrawerContent(
    uiState: TaskUiState,
    onSelectView: (ViewMode) -> Unit,
    onSelectCategory: (String) -> Unit,
    onAddTask: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF0C1017),
        modifier = modifier
            .fillMaxHeight()
            .width(280.dp)
            .border(1.dp, JarkBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 24.dp, horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Brand header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0E1A29))
                        .border(1.dp, Color(0xFF6BB6FF), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "J",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        ),
                        color = Color(0xFFE8F4FF)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "J.A.R.K.",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        ),
                        color = JarkTextPrimary
                    )
                    Text(
                        text = "TO-DO SYSTEM",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 8.sp,
                            letterSpacing = 1.6.sp
                        ),
                        color = JarkTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Main Nav Section
            Text(
                text = "WORKSPACE",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.2.sp
                ),
                color = JarkTextMuted,
                modifier = Modifier.padding(horizontal = 10.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            DrawerNavItem(
                icon = Icons.Default.Dashboard,
                label = "Dashboard",
                count = uiState.allTasks.size,
                isSelected = uiState.currentView == ViewMode.DASHBOARD,
                onClick = { onSelectView(ViewMode.DASHBOARD) },
                testTag = "nav_dashboard"
            )

            DrawerNavItem(
                icon = Icons.Default.FormatListBulleted,
                label = "All tasks",
                count = uiState.openTasksCount,
                isSelected = uiState.currentView == ViewMode.ALL,
                onClick = { onSelectView(ViewMode.ALL) },
                testTag = "nav_all"
            )

            DrawerNavItem(
                icon = Icons.Default.CalendarToday,
                label = "Today",
                count = uiState.todayTasks.size,
                isSelected = uiState.currentView == ViewMode.TODAY,
                onClick = { onSelectView(ViewMode.TODAY) },
                testTag = "nav_today"
            )

            DrawerNavItem(
                icon = Icons.Default.Schedule,
                label = "Upcoming",
                count = uiState.upcomingTasks.size,
                isSelected = uiState.currentView == ViewMode.UPCOMING,
                onClick = { onSelectView(ViewMode.UPCOMING) },
                testTag = "nav_upcoming"
            )

            DrawerNavItem(
                icon = Icons.Default.Notifications,
                label = "Overdue",
                count = uiState.overdueTasks.size,
                isSelected = uiState.currentView == ViewMode.OVERDUE,
                onClick = { onSelectView(ViewMode.OVERDUE) },
                testTag = "nav_overdue"
            )

            DrawerNavItem(
                icon = Icons.Default.Archive,
                label = "Completed",
                count = uiState.completedCount,
                isSelected = uiState.currentView == ViewMode.COMPLETED,
                onClick = { onSelectView(ViewMode.COMPLETED) },
                testTag = "nav_completed"
            )

            DrawerNavItem(
                icon = Icons.Default.Bolt,
                label = "High priority",
                count = uiState.priorityTasks.size,
                isSelected = uiState.currentView == ViewMode.PRIORITY,
                onClick = { onSelectView(ViewMode.PRIORITY) },
                testTag = "nav_priority"
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Categories Section
            Text(
                text = "CATEGORIES",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.2.sp
                ),
                color = JarkTextMuted,
                modifier = Modifier.padding(horizontal = 10.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            DEFAULT_CATEGORIES.take(5).forEach { category ->
                val count = uiState.allTasks.count { it.category.equals(category, ignoreCase = true) && it.status != com.example.data.model.TaskStatus.COMPLETED }
                val isSelected = uiState.categoryFilter.equals(category, ignoreCase = true) && uiState.currentView == ViewMode.ALL

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) JarkBlueContainer else Color.Transparent)
                        .clickable { onSelectCategory(category) }
                        .padding(horizontal = 10.dp, vertical = 9.dp)
                ) {
                    CategoryDot(category = category)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = category,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isSelected) JarkTextPrimary else JarkTextSecondary,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "$count",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace
                        ),
                        color = JarkTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = JarkBorder)
            Spacer(modifier = Modifier.height(16.dp))

            // Bottom CTA Buttons
            Button(
                onClick = onAddTask,
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = JarkBlue,
                    contentColor = JarkBackground
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("drawer_add_task_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Add task",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("drawer_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = JarkTextMuted,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Settings",
                    color = JarkTextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun DrawerNavItem(
    icon: ImageVector,
    label: String,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) JarkBlueContainer else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 9.dp)
            .testTag(testTag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) JarkBlue else JarkTextMuted,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            ),
            color = if (isSelected) JarkTextPrimary else JarkTextSecondary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "$count",
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            ),
            color = if (isSelected) JarkBlue else JarkTextMuted
        )
    }
}
