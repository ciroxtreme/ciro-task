package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryEntity
import com.example.data.model.TaskEntity
import com.example.ui.components.CategoryIconBadge
import com.example.ui.components.TaskCard
import com.example.ui.theme.CreamBg
import com.example.ui.theme.WarmPrimary
import com.example.ui.theme.WarmText
import com.example.ui.theme.WarmTextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CalendarScreen(
    tasks: List<TaskEntity>,
    categories: List<CategoryEntity>,
    selectedDateMs: Long,
    onSelectDate: (Long) -> Unit,
    onOpenAddTask: () -> Unit,
    onToggleTaskComplete: (TaskEntity) -> Unit,
    onToggleSubTask: (TaskEntity, String) -> Unit,
    onTaskClick: (TaskEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    // Current viewed month offset
    var monthOffset by remember { mutableIntStateOf(0) }

    val calendar = remember(monthOffset) {
        Calendar.getInstance().apply {
            add(Calendar.MONTH, monthOffset)
            set(Calendar.DAY_OF_MONTH, 1)
        }
    }

    val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    val monthYearText = monthYearFormat.format(calendar.time)

    // Compute calendar grid days
    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) // 1=Sun, 2=Mon...
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

    val selectedCal = Calendar.getInstance().apply { timeInMillis = selectedDateMs }
    val selYear = selectedCal.get(Calendar.YEAR)
    val selMonth = selectedCal.get(Calendar.MONTH)
    val selDay = selectedCal.get(Calendar.DAY_OF_MONTH)

    // Filter tasks for selected day
    val selectedDayStart = Calendar.getInstance().apply {
        timeInMillis = selectedDateMs
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    val selectedDayEnd = selectedDayStart + 86400000L

    val dayTasks = tasks.filter { it.dueTimestamp in selectedDayStart until selectedDayEnd }
        .sortedBy { it.dueTimestamp }

    Box(modifier = modifier.fillMaxSize().background(CreamBg)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(14.dp))
                // Calendar Header (Screenshot 2)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFF3ECE0))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = WarmPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = monthYearText,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = WarmText,
                                fontSize = 15.sp
                            )
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = { monthOffset-- },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF3ECE0))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "Prev",
                                tint = WarmText,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = { monthOffset++ },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF3ECE0))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Next",
                                tint = WarmText,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Days of week
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { d ->
                        Text(
                            text = d,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = WarmTextSecondary,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Monthly Grid
                val totalCells = ((firstDayOfWeek - 1 + daysInMonth + 6) / 7) * 7
                val weeks = totalCells / 7

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (week in 0 until weeks) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            for (dayOfWeek in 1..7) {
                                val cellIndex = week * 7 + (dayOfWeek - 1)
                                val dayNumber = cellIndex - (firstDayOfWeek - 2)

                                if (dayNumber in 1..daysInMonth) {
                                    val cellCal = Calendar.getInstance().apply {
                                        timeInMillis = calendar.timeInMillis
                                        set(Calendar.DAY_OF_MONTH, dayNumber)
                                    }
                                    val isSelected = cellCal.get(Calendar.YEAR) == selYear &&
                                            cellCal.get(Calendar.MONTH) == selMonth &&
                                            cellCal.get(Calendar.DAY_OF_MONTH) == selDay

                                    // Tasks on this day
                                    val dayStart = cellCal.apply {
                                        set(Calendar.HOUR_OF_DAY, 0)
                                        set(Calendar.MINUTE, 0)
                                        set(Calendar.SECOND, 0)
                                    }.timeInMillis
                                    val dayEnd = dayStart + 86400000L
                                    val tasksOnThisDay = tasks.filter { it.dueTimestamp in dayStart until dayEnd }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isSelected) Color(0xFFC76F69)
                                                else if (tasksOnThisDay.isNotEmpty()) Color(0xFFFAF4EA)
                                                else Color.Transparent
                                            )
                                            .clickable { onSelectDate(cellCal.timeInMillis) }
                                            .padding(vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = dayNumber.toString(),
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                        color = if (isSelected) Color.White else WarmText,
                                                        fontSize = 12.sp
                                                    )
                                                )
                                                if (tasksOnThisDay.isNotEmpty() && !isSelected) {
                                                    Spacer(modifier = Modifier.width(2.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .size(4.dp)
                                                            .clip(CircleShape)
                                                            .background(Color(0xFFC76F69))
                                                    )
                                                }
                                            }

                                            // Tiny category icons (Matching Screenshot 2)
                                            if (tasksOnThisDay.isNotEmpty()) {
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(1.dp),
                                                    modifier = Modifier.padding(top = 2.dp)
                                                ) {
                                                    tasksOnThisDay.take(2).forEach { t ->
                                                        val cat = categories.find { it.id == t.categoryId }
                                                        CategoryIconBadge(
                                                            iconType = cat?.iconType ?: "book",
                                                            size = 12.dp,
                                                            shapeRadius = 3.dp
                                                        )
                                                    }
                                                }
                                            } else {
                                                Spacer(modifier = Modifier.height(14.dp))
                                            }
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
            }

            // Timeline Header
            item {
                Text(
                    text = "Timeline Jadwal",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = WarmPrimary,
                        fontSize = 16.sp
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            if (dayTasks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFF6F0E6))
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Tidak ada jadwal untuk tanggal ini. Klik pensil untuk menambahkan! 🌸",
                            color = WarmTextSecondary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // Group by hour
                val timeFormat = SimpleDateFormat("HH:00", Locale.getDefault())
                val groupedByHour = dayTasks.groupBy { timeFormat.format(Date(it.dueTimestamp)) }

                groupedByHour.forEach { (hour, hourTasks) ->
                    item {
                        Text(
                            text = hour,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = WarmPrimary,
                                fontSize = 14.sp
                            ),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    items(hourTasks, key = { it.id }) { task ->
                        val cat = categories.find { it.id == task.categoryId }
                        TaskCard(
                            task = task,
                            category = cat,
                            onToggleComplete = { onToggleTaskComplete(task) },
                            onToggleSubTask = { subId -> onToggleSubTask(task, subId) },
                            onClick = { onTaskClick(task) }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(110.dp))
            }
        }

        // Floating Pencil FAB
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
