package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.NavTab
import com.example.ui.TaskViewModel
import com.example.ui.components.AddTaskBottomSheet
import com.example.ui.components.CategoryTagPickerModal
import com.example.ui.components.FloatingBottomNavBar
import com.example.ui.components.TaskEditBottomSheet
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.SearchFilterScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.screens.TasksScreen
import com.example.ui.theme.CreamBg
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContent()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(viewModel: TaskViewModel = viewModel()) {
    // Request notification permission on Android 13+
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val allTasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val allCategories by viewModel.allCategories.collectAsStateWithLifecycle()
    val filteredTasks by viewModel.filteredTasks.collectAsStateWithLifecycle()
    val filterState by viewModel.filterState.collectAsStateWithLifecycle()
    val timeFilter by viewModel.timeFilter.collectAsStateWithLifecycle()
    val isSearchFilterVisible by viewModel.isSearchFilterVisible.collectAsStateWithLifecycle()
    val selectedCalendarDate by viewModel.selectedCalendarDate.collectAsStateWithLifecycle()
    val editingTask by viewModel.editingTask.collectAsStateWithLifecycle()
    val isCategoryPickerVisible by viewModel.isCategoryPickerVisible.collectAsStateWithLifecycle()
    val editingCategory by viewModel.editingCategory.collectAsStateWithLifecycle()

    var showAddTaskSheet by remember { mutableStateOf(false) }
    val addSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val editSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Handle back button for sub-screen / search
    BackHandler(enabled = isSearchFilterVisible || currentTab != NavTab.TASKS || editingTask != null || isCategoryPickerVisible) {
        when {
            isCategoryPickerVisible -> viewModel.closeCategoryPicker()
            editingTask != null -> viewModel.stopEditingTask()
            isSearchFilterVisible -> viewModel.toggleSearchFilter(false)
            else -> viewModel.setTab(NavTab.TASKS)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBg)
            .statusBarsPadding()
    ) {
        if (isSearchFilterVisible) {
            SearchFilterScreen(
                filterState = filterState,
                categories = allCategories,
                filteredTasks = filteredTasks,
                onQueryChange = { viewModel.setFilterQuery(it) },
                onCategorySelect = { viewModel.setFilterCategory(it) },
                onTagSelect = { viewModel.setFilterTag(it) },
                onPrioritySelect = { viewModel.setFilterPriority(it) },
                onClearFilters = { viewModel.clearAllFilters() },
                onClose = { viewModel.toggleSearchFilter(false) },
                onToggleTaskComplete = { viewModel.toggleTaskCompletion(it) },
                onToggleSubTask = { task, subId -> viewModel.toggleSubTask(task, subId) },
                onTaskClick = { viewModel.startEditingTask(it) }
            )
        } else {
            when (currentTab) {
                NavTab.TASKS -> {
                    TasksScreen(
                        tasks = filteredTasks,
                        allTasksRaw = allTasks,
                        categories = allCategories,
                        timeFilter = timeFilter,
                        selectedCategoryId = filterState.categoryId,
                        selectedTag = filterState.tag,
                        onSelectTimeFilter = { viewModel.setTimeFilter(it) },
                        onSelectCategory = { viewModel.setFilterCategory(it) },
                        onSelectTag = { viewModel.setFilterTag(it) },
                        onOpenSearch = { viewModel.toggleSearchFilter(true) },
                        onOpenSettings = { viewModel.setTab(NavTab.SETTINGS) },
                        onOpenAddTask = { showAddTaskSheet = true },
                        onAddList = { viewModel.openCategoryPicker() },
                        onAddTag = { viewModel.openCategoryPicker() },
                        onToggleTaskComplete = { viewModel.toggleTaskCompletion(it) },
                        onToggleSubTask = { task, subId -> viewModel.toggleSubTask(task, subId) },
                        onTaskClick = { viewModel.startEditingTask(it) }
                    )
                }
                NavTab.CALENDAR -> {
                    CalendarScreen(
                        tasks = allTasks,
                        categories = allCategories,
                        selectedDateMs = selectedCalendarDate,
                        onSelectDate = { viewModel.setSelectedCalendarDate(it) },
                        onOpenAddTask = { showAddTaskSheet = true },
                        onToggleTaskComplete = { viewModel.toggleTaskCompletion(it) },
                        onToggleSubTask = { task, subId -> viewModel.toggleSubTask(task, subId) },
                        onTaskClick = { viewModel.startEditingTask(it) }
                    )
                }
                NavTab.STATS -> {
                    StatsScreen(
                        tasks = allTasks,
                        categories = allCategories,
                        onOpenSearch = { viewModel.toggleSearchFilter(true) },
                        onOpenAddTask = { showAddTaskSheet = true }
                    )
                }
                NavTab.SETTINGS -> {
                    SettingsScreen(
                        tasks = allTasks,
                        categories = allCategories,
                        onTestReminder = { viewModel.testPopupReminder() },
                        onSaveCategory = { name, icon, hex -> viewModel.saveCategory(name, icon, hex) },
                        onDeleteCategory = { viewModel.deleteCategory(it) },
                        onExportCsv = { ctx -> viewModel.exportTasksCsv(ctx) },
                        onExportBackupJson = { ctx -> viewModel.exportBackupJson(ctx) },
                        onImportBackupJson = { ctx, uri, onRes -> viewModel.importBackupJson(ctx, uri, onRes) }
                    )
                }
            }

            // Floating Pill Navigation Bar
            FloatingBottomNavBar(
                currentTab = currentTab,
                onTabSelected = { viewModel.setTab(it) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp)
            )
        }

        // Add Task Bottom Sheet
        if (showAddTaskSheet) {
            AddTaskBottomSheet(
                sheetState = addSheetState,
                categories = allCategories,
                onDismiss = { showAddTaskSheet = false },
                onSaveTask = { title, remark, catId, priority, tags, dueDate, reminder, subtasks ->
                    viewModel.saveTask(
                        title = title,
                        remark = remark,
                        categoryId = catId,
                        priority = priority,
                        tags = tags,
                        dueTimestamp = dueDate,
                        hasReminder = reminder,
                        subtasks = subtasks
                    )
                }
            )
        }

        // Edit Task Bottom Sheet (Screenshot 1)
        editingTask?.let { taskToEdit ->
            TaskEditBottomSheet(
                task = taskToEdit,
                categories = allCategories,
                sheetState = editSheetState,
                onDismiss = { viewModel.stopEditingTask() },
                onSaveTask = { updated -> viewModel.updateTask(updated) },
                onDeleteTask = { taskToDelete -> viewModel.deleteTask(taskToDelete) },
                onOpenCategoryPicker = { viewModel.openCategoryPicker() }
            )
        }

        // Category & Tag Icon Picker Modal (Screenshot 2)
        if (isCategoryPickerVisible) {
            CategoryTagPickerModal(
                initialName = editingCategory?.name ?: "Default",
                initialIcon = editingCategory?.iconType ?: "🍔",
                initialColorHex = editingCategory?.colorHex ?: "#5CD8D3",
                onDismiss = { viewModel.closeCategoryPicker() },
                onConfirm = { name, icon, hex ->
                    viewModel.saveCategory(name, icon, hex)
                }
            )
        }
    }
}
