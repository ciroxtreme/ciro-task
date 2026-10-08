package com.example.ui.components

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardDoubleArrowUp
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryEntity
import com.example.data.model.SubTaskConverter
import com.example.data.model.TaskEntity
import com.example.ui.theme.CardBg
import com.example.ui.theme.PriorityHigh
import com.example.ui.theme.PriorityLow
import com.example.ui.theme.PriorityMedium
import com.example.ui.theme.WarmPrimary
import com.example.ui.theme.WarmText
import com.example.ui.theme.WarmTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TaskCard(
    task: TaskEntity,
    category: CategoryEntity?,
    onToggleComplete: () -> Unit,
    onToggleSubTask: (String) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val subtasks = SubTaskConverter.fromJson(task.subtasksJson)
    val dateFormat = SimpleDateFormat("MMM d HH:mm", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(task.dueTimestamp))

    val cardBgColor by animateColorAsState(
        targetValue = if (task.isCompleted) Color(0xFFF7F4EC) else CardBg,
        label = "cardBg"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (task.isCompleted) 0.dp else 1.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cute Squircle Checkbox
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(
                            width = 2.dp,
                            color = if (task.isCompleted) WarmPrimary else Color(0xFFB5ADA4),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .background(if (task.isCompleted) WarmPrimary else Color.Transparent)
                        .clickable { onToggleComplete() },
                    contentAlignment = Alignment.Center
                ) {
                    if (task.isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Category Icon Badge
                CategoryIconBadge(
                    iconType = category?.iconType ?: "book",
                    size = 38.dp,
                    shapeRadius = 12.dp
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Title and details
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (task.isCompleted) WarmTextSecondary else WarmText,
                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                        )
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // 1. List / Category Pill (e.g. [ Pet ] in screenshot)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFEBE5DC))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = category?.name ?: "Default",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    color = WarmText,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        // 2. Date Pill (e.g. [ Oct 06 19:49 ] in screenshot)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFEBE5DC))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = formattedDate,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    color = WarmText,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }

                        // Recurring Icon if any
                        if (task.isRecurring) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "Recurring",
                                tint = Color(0xFF5BA4CF),
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        // 3. Tags as cute mini squircle icon badges [ 🥦 ] [ 🎒 ] [ 🦷 ]
                        if (task.tags.isNotBlank()) {
                            val tagsList = task.tags.split(",").map { it.trim() }.filter { it.isNotBlank() }
                            tagsList.forEach { tagStr ->
                                val tagData = com.example.util.TagHelper.getTagData(tagStr)
                                TagIconBadge(tag = tagData, size = 23.dp, shapeRadius = 7.dp)
                            }
                        }
                    }
                }

                // Priority Icon
                when (task.priority.lowercase()) {
                    "high" -> {
                        Icon(
                            imageVector = Icons.Default.KeyboardDoubleArrowUp,
                            contentDescription = "High Priority",
                            tint = PriorityHigh,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    "medium" -> {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Medium Priority",
                            tint = PriorityMedium,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    "low" -> {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Low Priority",
                            tint = PriorityLow,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Subtasks checklist (if any)
            if (subtasks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 74.dp)
                ) {
                    subtasks.forEach { sub ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                                .clickable { onToggleSubTask(sub.id) }
                        ) {
                            Text(
                                text = "–",
                                fontSize = 12.sp,
                                color = WarmTextSecondary,
                                modifier = Modifier.padding(end = 4.dp)
                            )

                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .border(
                                        width = 1.5.dp,
                                        color = if (sub.isCompleted) WarmPrimary else Color(0xFFB5ADA4),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .background(if (sub.isCompleted) WarmPrimary else Color.Transparent),
                                contentAlignment = Alignment.Center
                            ) {
                                if (sub.isCompleted) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Subtask Completed",
                                        tint = Color.White,
                                        modifier = Modifier.size(10.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Text(
                                text = sub.title,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    color = if (sub.isCompleted) WarmTextSecondary else WarmText,
                                    textDecoration = if (sub.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
