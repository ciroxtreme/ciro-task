package com.example.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardDoubleArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryEntity
import com.example.data.model.SubTask
import com.example.data.model.SubTaskConverter
import com.example.data.model.TaskEntity
import com.example.ui.theme.FredokaFontFamily
import com.example.ui.theme.PastelBlue
import com.example.ui.theme.PastelGreen
import com.example.ui.theme.PastelOrange
import com.example.ui.theme.PastelPink
import com.example.ui.theme.PastelPurple
import com.example.ui.theme.PriorityHigh
import com.example.ui.theme.PriorityLow
import com.example.ui.theme.PriorityMedium
import com.example.ui.theme.QuicksandFontFamily
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
fun TaskEditBottomSheet(
    task: TaskEntity,
    categories: List<CategoryEntity>,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onSaveTask: (TaskEntity) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit,
    onOpenCategoryPicker: () -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(task.title) }
    var remark by remember { mutableStateOf(task.remark) }
    var categoryId by remember { mutableStateOf(task.categoryId) }
    var priority by remember { mutableStateOf(task.priority) }
    var dueTimestamp by remember { mutableLongStateOf(task.dueTimestamp) }
    var isCompleted by remember { mutableStateOf(task.isCompleted) }
    var isRecurring by remember { mutableStateOf(task.isRecurring) }
    var hasReminder by remember { mutableStateOf(task.hasReminder) }

    val tagsList = remember {
        mutableStateListOf<String>().apply {
            if (task.tags.isNotBlank()) {
                addAll(task.tags.split(",").map { it.trim() }.filter { it.isNotBlank() })
            }
        }
    }

    val subtasks = remember {
        mutableStateListOf<SubTask>().apply {
            addAll(SubTaskConverter.fromJson(task.subtasksJson))
        }
    }

    var newSubtaskInput by remember { mutableStateOf("") }
    var showMoreMenu by remember { mutableStateOf(false) }
    var showAddTagDialog by remember { mutableStateOf(false) }
    var showCustomTagPicker by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("MMM dd HH:mm", Locale.getDefault()) }

    // Helper to persist edits
    fun persistEdits() {
        val updated = task.copy(
            title = title.ifBlank { task.title },
            remark = remark,
            categoryId = categoryId,
            priority = priority,
            tags = tagsList.joinToString(","),
            dueTimestamp = dueTimestamp,
            isCompleted = isCompleted,
            isRecurring = isRecurring,
            hasReminder = hasReminder,
            subtasksJson = SubTaskConverter.toJson(subtasks.toList())
        )
        onSaveTask(updated)
    }

    ModalBottomSheet(
        onDismissRequest = {
            persistEdits()
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = Color(0xFFFFFDF8),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Top Bar: Checkbox + Date Chip + Priority Chip + More Menu (Screenshot 1)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Checkbox squircle
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(
                            width = 2.dp,
                            color = if (isCompleted) WarmPrimary else Color(0xFFB5ADA4),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .background(if (isCompleted) WarmPrimary else Color.Transparent)
                        .clickable {
                            isCompleted = !isCompleted
                            persistEdits()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Date & Time Chip [ ⏱ Oct 07 19:49 ] (Screenshot 1)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF6F0E6))
                        .clickable {
                            val c = Calendar.getInstance().apply { timeInMillis = dueTimestamp }
                            DatePickerDialog(
                                context,
                                { _, y, m, d ->
                                    c.set(Calendar.YEAR, y)
                                    c.set(Calendar.MONTH, m)
                                    c.set(Calendar.DAY_OF_MONTH, d)
                                    TimePickerDialog(
                                        context,
                                        { _, hour, minute ->
                                            c.set(Calendar.HOUR_OF_DAY, hour)
                                            c.set(Calendar.MINUTE, minute)
                                            dueTimestamp = c.timeInMillis
                                            persistEdits()
                                        },
                                        c.get(Calendar.HOUR_OF_DAY),
                                        c.get(Calendar.MINUTE),
                                        true
                                    ).show()
                                },
                                c.get(Calendar.YEAR),
                                c.get(Calendar.MONTH),
                                c.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = WarmPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = dateFormat.format(Date(dueTimestamp)),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = WarmText
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Priority Chip [ ⬆ High ] (Screenshot 1)
                val (priorityColor, priorityIcon) = when (priority.lowercase()) {
                    "high" -> Pair(PriorityHigh, Icons.Default.KeyboardDoubleArrowUp)
                    "medium" -> Pair(PriorityMedium, Icons.Default.KeyboardArrowUp)
                    "low" -> Pair(PriorityLow, Icons.Default.KeyboardArrowDown)
                    else -> Pair(Color(0xFF908A83), Icons.Default.KeyboardArrowDown)
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(priorityColor.copy(alpha = 0.16f))
                        .clickable {
                            priority = when (priority) {
                                "High" -> "Medium"
                                "Medium" -> "Low"
                                "Low" -> "None"
                                else -> "High"
                            }
                            persistEdits()
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = priorityIcon,
                        contentDescription = null,
                        tint = priorityColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = priority,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = priorityColor
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // More Menu Button (☰)
                Box {
                    IconButton(onClick = { showMoreMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More",
                            tint = WarmText
                        )
                    }

                    DropdownMenu(
                        expanded = showMoreMenu,
                        onDismissRequest = { showMoreMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Hapus Tugas (Delete)") },
                            onClick = {
                                showMoreMenu = false
                                onDeleteTask(task)
                                onDismiss()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = WarmPrimary)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (isRecurring) "Matikan Pengulangan" else "Jadikan Berulang (Daily)") },
                            onClick = {
                                isRecurring = !isRecurring
                                persistEdits()
                                showMoreMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (hasReminder) "Matikan Pop-up Reminder" else "Aktifkan Pop-up Reminder") },
                            onClick = {
                                hasReminder = !hasReminder
                                persistEdits()
                                showMoreMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Task Title (Big Bold text field) (Screenshot 1)
            BasicTextField(
                value = title,
                onValueChange = {
                    title = it
                    persistEdits()
                },
                textStyle = TextStyle(
                    fontFamily = FredokaFontFamily,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCompleted) WarmTextSecondary else WarmText,
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
                ),
                cursorBrush = SolidColor(WarmPrimary),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Tags and Category Pills Row (Screenshot 1: [ 🎒 Study ] [ 🏋️ Sport ] [ 🦷 Tooth ] [ 🖊 Design ])
            val currentCat = categories.find { it.id == categoryId }
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Category / List Utama Pill (Screenshot 1)
                item {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(PastelPink)
                            .clickable { onOpenCategoryPicker() }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CategoryIconBadge(
                            iconType = currentCat?.iconType ?: "book",
                            size = 22.dp,
                            shapeRadius = 6.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = currentCat?.name ?: "Default",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = WarmText
                        )
                    }
                }

                // Tag Pills (with remove button)
                items(tagsList) { tag ->
                    val tagData = com.example.util.TagHelper.getTagData(tag)
                    TagPill(
                        tag = tagData,
                        onRemove = {
                            tagsList.remove(tag)
                            persistEdits()
                        }
                    )
                }

                // Add Tag Button
                item {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFF3ECE0))
                            .clickable { showAddTagDialog = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Tag",
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

            // Remark Section with vertical line accent (Screenshot 1)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                // Vertical accent line
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(WarmPrimary.copy(alpha = 0.5f))
                )

                Spacer(modifier = Modifier.width(10.dp))

                BasicTextField(
                    value = remark,
                    onValueChange = {
                        remark = it
                        persistEdits()
                    },
                    textStyle = TextStyle(
                        fontFamily = QuicksandFontFamily,
                        fontSize = 13.sp,
                        color = WarmTextSecondary,
                        lineHeight = 18.sp
                    ),
                    cursorBrush = SolidColor(WarmPrimary),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Subtasks Section (Screenshot 1)
            subtasks.forEachIndexed { index, sub ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "–",
                        fontSize = 16.sp,
                        color = WarmTextSecondary,
                        modifier = Modifier.padding(end = 6.dp)
                    )

                    // Rounded subtask checkbox (⭕ in Screenshot 1)
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .border(
                                width = 1.5.dp,
                                color = if (sub.isCompleted) WarmPrimary else Color(0xFFB5ADA4),
                                shape = CircleShape
                            )
                            .background(if (sub.isCompleted) WarmPrimary else Color.Transparent)
                            .clickable {
                                subtasks[index] = sub.copy(isCompleted = !sub.isCompleted)
                                persistEdits()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (sub.isCompleted) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = sub.title,
                        style = TextStyle(
                            fontFamily = QuicksandFontFamily,
                            fontSize = 13.sp,
                            color = if (sub.isCompleted) WarmTextSecondary else WarmText,
                            textDecoration = if (sub.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    // Delete subtask cross
                    IconButton(
                        onClick = {
                            subtasks.removeAt(index)
                            persistEdits()
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Hapus",
                            tint = Color(0xFFAA9E95),
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    // Drag handle (☰ in Screenshot 1)
                    Icon(
                        imageVector = Icons.Default.DragHandle,
                        contentDescription = "Reorder",
                        tint = Color(0xFFAA9E95),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Ghost row: "- ⭕ Add an sub task." (Screenshot 1)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "–",
                    fontSize = 16.sp,
                    color = Color(0xFFB5ADA4),
                    modifier = Modifier.padding(end = 6.dp)
                )

                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, Color(0xFFC7BFB6), CircleShape)
                )

                Spacer(modifier = Modifier.width(8.dp))

                BasicTextField(
                    value = newSubtaskInput,
                    onValueChange = { newSubtaskInput = it },
                    textStyle = TextStyle(
                        fontFamily = QuicksandFontFamily,
                        fontSize = 13.sp,
                        color = WarmText
                    ),
                    cursorBrush = SolidColor(WarmPrimary),
                    modifier = Modifier.weight(1f),
                    decorationBox = { innerTextField ->
                        if (newSubtaskInput.isEmpty()) {
                            Text(
                                text = "Add an sub task...",
                                fontSize = 13.sp,
                                color = Color(0xFFA69E96)
                            )
                        }
                        innerTextField()
                    }
                )

                if (newSubtaskInput.isNotBlank()) {
                    IconButton(
                        onClick = {
                            subtasks.add(
                                SubTask(
                                    id = UUID.randomUUID().toString(),
                                    title = newSubtaskInput.trim(),
                                    isCompleted = false
                                )
                            )
                            newSubtaskInput = ""
                            persistEdits()
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Tambah",
                            tint = WarmPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save / Done Button
            Button(
                onClick = {
                    persistEdits()
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WarmPrimary)
            ) {
                Text(
                    text = "Simpan Perubahan ✨",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
        }
    }

    // Quick tag selector dialog
    if (showAddTagDialog) {
        AddTagDialog(
            onDismiss = { showAddTagDialog = false },
            onSelectTag = { tagName ->
                if (!tagsList.contains(tagName)) {
                    tagsList.add(tagName)
                    persistEdits()
                }
            },
            onOpenCustomPicker = {
                showCustomTagPicker = true
            }
        )
    }

    // Modal to create custom tag with full icon & color grid
    if (showCustomTagPicker) {
        CategoryTagPickerModal(
            initialName = "",
            initialIcon = "🏷️",
            titleDialog = "Tambah Tag Kustom",
            onDismiss = { showCustomTagPicker = false },
            onConfirm = { tagName, emoji, _ ->
                if (tagName.isNotBlank() && !tagsList.contains(tagName)) {
                    tagsList.add(tagName)
                    persistEdits()
                }
            }
        )
    }
}
