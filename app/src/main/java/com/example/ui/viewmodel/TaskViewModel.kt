package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ai.JarkEngine
import com.example.data.model.Task
import com.example.data.model.TaskDraft
import com.example.data.model.TaskPriority
import com.example.data.model.TaskStatus
import com.example.data.model.ViewMode
import com.example.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.min
import kotlin.math.roundToInt

enum class CommandStatus {
    IDLE,
    LOADING,
    LISTENING
}

data class TaskUiState(
    val allTasks: List<Task> = emptyList(),
    val visibleTasks: List<Task> = emptyList(),
    val todayTasks: List<Task> = emptyList(),
    val upcomingTasks: List<Task> = emptyList(),
    val overdueTasks: List<Task> = emptyList(),
    val priorityTasks: List<Task> = emptyList(),
    val completedTasks: List<Task> = emptyList(),
    val openTasksCount: Int = 0,
    val completedCount: Int = 0,
    val completionRate: Int = 0,
    val focusScore: Int = 0,
    val currentView: ViewMode = ViewMode.DASHBOARD,
    val searchQuery: String = "",
    val statusFilter: TaskStatus? = null,
    val priorityFilter: TaskPriority? = null,
    val categoryFilter: String? = null,
    val isModalOpen: Boolean = false,
    val editingTask: Task? = null,
    val isSettingsOpen: Boolean = false,
    val commandText: String = "",
    val commandStatus: CommandStatus = CommandStatus.IDLE,
    val commandReply: String = "",
    val notice: String? = null
)

class TaskViewModel(
    private val repository: TaskRepository,
    private val jarkEngine: JarkEngine
) : ViewModel() {

    private val _currentView = MutableStateFlow(ViewMode.DASHBOARD)
    private val _searchQuery = MutableStateFlow("")
    private val _statusFilter = MutableStateFlow<TaskStatus?>(null)
    private val _priorityFilter = MutableStateFlow<TaskPriority?>(null)
    private val _categoryFilter = MutableStateFlow<String?>(null)

    private val _isModalOpen = MutableStateFlow(false)
    private val _editingTask = MutableStateFlow<Task?>(null)
    private val _isSettingsOpen = MutableStateFlow(false)

    private val _commandText = MutableStateFlow("")
    private val _commandStatus = MutableStateFlow(CommandStatus.IDLE)
    private val _commandReply = MutableStateFlow("")
    private val _notice = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            repository.seedStarterTasksIfEmpty()
        }
    }

    val uiState: StateFlow<TaskUiState> = combine(
        repository.allTasks,
        _currentView,
        _searchQuery,
        _statusFilter,
        _priorityFilter,
        _categoryFilter
    ) { tasks, view, query, statusF, priorityF, catF ->
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val todayTasks = TaskRepository.getTodayTasks(tasks, today)
        val upcomingTasks = TaskRepository.getUpcomingTasks(tasks, today)
        val overdueTasks = TaskRepository.getOverdueTasks(tasks, today)
        val priorityTasks = TaskRepository.getPriorityTasks(tasks)
        val completedTasks = tasks.filter { it.status == TaskStatus.COMPLETED }
        val openTasks = tasks.filter { it.status != TaskStatus.COMPLETED }

        val baseTasks = when (view) {
            ViewMode.DASHBOARD -> tasks
            ViewMode.ALL -> tasks
            ViewMode.TODAY -> todayTasks
            ViewMode.UPCOMING -> upcomingTasks
            ViewMode.OVERVIEW -> tasks
            ViewMode.OVERDUE -> overdueTasks
            ViewMode.COMPLETED -> completedTasks
            ViewMode.PRIORITY -> priorityTasks
        }

        var filtered = TaskRepository.searchTasks(baseTasks, query)
        if (statusF != null) filtered = filtered.filter { it.status == statusF }
        if (priorityF != null) filtered = filtered.filter { it.priority == priorityF }
        if (!catF.isNullOrBlank() && catF != "all") filtered = filtered.filter { it.category.equals(catF, ignoreCase = true) }

        val rate = if (tasks.isNotEmpty()) ((completedTasks.size.toDouble() / tasks.size.toDouble()) * 100).roundToInt() else 0
        val focus = min(100, rate + 38)

        TaskUiState(
            allTasks = tasks,
            visibleTasks = filtered,
            todayTasks = todayTasks,
            upcomingTasks = upcomingTasks,
            overdueTasks = overdueTasks,
            priorityTasks = priorityTasks,
            completedTasks = completedTasks,
            openTasksCount = openTasks.size,
            completedCount = completedTasks.size,
            completionRate = rate,
            focusScore = focus,
            currentView = view,
            searchQuery = query,
            statusFilter = statusF,
            priorityFilter = priorityF,
            categoryFilter = catF,
            isModalOpen = _isModalOpen.value,
            editingTask = _editingTask.value,
            isSettingsOpen = _isSettingsOpen.value,
            commandText = _commandText.value,
            commandStatus = _commandStatus.value,
            commandReply = _commandReply.value,
            notice = _notice.value
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TaskUiState()
    )

    fun setView(view: ViewMode) {
        _currentView.value = view
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setStatusFilter(status: TaskStatus?) {
        _statusFilter.value = status
    }

    fun setPriorityFilter(priority: TaskPriority?) {
        _priorityFilter.value = priority
    }

    fun setCategoryFilter(category: String?) {
        _categoryFilter.value = if (category == "all") null else category
    }

    fun openCreateModal() {
        _editingTask.value = null
        _isModalOpen.value = true
    }

    fun openEditModal(task: Task) {
        _editingTask.value = task
        _isModalOpen.value = true
    }

    fun closeModal() {
        _isModalOpen.value = false
        _editingTask.value = null
    }

    fun setSettingsOpen(isOpen: Boolean) {
        _isSettingsOpen.value = isOpen
    }

    fun setNotice(message: String?) {
        _notice.value = message
    }

    fun setCommandText(text: String) {
        _commandText.value = text
    }

    fun setCommandStatus(status: CommandStatus) {
        _commandStatus.value = status
    }

    fun toggleTask(task: Task) {
        viewModelScope.launch {
            repository.toggleTaskStatus(task)
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
        }
    }

    fun submitTask(draft: TaskDraft) {
        viewModelScope.launch {
            val currentEditing = _editingTask.value
            if (currentEditing != null) {
                repository.updateTask(currentEditing, draft)
            } else {
                repository.createTask(draft)
            }
            closeModal()
        }
    }

    fun submitCommand(input: String = _commandText.value) {
        val text = input.trim()
        if (text.isBlank() || _commandStatus.value == CommandStatus.LOADING) return

        _commandStatus.value = CommandStatus.LOADING
        _commandReply.value = ""

        viewModelScope.launch {
            try {
                val result = jarkEngine.executeCommand(text)
                _commandReply.value = result.reply
                if (result.changed) {
                    _commandText.value = ""
                }
            } catch (e: Exception) {
                _commandReply.value = e.message ?: "J.A.R.K. could not process that instruction."
            } finally {
                _commandStatus.value = CommandStatus.IDLE
            }
        }
    }

    fun resetData() {
        viewModelScope.launch {
            repository.resetToStarters()
            _notice.value = "Workspace reset to J.A.R.K. starter dataset."
        }
    }
}

class TaskViewModelFactory(
    private val repository: TaskRepository,
    private val jarkEngine: JarkEngine
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            return TaskViewModel(repository, jarkEngine) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
