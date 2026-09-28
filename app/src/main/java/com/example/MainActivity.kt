package com.example

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ai.JarkEngine
import com.example.data.model.ViewMode
import com.example.ui.components.DrawerContent
import com.example.ui.components.SettingsDialog
import com.example.ui.components.TaskModalDialog
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.TaskViewScreen
import com.example.ui.theme.JarkBackground
import com.example.ui.theme.JarkBlue
import com.example.ui.theme.JarkBlueContainer
import com.example.ui.theme.JarkBorder
import com.example.ui.theme.JarkRed
import com.example.ui.theme.JarkSurface
import com.example.ui.theme.JarkTextMuted
import com.example.ui.theme.JarkTextPrimary
import com.example.ui.theme.JarkTextSecondary
import com.example.ui.theme.JarkTheme
import com.example.ui.viewmodel.CommandStatus
import com.example.ui.viewmodel.TaskViewModel
import com.example.ui.viewmodel.TaskViewModelFactory
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val viewModel: TaskViewModel by viewModels {
        val app = application as JarkApplication
        TaskViewModelFactory(
            repository = app.repository,
            jarkEngine = JarkEngine(app.repository)
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            JarkTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                val snackbarHostState = remember { SnackbarHostState() }

                // Speech recognition launcher
                val speechLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    viewModel.setCommandStatus(CommandStatus.IDLE)
                    if (result.resultCode == Activity.RESULT_OK) {
                        val data = result.data
                        val matches = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                        val transcript = matches?.firstOrNull()
                        if (!transcript.isNullOrBlank()) {
                            viewModel.setCommandText(transcript)
                            viewModel.submitCommand(transcript)
                        }
                    }
                }

                fun startVoiceInput() {
                    try {
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak J.A.R.K. command...")
                        }
                        viewModel.setCommandStatus(CommandStatus.LISTENING)
                        speechLauncher.launch(intent)
                    } catch (_: Exception) {
                        viewModel.setCommandStatus(CommandStatus.IDLE)
                        Toast.makeText(this, "Speech recognition is not available on this device.", Toast.LENGTH_SHORT).show()
                    }
                }

                BackHandler(enabled = drawerState.isOpen || uiState.currentView != ViewMode.DASHBOARD) {
                    if (drawerState.isOpen) {
                        scope.launch { drawerState.close() }
                    } else if (uiState.currentView != ViewMode.DASHBOARD) {
                        viewModel.setView(ViewMode.DASHBOARD)
                    }
                }

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet(
                            drawerContainerColor = Color(0xFF0C1017),
                            drawerContentColor = JarkTextPrimary
                        ) {
                            DrawerContent(
                                uiState = uiState,
                                onSelectView = { view ->
                                    viewModel.setView(view)
                                    viewModel.setCategoryFilter(null)
                                    scope.launch { drawerState.close() }
                                },
                                onSelectCategory = { category ->
                                    viewModel.setView(ViewMode.ALL)
                                    viewModel.setCategoryFilter(category)
                                    scope.launch { drawerState.close() }
                                },
                                onAddTask = {
                                    scope.launch { drawerState.close() }
                                    viewModel.openCreateModal()
                                },
                                onOpenSettings = {
                                    scope.launch { drawerState.close() }
                                    viewModel.setSettingsOpen(true)
                                }
                            )
                        }
                    }
                ) {
                    Scaffold(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(JarkBackground),
                        containerColor = JarkBackground,
                        topBar = {
                            TopAppBar(
                                title = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "J.A.R.K. / ",
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontFamily = FontFamily.Monospace,
                                                color = JarkTextMuted
                                            )
                                        )
                                        Text(
                                            text = uiState.currentView.label,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                color = JarkTextPrimary
                                            )
                                        )
                                    }
                                },
                                navigationIcon = {
                                    IconButton(
                                        onClick = { scope.launch { drawerState.open() } },
                                        modifier = Modifier.testTag("open_drawer_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Menu,
                                            contentDescription = "Open navigation menu",
                                            tint = JarkTextSecondary
                                        )
                                    }
                                },
                                actions = {
                                    IconButton(
                                        onClick = { viewModel.setSettingsOpen(true) },
                                        modifier = Modifier.testTag("topbar_settings_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Settings,
                                            contentDescription = "Settings",
                                            tint = JarkTextSecondary
                                        )
                                    }
                                    // Avatar pill
                                    Box(
                                        modifier = Modifier
                                            .padding(end = 12.dp)
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF273A4D))
                                            .border(1.dp, Color(0xFF46617B), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "JD",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            ),
                                            color = Color(0xFF9BD1FF)
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = JarkBackground,
                                    titleContentColor = JarkTextPrimary
                                )
                            )
                        },
                        bottomBar = {
                            NavigationBar(
                                containerColor = Color(0xFF0C1118),
                                modifier = Modifier
                                    .border(1.dp, JarkBorder)
                                    .windowInsetsPadding(WindowInsets.navigationBars)
                            ) {
                                NavigationBarItem(
                                    selected = uiState.currentView == ViewMode.DASHBOARD,
                                    onClick = {
                                        viewModel.setView(ViewMode.DASHBOARD)
                                        viewModel.setCategoryFilter(null)
                                    },
                                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                                    label = { Text("Dashboard", style = MaterialTheme.typography.labelSmall) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = JarkBlue,
                                        selectedTextColor = JarkBlue,
                                        indicatorColor = JarkBlueContainer,
                                        unselectedIconColor = JarkTextMuted,
                                        unselectedTextColor = JarkTextMuted
                                    ),
                                    modifier = Modifier.testTag("nav_bottom_dashboard")
                                )

                                NavigationBarItem(
                                    selected = uiState.currentView == ViewMode.ALL,
                                    onClick = {
                                        viewModel.setView(ViewMode.ALL)
                                        viewModel.setCategoryFilter(null)
                                    },
                                    icon = { Icon(Icons.Default.FormatListBulleted, contentDescription = "All tasks") },
                                    label = { Text("All", style = MaterialTheme.typography.labelSmall) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = JarkBlue,
                                        selectedTextColor = JarkBlue,
                                        indicatorColor = JarkBlueContainer,
                                        unselectedIconColor = JarkTextMuted,
                                        unselectedTextColor = JarkTextMuted
                                    ),
                                    modifier = Modifier.testTag("nav_bottom_all")
                                )

                                NavigationBarItem(
                                    selected = uiState.currentView == ViewMode.TODAY,
                                    onClick = {
                                        viewModel.setView(ViewMode.TODAY)
                                        viewModel.setCategoryFilter(null)
                                    },
                                    icon = { Icon(Icons.Default.CalendarToday, contentDescription = "Today") },
                                    label = { Text("Today", style = MaterialTheme.typography.labelSmall) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = JarkBlue,
                                        selectedTextColor = JarkBlue,
                                        indicatorColor = JarkBlueContainer,
                                        unselectedIconColor = JarkTextMuted,
                                        unselectedTextColor = JarkTextMuted
                                    ),
                                    modifier = Modifier.testTag("nav_bottom_today")
                                )

                                NavigationBarItem(
                                    selected = uiState.currentView == ViewMode.PRIORITY,
                                    onClick = {
                                        viewModel.setView(ViewMode.PRIORITY)
                                        viewModel.setCategoryFilter(null)
                                    },
                                    icon = { Icon(Icons.Default.Bolt, contentDescription = "Priority") },
                                    label = { Text("Priority", style = MaterialTheme.typography.labelSmall) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = JarkBlue,
                                        selectedTextColor = JarkBlue,
                                        indicatorColor = JarkBlueContainer,
                                        unselectedIconColor = JarkTextMuted,
                                        unselectedTextColor = JarkTextMuted
                                    ),
                                    modifier = Modifier.testTag("nav_bottom_priority")
                                )
                            }
                        },
                        floatingActionButton = {
                            FloatingActionButton(
                                onClick = { viewModel.openCreateModal() },
                                containerColor = JarkBlue,
                                contentColor = JarkBackground,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("main_fab_add_task")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Create Task"
                                )
                            }
                        },
                        snackbarHost = { SnackbarHost(snackbarHostState) }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            // Optional Notice Banner
                            if (uiState.notice != null) {
                                Surface(
                                    color = Color(0xFF442924),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, Color(0xFF77413A))
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = uiState.notice!!,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFFFFC2A7),
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(
                                            onClick = { viewModel.setNotice(null) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Dismiss notice",
                                                tint = Color(0xFFFFC2A7),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            when (uiState.currentView) {
                                ViewMode.DASHBOARD -> {
                                    DashboardScreen(
                                        uiState = uiState,
                                        onNavigate = { view -> viewModel.setView(view) },
                                        onToggleTask = { task -> viewModel.toggleTask(task) },
                                        onEditTask = { task -> viewModel.openEditModal(task) },
                                        onCreateTask = { viewModel.openCreateModal() },
                                        onCommandChange = { text -> viewModel.setCommandText(text) },
                                        onCommandSubmit = { viewModel.submitCommand() },
                                        onVoiceClick = { startVoiceInput() }
                                    )
                                }
                                else -> {
                                    TaskViewScreen(
                                        uiState = uiState,
                                        onToggleTask = { task -> viewModel.toggleTask(task) },
                                        onEditTask = { task -> viewModel.openEditModal(task) },
                                        onDeleteTask = { task -> viewModel.deleteTask(task.id) },
                                        onCreateTask = { viewModel.openCreateModal() },
                                        onSearchChange = { q -> viewModel.setSearchQuery(q) },
                                        onStatusFilterChange = { s -> viewModel.setStatusFilter(s) },
                                        onPriorityFilterChange = { p -> viewModel.setPriorityFilter(p) },
                                        onCategoryFilterChange = { c -> viewModel.setCategoryFilter(c) }
                                    )
                                }
                            }

                            // Modals
                            if (uiState.isModalOpen) {
                                TaskModalDialog(
                                    task = uiState.editingTask,
                                    onDismiss = { viewModel.closeModal() },
                                    onSubmit = { draft -> viewModel.submitTask(draft) }
                                )
                            }

                            if (uiState.isSettingsOpen) {
                                SettingsDialog(
                                    uiState = uiState,
                                    onDismiss = { viewModel.setSettingsOpen(false) },
                                    onResetData = { viewModel.resetData() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
