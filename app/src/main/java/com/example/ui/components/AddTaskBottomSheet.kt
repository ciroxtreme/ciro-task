package com.example.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryEntity
import com.example.data.model.SubTask
import com.example.data.model.TaskEntity
import com.example.ui.theme.CardBg
import com.example.ui.theme.PastelBlue
import com.example.ui.theme.PastelOrange
import com.example.ui.theme.PastelPink
import com.example.ui.theme.PriorityHigh
import com.example.ui.theme.PriorityLow
import com.example.ui.theme.PriorityMedium
import com.example.ui.theme.WarmPrimary
import com.example.ui.theme.WarmText
import com.example.ui.theme.WarmTextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskBottomSheet(
    sheetState: SheetState,
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onSaveTask: (
        title: String,
        remark: String,
        categoryId: String,
        priority: String,
        tags: String,
        dueTimestamp: Long,
        hasReminder: Boolean,
        subtasks: List<SubTask>
    ) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var remark by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf(categories.firstOrNull()?.id ?: "default") }
    var selectedPriority by remember { mutableStateOf("Medium") }
    val tagsList = remember { mutableStateListOf("Tree", "Bag") }
    var hasReminder by remember { mutableStateOf(true) }
    var showAddTagDialog by remember { mutableStateOf(false) }

    val calendar = remember { Calendar.getInstance().apply { add(Calendar.HOUR_OF_DAY, 1) } }
    var selectedDateMs by remember { mutableStateOf(calendar.timeInMillis) }

    val subtasks = remember { mutableStateListOf<SubTask>() }
    var newSubtaskText by remember { mutableStateOf("") }

    val dateFormat = remember { SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CardBg,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "✨ Tambah Tugas Baru",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = WarmText,
                        fontSize = 20.sp
                    )
                )

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Title input
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Judul Tugas (Task Title)") },
                placeholder = { Text("Contoh: Meeting with client / Belajar Kotlin") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = WarmPrimary,
                    unfocusedBorderColor = Color(0xFFE2DDD5)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Remark input
            OutlinedTextField(
                value = remark,
                onValueChange = { remark = it },
                label = { Text("Catatan / Keterangan (Remark)") },
                placeholder = { Text("Tambahkan detail atau info penting...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = WarmPrimary,
                    unfocusedBorderColor = Color(0xFFE2DDD5)
                ),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Category Selector
            Text(
                text = "Pilih Kategori (List)",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = WarmText
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    val isSelected = cat.id == selectedCategoryId
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) WarmPrimary else Color(0xFFE4DFD7),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .background(if (isSelected) Color(0xFFFBECEB) else Color.White)
                            .clickable { selectedCategoryId = cat.id }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CategoryIconBadge(
                            iconType = cat.iconType,
                            size = 26.dp,
                            shapeRadius = 8.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = cat.name,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) WarmPrimary else WarmText
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Priority
            Text(
                text = "Prioritas",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = WarmText
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("High", "Medium", "Low", "None").forEach { priority ->
                    val isSelected = selectedPriority == priority
                    val priorityColor = when (priority) {
                        "High" -> PriorityHigh
                        "Medium" -> PriorityMedium
                        "Low" -> PriorityLow
                        else -> Color(0xFF8E8882)
                    }
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedPriority = priority },
                        label = {
                            Text(
                                text = priority,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = priorityColor.copy(alpha = 0.2f),
                            selectedLabelColor = priorityColor
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tags Selector Row
            Text(
                text = "Pilih Tags",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = WarmText
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(tagsList) { tag ->
                    val tagData = com.example.util.TagHelper.getTagData(tag)
                    TagPill(tag = tagData, onRemove = { tagsList.remove(tag) })
                }
                item {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFF3ECE0))
                            .clickable { showAddTagDialog = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = WarmText,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Tag",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = WarmText
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Due Date & Time Pickers
            Text(
                text = "Jadwal & Pengingat",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = WarmText
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Date Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFF7F2E8))
                        .clickable {
                            val c = Calendar.getInstance().apply { timeInMillis = selectedDateMs }
                            DatePickerDialog(
                                context,
                                { _, y, m, d ->
                                    c.set(Calendar.YEAR, y)
                                    c.set(Calendar.MONTH, m)
                                    c.set(Calendar.DAY_OF_MONTH, d)
                                    selectedDateMs = c.timeInMillis
                                },
                                c.get(Calendar.YEAR),
                                c.get(Calendar.MONTH),
                                c.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = WarmPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = dateFormat.format(Date(selectedDateMs)),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = WarmText
                        )
                    }
                }

                // Time Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFF7F2E8))
                        .clickable {
                            val c = Calendar.getInstance().apply { timeInMillis = selectedDateMs }
                            TimePickerDialog(
                                context,
                                { _, hour, minute ->
                                    c.set(Calendar.HOUR_OF_DAY, hour)
                                    c.set(Calendar.MINUTE, minute)
                                    c.set(Calendar.SECOND, 0)
                                    c.set(Calendar.MILLISECOND, 0)
                                    selectedDateMs = c.timeInMillis
                                },
                                c.get(Calendar.HOUR_OF_DAY),
                                c.get(Calendar.MINUTE),
                                true
                            ).show()
                        }
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = WarmPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = timeFormat.format(Date(selectedDateMs)),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = WarmText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Pop-up Reminder Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFFFF8EE))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Aktifkan Pop-up Reminder",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = WarmText
                        )
                    )
                    Text(
                        text = "Pop-up heads-up saat jam pengingat tiba",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            color = WarmTextSecondary
                        )
                    )
                }
                Switch(
                    checked = hasReminder,
                    onCheckedChange = { hasReminder = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = WarmPrimary
                    )
                )
            }

            if (hasReminder && !com.example.reminder.ReminderManager.canScheduleExactAlarms(context)) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFF3CD))
                        .clickable { com.example.reminder.ReminderManager.openExactAlarmSettings(context) }
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⏰", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Izin 'Alarms & Reminders' belum aktif. Ketuk untuk mengizinkan agar alarm tidak ditahan XOS.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF856404),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Subtasks Builder
            Text(
                text = "Subtasks (Checklist Anak)",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = WarmText
                )
            )
            Spacer(modifier = Modifier.height(6.dp))

            subtasks.forEachIndexed { idx, sub ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("•", color = WarmPrimary, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = sub.title,
                        style = MaterialTheme.typography.bodyMedium.copy(color = WarmText),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { subtasks.removeAt(idx) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Hapus",
                            tint = Color(0xFFAA9E95),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newSubtaskText,
                    onValueChange = { newSubtaskText = it },
                    placeholder = { Text("Tambah subtask baru...", fontSize = 12.sp) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (newSubtaskText.isNotBlank()) {
                            subtasks.add(
                                SubTask(
                                    id = UUID.randomUUID().toString(),
                                    title = newSubtaskText.trim(),
                                    isCompleted = false
                                )
                            )
                            newSubtaskText = ""
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF0EBE2))
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        tint = WarmText
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save Button
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSaveTask(
                            title.trim(),
                            remark.trim(),
                            selectedCategoryId,
                            selectedPriority,
                            tagsList.joinToString(","),
                            selectedDateMs,
                            hasReminder,
                            subtasks.toList()
                        )
                        onDismiss()
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WarmPrimary)
            ) {
                Text(
                    text = "Simpan Tugas ✨",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
        }
    }

    if (showAddTagDialog) {
        AddTagDialog(
            onDismiss = { showAddTagDialog = false },
            onSelectTag = { tagName ->
                if (!tagsList.contains(tagName)) {
                    tagsList.add(tagName)
                }
            },
            onOpenCustomPicker = {
                // If user wants custom tag
                tagsList.add("Custom")
            }
        )
    }
}
