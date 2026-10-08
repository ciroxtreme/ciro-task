package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryEntity
import com.example.data.model.TaskEntity
import com.example.ui.FilterState
import com.example.ui.components.CategoryIconBadge
import com.example.ui.components.TaskCard
import com.example.ui.theme.CreamBg
import com.example.ui.theme.PastelBlue
import com.example.ui.theme.PastelOrange
import com.example.ui.theme.PastelPink
import com.example.ui.theme.PastelPurple
import com.example.ui.theme.WarmPrimary
import com.example.ui.theme.WarmText
import com.example.ui.theme.WarmTextSecondary

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SearchFilterScreen(
    filterState: FilterState,
    categories: List<CategoryEntity>,
    filteredTasks: List<TaskEntity>,
    onQueryChange: (String) -> Unit,
    onCategorySelect: (String?) -> Unit,
    onTagSelect: (String?) -> Unit,
    onPrioritySelect: (String?) -> Unit,
    onClearFilters: () -> Unit,
    onClose: () -> Unit,
    onToggleTaskComplete: (TaskEntity) -> Unit,
    onToggleSubTask: (TaskEntity, String) -> Unit,
    onTaskClick: (TaskEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val commonTags = listOf("Sport", "Food", "Study", "Work", "Pet", "Family")
    val priorities = listOf("High", "Medium", "Low", "None")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CreamBg)
            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
    ) {
        // Top Search Bar (Matching Screenshot 4)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF1EBE0))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = WarmText
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            OutlinedTextField(
                value = filterState.query,
                onValueChange = onQueryChange,
                placeholder = {
                    Text(
                        "Search by task title or remark",
                        fontSize = 13.sp,
                        color = Color(0xFFA69E96)
                    )
                },
                trailingIcon = {
                    if (filterState.query.isNotBlank()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = WarmTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color(0xFFA69E96)
                        )
                    }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = WarmPrimary,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color(0xFFF3ECE0),
                    unfocusedContainerColor = Color(0xFFF3ECE0)
                ),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Filter Matrix Section (Screenshot 4)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFFAF4EA))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Row 1: List / Category
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterLabelPill(text = "List")
                Spacer(modifier = Modifier.width(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    categories.forEach { cat ->
                        val isSelected = filterState.categoryId == cat.id
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) WarmPrimary else Color(0xFFEEE7DC))
                                .clickable { onCategorySelect(cat.id) }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CategoryIconBadge(iconType = cat.iconType, size = 18.dp, shapeRadius = 5.dp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = cat.name,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else WarmText
                            )
                        }
                    }
                }
            }

            // Row 2: Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterLabelPill(text = "Tag")
                Spacer(modifier = Modifier.width(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    commonTags.forEach { tag ->
                        val isSelected = filterState.tag.equals(tag, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) WarmPrimary else Color(0xFFEEE7DC))
                                .clickable { onTagSelect(tag) }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = tag,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else WarmText
                            )
                        }
                    }
                }
            }

            // Row 3: Priority
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterLabelPill(text = "Priority")
                Spacer(modifier = Modifier.width(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    priorities.forEach { pr ->
                        val isSelected = filterState.priority.equals(pr, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) WarmPrimary else Color(0xFFEEE7DC))
                                .clickable { onPrioritySelect(pr) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = pr,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else WarmText
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Results Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Hasil Pencarian (${filteredTasks.size})",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = WarmText
                )
            )

            if (filterState.query.isNotBlank() || filterState.categoryId != null || filterState.tag != null || filterState.priority != null) {
                Text(
                    text = "Reset Filter",
                    fontSize = 12.sp,
                    color = WarmPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onClearFilters() }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Results list
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredTasks, key = { it.id }) { task ->
                val cat = categories.find { it.id == task.categoryId }
                TaskCard(
                    task = task,
                    category = cat,
                    onToggleComplete = { onToggleTaskComplete(task) },
                    onToggleSubTask = { subId -> onToggleSubTask(task, subId) },
                    onClick = { onTaskClick(task) }
                )
            }

            if (filteredTasks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Tidak ada tugas yang cocok dengan filter 🔍",
                            color = WarmTextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun FilterLabelPill(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFC76F69))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}
