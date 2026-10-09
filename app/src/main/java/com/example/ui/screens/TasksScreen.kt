package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.CategoryEntity
import com.example.data.model.TaskEntity
import com.example.ui.components.CategoryIconBadge
import com.example.ui.components.CuteMascotAvatar
import com.example.ui.components.TaskCard
import com.example.ui.theme.CreamBg
import com.example.ui.theme.WarmPrimary
import com.example.ui.theme.WarmText
import com.example.ui.theme.WarmTextSecondary
import java.util.Calendar

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.rememberCoroutineScope
import com.example.data.model.TaskTimeFilter
import com.example.ui.components.TaskSideDrawerContent
import kotlinx.coroutines.launch

@Composable
fun TasksScreen(
    tasks: List<TaskEntity>,
    allTasksRaw: List<TaskEntity>,
    categories: List<CategoryEntity>,
    timeFilter: TaskTimeFilter,
    selectedCategoryId: String?,
    selectedTag: String?,
    selectedSort: com.example.ui.components.SortOption,
    isExactAlarmAllowed: Boolean = true,
    onRequestExactAlarmPermission: () -> Unit = {},
    onSelectSortOption: (com.example.ui.components.SortOption) -> Unit,
    onSelectTimeFilter: (TaskTimeFilter) -> Unit,
    onSelectCategory: (String?) -> Unit,
    onSelectTag: (String?) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAddTask: () -> Unit,
    onAddList: () -> Unit,
    onAddTag: () -> Unit,
    onToggleTaskComplete: (TaskEntity) -> Unit,
    onToggleSubTask: (TaskEntity, String) -> Unit,
    onTaskClick: (TaskEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    var isCompletedExpanded by remember { mutableStateOf(true) }
    var showSortPopup by remember { mutableStateOf(false) }

    // Split tasks into Today, Next 7 days, and Completed
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val startOfToday = calendar.timeInMillis
    val endOfToday = startOfToday + 86400000L
    val endOfNext7Days = startOfToday + (8L * 86400000L)

    val activeTasks = tasks.filter { !it.isCompleted }
    val completedTasks = tasks.filter { it.isCompleted }

    val todayTasks = activeTasks.filter { it.dueTimestamp in startOfToday until endOfToday }
    val next7DaysTasks = activeTasks.filter { it.dueTimestamp in endOfToday until endOfNext7Days }
    val otherTasks = activeTasks.filter { it.dueTimestamp < startOfToday || it.dueTimestamp >= endOfNext7Days }

    val currentTitleText = when {
        selectedCategoryId != null -> categories.find { it.id == selectedCategoryId }?.name ?: "List"
        selectedTag != null -> "#$selectedTag"
        timeFilter == TaskTimeFilter.TODAY -> "Today"
        timeFilter == TaskTimeFilter.TOMORROW -> "Tomorrow"
        timeFilter == TaskTimeFilter.NEXT_7_DAYS -> "Next 7 days"
        timeFilter == TaskTimeFilter.COMPLETED -> "Completed"
        else -> "All Tasks"
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color(0xFFFBF6EC),
                drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
            ) {
                TaskSideDrawerContent(
                    tasks = allTasksRaw,
                    categories = categories,
                    selectedTimeFilter = timeFilter,
                    selectedCategoryId = selectedCategoryId,
                    selectedTag = selectedTag,
                    onSelectTimeFilter = {
                        onSelectTimeFilter(it)
                        coroutineScope.launch { drawerState.close() }
                    },
                    onSelectCategory = {
                        onSelectCategory(it)
                        coroutineScope.launch { drawerState.close() }
                    },
                    onSelectTag = {
                        onSelectTag(it)
                        coroutineScope.launch { drawerState.close() }
                    },
                    onOpenSearch = {
                        coroutineScope.launch { drawerState.close() }
                        onOpenSearch()
                    },
                    onOpenSettings = {
                        coroutineScope.launch { drawerState.close() }
                        onOpenSettings()
                    },
                    onAddList = {
                        coroutineScope.launch { drawerState.close() }
                        onAddList()
                    },
                    onAddTag = {
                        coroutineScope.launch { drawerState.close() }
                        onAddTag()
                    }
                )
            }
        }
    ) {
        Box(modifier = modifier.fillMaxSize().background(CreamBg)) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(14.dp))
                    // Top Header matching Screenshot 1 & 2: All Tasks Pill + Hamburger Menu (Sort Popup Trigger)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFFF3ECE0))
                                .clickable {
                                    coroutineScope.launch { drawerState.open() }
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.GridView,
                                    contentDescription = "Grid View",
                                    tint = WarmText,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = currentTitleText,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = WarmText,
                                        fontSize = 17.sp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Right Hamburger Menu Button for Sort By Popup (Screenshot 1 & 2)
                        IconButton(
                            onClick = { showSortPopup = true },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFF3ECE0))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Sort Menu",
                                tint = WarmText,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                if (!isExactAlarmAllowed) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onRequestExactAlarmPermission() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("⏰", fontSize = 22.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Izin Alarm Belum Aktif",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF856404),
                                            fontSize = 13.sp
                                        )
                                    )
                                    Text(
                                        text = "Ketuk untuk mengizinkan 'Alarms & Reminders' agar tidak ditahan Infinix XOS.",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFF856404).copy(alpha = 0.85f),
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Aktifkan",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF533F03),
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }
                    }
                }

            // Category Horizontal Row with cute circular icons and counts
            item {
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { cat ->
                        val count = tasks.count { it.categoryId == cat.id && !it.isCompleted }
                        val isSelected = selectedCategoryId == cat.id

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable {
                                    onSelectCategory(if (isSelected) null else cat.id)
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) WarmPrimary else Color.Transparent)
                                    .padding(2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CategoryIconBadge(
                                    iconType = cat.iconType,
                                    size = 44.dp,
                                    shapeRadius = 22.dp
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = count.toString(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) WarmPrimary else WarmTextSecondary
                                )
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Cozy Illustrated Hero Banner
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF9)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_task_hero_1791117732542),
                            contentDescription = "Cute cozy desk illustration",
                            modifier = Modifier
                                .size(76.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Hai Ciro! 🐱✨",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = WarmPrimary,
                                    fontSize = 16.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Ada ${todayTasks.size} tugas untuk hari ini. Semangat produktif!",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = WarmTextSecondary,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            )
                        }
                    }
                }
            }

            // Section 1: Today
            item {
                SectionHeader(
                    title = "Today",
                    count = todayTasks.size
                )
            }

            if (todayTasks.isEmpty()) {
                item {
                    EmptySectionPlaceholder(message = "Semua tugas hari ini beres! Waktunya bersantai ☕🍰")
                }
            } else {
                items(todayTasks, key = { it.id }) { task ->
                    val cat = categories.find { it.id == task.categoryId }
                    TaskCard(
                        task = task,
                        category = cat,
                        onToggleComplete = { onToggleTaskComplete(task) },
                        onToggleSubTask = { subId -> onToggleSubTask(task, subId) },
                        onClick = { onTaskClick(task) }
                    )
                }
            }

            // Section 2: Next 7 days
            item {
                Spacer(modifier = Modifier.height(6.dp))
                SectionHeader(
                    title = "Next 7 days",
                    count = next7DaysTasks.size
                )
            }

            if (next7DaysTasks.isEmpty()) {
                item {
                    EmptySectionPlaceholder(message = "Belum ada tugas untuk 7 hari ke depan 🌿")
                }
            } else {
                items(next7DaysTasks, key = { it.id }) { task ->
                    val cat = categories.find { it.id == task.categoryId }
                    TaskCard(
                        task = task,
                        category = cat,
                        onToggleComplete = { onToggleTaskComplete(task) },
                        onToggleSubTask = { subId -> onToggleSubTask(task, subId) },
                        onClick = { onTaskClick(task) }
                    )
                }
            }

            // Section 3: Other Active Tasks (if any)
            if (otherTasks.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    SectionHeader(
                        title = "Upcoming / Later",
                        count = otherTasks.size
                    )
                }
                items(otherTasks, key = { it.id }) { task ->
                    val cat = categories.find { it.id == task.categoryId }
                    TaskCard(
                        task = task,
                        category = cat,
                        onToggleComplete = { onToggleTaskComplete(task) },
                        onToggleSubTask = { subId -> onToggleSubTask(task, subId) },
                        onClick = { onTaskClick(task) }
                    )
                }
            }

            // Section 4: Completed
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isCompletedExpanded = !isCompletedExpanded }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Completed",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = WarmPrimary,
                                fontSize = 16.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = if (isCompletedExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Expand",
                            tint = WarmPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Text(
                        text = completedTasks.size.toString(),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = WarmTextSecondary
                        )
                    )
                }
            }

            if (isCompletedExpanded) {
                items(completedTasks, key = { it.id }) { task ->
                    val cat = categories.find { it.id == task.categoryId }
                    TaskCard(
                        task = task,
                        category = cat,
                        onToggleComplete = { onToggleTaskComplete(task) },
                        onToggleSubTask = { subId -> onToggleSubTask(task, subId) },
                        onClick = { onTaskClick(task) }
                    )
                }
            }

            // Extra bottom spacing for Floating Bottom Bar
            item {
                Spacer(modifier = Modifier.height(110.dp))
            }
        }

        // Sort By Popup Menu (Screenshot 1)
        if (showSortPopup) {
            com.example.ui.components.SortByMenuPopup(
                selectedSort = selectedSort,
                onSortSelected = onSelectSortOption,
                onOpenSettings = onOpenSettings,
                onDismiss = { showSortPopup = false }
            )
        }

        // Floating Pencil FAB (Screenshot 1 & 2)
        FloatingActionButton(
            onClick = onOpenAddTask,
            shape = RoundedCornerShape(20.dp),
            containerColor = Color(0xFFC76F69),
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 90.dp, end = 20.dp)
                .size(54.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Tambah Tugas",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
}

@Composable
private fun SectionHeader(title: String, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = WarmPrimary,
                fontSize = 16.sp
            )
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = WarmTextSecondary
            )
        )
    }
}

@Composable
private fun EmptySectionPlaceholder(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF6F0E6))
            .padding(14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall.copy(
                color = WarmTextSecondary,
                fontSize = 12.sp
            )
        )
    }
}
