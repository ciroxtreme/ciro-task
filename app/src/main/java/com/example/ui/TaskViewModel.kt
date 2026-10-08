package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.CategoryEntity
import com.example.data.model.SubTask
import com.example.data.model.SubTaskConverter
import com.example.data.model.TaskEntity
import com.example.data.repository.TaskRepository
import com.example.reminder.ReminderManager
import com.example.reminder.TaskReminderReceiver
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

import com.example.data.model.TaskTimeFilter
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class NavTab {
    TASKS,
    CALENDAR,
    STATS,
    SETTINGS
}

data class FilterState(
    val query: String = "",
    val categoryId: String? = null,
    val tag: String? = null,
    val priority: String? = null,
    val startDate: Long? = null,
    val endDate: Long? = null
)

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = TaskRepository(database.taskDao(), database.categoryDao())

    val allTasks: StateFlow<List<TaskEntity>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCategories: StateFlow<List<CategoryEntity>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            allTasks.collect { tasks ->
                val now = System.currentTimeMillis()
                val categories = allCategories.value
                val categoryMap = categories.associate { it.id to it.name }
                tasks.forEach { task ->
                    if (task.hasReminder && !task.isCompleted && task.dueTimestamp > now) {
                        val catName = categoryMap[task.categoryId] ?: "Tugas"
                        ReminderManager.scheduleTaskReminder(application, task, catName)
                    }
                }
            }
        }
    }

    private val _currentTab = MutableStateFlow(NavTab.TASKS)
    val currentTab: StateFlow<NavTab> = _currentTab.asStateFlow()

    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    private val _isSearchFilterVisible = MutableStateFlow(false)
    val isSearchFilterVisible: StateFlow<Boolean> = _isSearchFilterVisible.asStateFlow()

    private val _selectedCalendarDate = MutableStateFlow(System.currentTimeMillis())
    val selectedCalendarDate: StateFlow<Long> = _selectedCalendarDate.asStateFlow()

    // Editing task state (opens TaskEditBottomSheet)
    private val _editingTask = MutableStateFlow<TaskEntity?>(null)
    val editingTask: StateFlow<TaskEntity?> = _editingTask.asStateFlow()

    // Category / Tag picker modal state
    private val _isCategoryPickerVisible = MutableStateFlow(false)
    val isCategoryPickerVisible: StateFlow<Boolean> = _isCategoryPickerVisible.asStateFlow()

    private val _editingCategory = MutableStateFlow<CategoryEntity?>(null)
    val editingCategory: StateFlow<CategoryEntity?> = _editingCategory.asStateFlow()

    // Set of tasks already alerted during this session to prevent repeated spam
    private val alertedTaskIds = mutableSetOf<Long>()

    // Time filter for side drawer (All, Today, Tomorrow, Next 7 days, Completed)
    private val _timeFilter = MutableStateFlow(TaskTimeFilter.ALL)
    val timeFilter: StateFlow<TaskTimeFilter> = _timeFilter.asStateFlow()

    // Sort option for task list (Date, Priority, List, None)
    private val _sortOption = MutableStateFlow(com.example.ui.components.SortOption.NONE)
    val sortOption: StateFlow<com.example.ui.components.SortOption> = _sortOption.asStateFlow()

    // Filtered tasks flow with sorting
    val filteredTasks: StateFlow<List<TaskEntity>> = combine(allTasks, _filterState, _timeFilter, _sortOption) { tasks, filter, timeF, sortOpt ->
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val todayStart = cal.timeInMillis
        val todayEnd = todayStart + (24 * 3600 * 1000) - 1
        val tomorrowStart = todayEnd + 1
        val tomorrowEnd = tomorrowStart + (24 * 3600 * 1000) - 1
        val next7DaysEnd = todayStart + (7L * 24 * 3600 * 1000) - 1

        val filtered = tasks.filter { task ->
            val matchesTime = when (timeF) {
                TaskTimeFilter.ALL -> true
                TaskTimeFilter.TODAY -> !task.isCompleted && task.dueTimestamp in todayStart..todayEnd
                TaskTimeFilter.TOMORROW -> !task.isCompleted && task.dueTimestamp in tomorrowStart..tomorrowEnd
                TaskTimeFilter.NEXT_7_DAYS -> !task.isCompleted && task.dueTimestamp in todayStart..next7DaysEnd
                TaskTimeFilter.COMPLETED -> task.isCompleted
            }

            val matchesQuery = if (filter.query.isBlank()) true else {
                task.title.contains(filter.query, ignoreCase = true) ||
                        task.remark.contains(filter.query, ignoreCase = true)
            }
            val matchesCategory = if (filter.categoryId == null || filter.categoryId == "all") true else {
                task.categoryId.equals(filter.categoryId, ignoreCase = true)
            }
            val matchesTag = if (filter.tag.isNullOrBlank()) true else {
                task.tags.split(",").any { it.trim().equals(filter.tag, ignoreCase = true) }
            }
            val matchesPriority = if (filter.priority.isNullOrBlank()) true else {
                task.priority.equals(filter.priority, ignoreCase = true)
            }
            val matchesDate = if (filter.startDate != null && filter.endDate != null) {
                task.dueTimestamp in filter.startDate..filter.endDate
            } else true

            matchesTime && matchesQuery && matchesCategory && matchesTag && matchesPriority && matchesDate
        }

        // Apply Sorting
        when (sortOpt) {
            com.example.ui.components.SortOption.DATE -> filtered.sortedBy { it.dueTimestamp }
            com.example.ui.components.SortOption.PRIORITY -> filtered.sortedByDescending {
                when (it.priority.lowercase()) {
                    "high" -> 3
                    "medium" -> 2
                    "low" -> 1
                    else -> 0
                }
            }
            com.example.ui.components.SortOption.LIST -> filtered.sortedBy { it.categoryId }
            com.example.ui.components.SortOption.NONE -> filtered
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSortOption(option: com.example.ui.components.SortOption) {
        _sortOption.value = option
    }

    fun setTab(tab: NavTab) {
        _currentTab.value = tab
    }

    fun toggleSearchFilter(open: Boolean? = null) {
        _isSearchFilterVisible.value = open ?: !_isSearchFilterVisible.value
    }

    fun setFilterQuery(query: String) {
        _filterState.value = _filterState.value.copy(query = query)
    }

    fun setFilterCategory(categoryId: String?) {
        _filterState.value = _filterState.value.copy(
            categoryId = if (_filterState.value.categoryId == categoryId) null else categoryId
        )
    }

    fun setFilterTag(tag: String?) {
        _filterState.value = _filterState.value.copy(
            tag = if (_filterState.value.tag == tag) null else tag
        )
    }

    fun setFilterPriority(priority: String?) {
        _filterState.value = _filterState.value.copy(
            priority = if (_filterState.value.priority == priority) null else priority
        )
    }

    fun clearAllFilters() {
        _filterState.value = FilterState()
    }

    fun setSelectedCalendarDate(dateMs: Long) {
        _selectedCalendarDate.value = dateMs
    }

    fun toggleTaskCompletion(task: TaskEntity) {
        viewModelScope.launch {
            val updated = task.copy(
                isCompleted = !task.isCompleted,
                completedAt = if (!task.isCompleted) System.currentTimeMillis() else null
            )
            repository.updateTask(updated)
            if (updated.isCompleted) {
                alertedTaskIds.add(task.id)
                ReminderManager.cancelTaskReminder(getApplication(), task.id)
            } else {
                alertedTaskIds.remove(task.id)
                val catName = allCategories.value.find { it.id == task.categoryId }?.name ?: "Task"
                ReminderManager.scheduleTaskReminder(getApplication(), updated, catName)
            }
        }
    }

    fun toggleSubTask(task: TaskEntity, subTaskId: String) {
        viewModelScope.launch {
            val subtasks = SubTaskConverter.fromJson(task.subtasksJson).map {
                if (it.id == subTaskId) it.copy(isCompleted = !it.isCompleted) else it
            }
            val updated = task.copy(subtasksJson = SubTaskConverter.toJson(subtasks))
            repository.updateTask(updated)
        }
    }

    fun saveTask(
        title: String,
        remark: String,
        categoryId: String,
        priority: String,
        tags: String,
        dueTimestamp: Long,
        hasReminder: Boolean,
        subtasks: List<SubTask>,
        existingId: Long? = null
    ) {
        viewModelScope.launch {
            val task = TaskEntity(
                id = existingId ?: 0L,
                title = title,
                remark = remark,
                categoryId = categoryId,
                priority = priority,
                tags = tags,
                dueTimestamp = dueTimestamp,
                hasReminder = hasReminder,
                subtasksJson = SubTaskConverter.toJson(subtasks),
                isCompleted = false
            )
            val newId = if (existingId != null && existingId > 0) {
                repository.updateTask(task)
                existingId
            } else {
                repository.insertTask(task)
            }
            alertedTaskIds.remove(newId)
            val savedTask = task.copy(id = newId)
            val catName = allCategories.value.find { it.id == categoryId }?.name ?: "Task"
            if (hasReminder) {
                ReminderManager.scheduleTaskReminder(getApplication(), savedTask, catName)
            }
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            alertedTaskIds.remove(task.id)
            ReminderManager.cancelTaskReminder(getApplication(), task.id)
            repository.deleteTask(task)
        }
    }

    fun startEditingTask(task: TaskEntity) {
        _editingTask.value = task
    }

    fun stopEditingTask() {
        _editingTask.value = null
    }

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task)
            _editingTask.value = null
            val catName = allCategories.value.find { it.id == task.categoryId }?.name ?: "Task"
            if (task.hasReminder && !task.isCompleted) {
                ReminderManager.scheduleTaskReminder(getApplication(), task, catName)
            } else {
                ReminderManager.cancelTaskReminder(getApplication(), task.id)
            }
        }
    }

    fun openCategoryPicker(category: CategoryEntity? = null) {
        _editingCategory.value = category
        _isCategoryPickerVisible.value = true
    }

    fun closeCategoryPicker() {
        _isCategoryPickerVisible.value = false
        _editingCategory.value = null
    }

    fun saveCategory(name: String, iconType: String, colorHex: String) {
        viewModelScope.launch {
            val existing = _editingCategory.value
            val id = existing?.id ?: name.lowercase().replace(" ", "_")
            val order = existing?.orderIndex ?: allCategories.value.size
            repository.insertCategory(
                CategoryEntity(
                    id = id,
                    name = name,
                    iconType = iconType,
                    colorHex = colorHex,
                    orderIndex = order
                )
            )
            closeCategoryPicker()
        }
    }

    fun setTimeFilter(filter: TaskTimeFilter) {
        _timeFilter.value = filter
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }

    fun exportTasksCsv(context: Context): File? {
        return com.example.util.BackupManager.exportTasksCsv(context, allTasks.value, allCategories.value)
    }

    fun exportBackupJson(context: Context): File? {
        return com.example.util.BackupManager.exportBackupJson(context, allTasks.value, allCategories.value)
    }

    fun importBackupJson(context: Context, uri: android.net.Uri, onResult: (Int) -> Unit) {
        viewModelScope.launch {
            val count = com.example.util.BackupManager.importBackupJson(context, uri, repository)
            onResult(count)
        }
    }

    fun testPopupReminder() {
        ReminderManager.triggerImmediateTestReminder(
            context = getApplication(),
            title = "Meeting with friends",
            category = "Family"
        )
    }
}
