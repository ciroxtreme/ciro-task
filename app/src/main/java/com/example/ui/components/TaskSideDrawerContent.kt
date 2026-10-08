package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TableRows
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryEntity
import com.example.data.model.TaskEntity
import com.example.data.model.TaskTimeFilter
import com.example.ui.theme.WarmPrimary
import com.example.ui.theme.WarmText
import com.example.ui.theme.WarmTextSecondary
import com.example.util.TagData
import com.example.util.TagHelper
import java.util.Calendar

@Composable
fun TaskSideDrawerContent(
    tasks: List<TaskEntity>,
    categories: List<CategoryEntity>,
    selectedTimeFilter: TaskTimeFilter,
    selectedCategoryId: String?,
    selectedTag: String?,
    onSelectTimeFilter: (TaskTimeFilter) -> Unit,
    onSelectCategory: (String?) -> Unit,
    onSelectTag: (String?) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    onAddList: () -> Unit,
    onAddTag: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Calculate counts
    val cal = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }
    val todayStart = cal.timeInMillis
    val todayEnd = todayStart + (24 * 3600 * 1000) - 1
    val tomorrowStart = todayEnd + 1
    val tomorrowEnd = tomorrowStart + (24 * 3600 * 1000) - 1
    val next7DaysEnd = todayStart + (7L * 24 * 3600 * 1000) - 1

    val allTasksCount = tasks.count { !it.isCompleted }
    val todayCount = tasks.count { !it.isCompleted && it.dueTimestamp in todayStart..todayEnd }
    val tomorrowCount = tasks.count { !it.isCompleted && it.dueTimestamp in tomorrowStart..tomorrowEnd }
    val next7DaysCount = tasks.count { !it.isCompleted && it.dueTimestamp in todayStart..next7DaysEnd }
    val completedCount = tasks.count { it.isCompleted }

    // Unique active tags with their counts
    val tagCounts = remember(tasks) {
        val map = mutableMapOf<String, Int>()
        tasks.forEach { t ->
            if (t.tags.isNotBlank()) {
                t.tags.split(",").map { it.trim() }.filter { it.isNotBlank() }.forEach { tag ->
                    map[tag] = (map[tag] ?: 0) + 1
                }
            }
        }
        // Ensure default tags exist
        listOf("Sport", "Vegetable", "Food", "Tooth", "Tree", "Bag").forEach { dt ->
            if (!map.containsKey(dt)) map[dt] = 0
        }
        map.toList()
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(290.dp)
            .background(Color(0xFFFBF6EC))
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Search Bar Pill (Screenshot 1: [ 🔍 Search ])
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFF2ECE0))
                .clickable { onOpenSearch() }
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = WarmTextSecondary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Search",
                fontSize = 14.sp,
                color = WarmTextSecondary,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // SECTION 1: Tasks (with Settings gear icon)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tasks",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = WarmText,
                    fontSize = 15.sp
                )
            )
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = WarmTextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Tasks Filter Items (Screenshot 1: All Tasks, Today, Tomorrow, Next 7 days, Completed)
        TaskFilterPill(
            icon = Icons.Default.GridView,
            title = "All Tasks",
            count = allTasksCount,
            isSelected = selectedTimeFilter == TaskTimeFilter.ALL && selectedCategoryId == null && selectedTag == null,
            onClick = {
                onSelectCategory(null)
                onSelectTag(null)
                onSelectTimeFilter(TaskTimeFilter.ALL)
            }
        )

        TaskFilterPill(
            icon = Icons.Default.Cloud,
            title = "Today",
            count = todayCount,
            isSelected = selectedTimeFilter == TaskTimeFilter.TODAY,
            onClick = {
                onSelectCategory(null)
                onSelectTag(null)
                onSelectTimeFilter(TaskTimeFilter.TODAY)
            }
        )

        TaskFilterPill(
            icon = Icons.Default.Nightlight,
            title = "Tomorrow",
            count = tomorrowCount,
            isSelected = selectedTimeFilter == TaskTimeFilter.TOMORROW,
            onClick = {
                onSelectCategory(null)
                onSelectTag(null)
                onSelectTimeFilter(TaskTimeFilter.TOMORROW)
            }
        )

        TaskFilterPill(
            icon = Icons.Default.Today,
            title = "Next 7 days",
            count = next7DaysCount,
            isSelected = selectedTimeFilter == TaskTimeFilter.NEXT_7_DAYS,
            onClick = {
                onSelectCategory(null)
                onSelectTag(null)
                onSelectTimeFilter(TaskTimeFilter.NEXT_7_DAYS)
            }
        )

        TaskFilterPill(
            icon = Icons.Default.TableRows,
            title = "Completed",
            count = completedCount,
            isSelected = selectedTimeFilter == TaskTimeFilter.COMPLETED,
            onClick = {
                onSelectCategory(null)
                onSelectTag(null)
                onSelectTimeFilter(TaskTimeFilter.COMPLETED)
            }
        )

        Spacer(modifier = Modifier.height(18.dp))

        // SECTION 2: List (with + button)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "List",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = WarmText,
                    fontSize = 15.sp
                )
            )
            IconButton(
                onClick = onAddList,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add List",
                    tint = WarmText,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // List / Category items (Screenshot 1: Default, Family, Study, Pet, Health)
        categories.forEach { cat ->
            val count = tasks.count { !it.isCompleted && it.categoryId == cat.id }
            val isSelected = selectedCategoryId == cat.id

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isSelected) WarmPrimary.copy(alpha = 0.2f) else Color(0xFFF6F0E6))
                    .clickable {
                        onSelectTag(null)
                        onSelectTimeFilter(TaskTimeFilter.ALL)
                        onSelectCategory(if (isSelected) null else cat.id)
                    }
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CategoryIconBadge(
                    iconType = cat.iconType,
                    size = 28.dp,
                    shapeRadius = 8.dp
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = cat.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = WarmText,
                        fontSize = 13.sp
                    ),
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = count.toString(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = WarmTextSecondary
                )

                Spacer(modifier = Modifier.width(6.dp))

                Icon(
                    imageVector = Icons.Default.DragHandle,
                    contentDescription = null,
                    tint = Color(0xFFAFA79D),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // SECTION 3: Tag (with + button)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tag",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = WarmText,
                    fontSize = 15.sp
                )
            )
            IconButton(
                onClick = onAddTag,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Tag",
                    tint = WarmText,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Tag items (Screenshot 1: Sport, Vegetable, Food, Tooth)
        tagCounts.take(6).forEach { (tagName, count) ->
            val tagData = TagHelper.getTagData(tagName)
            val isSelected = selectedTag.equals(tagName, ignoreCase = true)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isSelected) WarmPrimary.copy(alpha = 0.2f) else Color(0xFFF6F0E6))
                    .clickable {
                        onSelectCategory(null)
                        onSelectTimeFilter(TaskTimeFilter.ALL)
                        onSelectTag(if (isSelected) null else tagName)
                    }
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TagIconBadge(tag = tagData, size = 26.dp, shapeRadius = 8.dp)

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = tagData.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = WarmText,
                        fontSize = 13.sp
                    ),
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = count.toString(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = WarmTextSecondary
                )

                Spacer(modifier = Modifier.width(6.dp))

                Icon(
                    imageVector = Icons.Default.DragHandle,
                    contentDescription = null,
                    tint = Color(0xFFAFA79D),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun TaskFilterPill(
    icon: ImageVector,
    title: String,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) WarmPrimary else Color(0xFFF6F0E6))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = if (isSelected) Color.White else WarmTextSecondary,
            modifier = Modifier.size(18.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else WarmText,
                fontSize = 13.sp
            ),
            modifier = Modifier.weight(1f)
        )

        Text(
            text = count.toString(),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.White else WarmTextSecondary
        )
    }
}
