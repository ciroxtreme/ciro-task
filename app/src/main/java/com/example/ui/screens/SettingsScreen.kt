package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ViewQuilt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import com.example.data.model.CategoryEntity
import com.example.data.model.TaskEntity
import com.example.ui.components.CategoryIconBadge
import com.example.ui.components.CategoryTagPickerModal
import com.example.ui.components.CuteMascotAvatar
import com.example.ui.components.TagIconBadge
import com.example.ui.theme.CreamBg
import com.example.ui.theme.PastelBlue
import com.example.ui.theme.PastelGreen
import com.example.ui.theme.PastelOrange
import com.example.ui.theme.PastelPink
import com.example.ui.theme.PastelPurple
import com.example.ui.theme.WarmPrimary
import com.example.ui.theme.WarmText
import com.example.ui.theme.WarmTextSecondary
import com.example.util.ApkExtractor
import com.example.util.TagHelper
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    tasks: List<TaskEntity>,
    categories: List<CategoryEntity>,
    onTestReminder: () -> Unit,
    onSaveCategory: (name: String, iconType: String, colorHex: String) -> Unit,
    onDeleteCategory: (CategoryEntity) -> Unit,
    onExportCsv: (Context) -> File?,
    onExportBackupJson: (Context) -> File?,
    onImportBackupJson: (Context, Uri, (Int) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val apkInfo = remember { ApkExtractor.getApkInfo(context) }
    var isTaskNotificationEnabled by remember { mutableStateOf(true) }

    var showManageListsSheet by remember { mutableStateOf(false) }
    var showManageTagsSheet by remember { mutableStateOf(false) }
    var showWidgetPreviewDialog by remember { mutableStateOf(false) }
    var editingCategoryForPicker by remember { mutableStateOf<CategoryEntity?>(null) }
    var showCategoryPickerModal by remember { mutableStateOf(false) }
    var editingTagForPicker by remember { mutableStateOf<com.example.util.TagData?>(null) }
    var showTagPickerModal by remember { mutableStateOf(false) }
    var showAlarmLogsDialog by remember { mutableStateOf(false) }

    val completedCount = tasks.count { it.isCompleted }
    val totalCount = tasks.size

    // Save APK File to Storage launcher
    val saveApkLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/vnd.android.package-archive")
    ) { uri ->
        if (uri != null) {
            val success = ApkExtractor.exportApkToUri(context, uri)
            if (success) {
                Toast.makeText(context, "✅ Berhasil menyimpan file APK!", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Import JSON Backup launcher
    val importBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            onImportBackupJson(context, uri) { count ->
                Toast.makeText(context, "✅ Berhasil memulihkan $count tugas!", Toast.LENGTH_LONG).show()
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CreamBg)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))

                // Anime Hero Banner for "Tuan Ciro" (Cool Anime Style Companion Card)
                Card(
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBF3)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFFFFF6EE),
                                        Color(0xFFFDECE8),
                                        Color(0xFFF6EAFA)
                                    )
                                )
                            )
                            .padding(18.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clip(CircleShape)
                                        .background(PastelPurple)
                                        .border(2.dp, WarmPrimary.copy(alpha = 0.5f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CuteMascotAvatar(size = 64.dp)
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Okaeri, Tuan Ciro! ⚔️🐱",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                color = WarmText,
                                                fontSize = 17.sp
                                            )
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(WarmPrimary)
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "RANK: S-CLASS",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.White
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(PastelGreen)
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "Misi: $completedCount/$totalCount Selesai",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF277B43)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Anime Companion Quote
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color.White.copy(alpha = 0.75f))
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = "“Kekuatan sejati bukan berarti tanpa lelah, melainkan tekad untuk menuntaskan setiap misi hari ini. Pantang menyerah, Tuan Ciro! Ganbatte! ✨”",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF5D524C),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            // SECTION 1: Pengaturan Notifikasi & Alarm
            item {
                SectionHeader("Notifikasi & Optimasi Alarm")
            }

            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBF3)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        SettingRowItem(
                            icon = Icons.Default.Notifications,
                            iconBg = PastelPink,
                            title = "Task Notification",
                            subtitle = "Kirim notifikasi & alarm saat tugas jatuh tempo.",
                            trailing = {
                                Switch(
                                    checked = isTaskNotificationEnabled,
                                    onCheckedChange = { isTaskNotificationEnabled = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = WarmPrimary
                                    )
                                )
                            }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        val isExactAlarmGranted = remember(context) { com.example.reminder.ReminderManager.canScheduleExactAlarms(context) }
                        SettingRowItem(
                            icon = Icons.Default.Notifications,
                            iconBg = if (isExactAlarmGranted) PastelGreen else PastelOrange,
                            title = "Izin Alarm Presisi (Exact Alarm)",
                            subtitle = if (isExactAlarmGranted) "✅ Diizinkan (Alarm akan bunyi tepat waktu)" else "⚠️ Dibatasi (Ketuk untuk mengaktifkan izin alarm presisi)",
                            onClick = { com.example.reminder.ReminderManager.openExactAlarmSettings(context) }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        val isIgnoringBattery = remember(context) { com.example.reminder.ReminderManager.isIgnoringBatteryOptimizations(context) }
                        SettingRowItem(
                            icon = Icons.Default.HelpOutline,
                            iconBg = if (isIgnoringBattery) PastelGreen else PastelPink,
                            title = "Matikan Penghemat Baterai",
                            subtitle = if (isIgnoringBattery) "✅ Bebas Pembatasan (Alarm tetap aktif saat HP mati/tutup)" else "⚠️ Dibatasi Sistem (Ketuk agar alarm tidak dimatikan HP)",
                            onClick = { com.example.reminder.ReminderManager.openBatteryOptimizationSettings(context) }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        SettingRowItem(
                            icon = Icons.Default.HelpOutline,
                            iconBg = PastelPurple,
                            title = "Mulai Otomatis (Autostart)",
                            subtitle = "Izinkan aplikasi tetap aktif di latar belakang (Infinix, Xiaomi, Oppo, Vivo).",
                            onClick = { com.example.reminder.ReminderManager.openAutoStartSettings(context) }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        SettingRowItem(
                            icon = Icons.Default.Notifications,
                            iconBg = PastelOrange,
                            title = "Tes Alarm & Notifikasi",
                            subtitle = "Tes langsung bunyi alarm & notifikasi di HP Anda.",
                            onClick = onTestReminder
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        SettingRowItem(
                            icon = Icons.Default.HelpOutline,
                            iconBg = PastelBlue,
                            title = "📋 Log Diagnostik Alarm (Realtime)",
                            subtitle = "Lihat catatan saat alarm dijadwalkan & dipicu di HP Infinix Anda.",
                            onClick = { showAlarmLogsDialog = true }
                        )
                    }
                }
            }

            // SECTION 2: Kelola Tugas & List (Management)
            item {
                SectionHeader("Management")
            }

            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBF3)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Desktop Widget Tester
                        SettingRowItem(
                            icon = Icons.Default.ViewQuilt,
                            iconBg = PastelBlue,
                            title = "Desktop Widget",
                            subtitle = "Lihat & tes tampilan widget desktop Ciro Task.",
                            onClick = { showWidgetPreviewDialog = true }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Kelola List (Lists)
                        SettingRowItem(
                            icon = Icons.Default.FormatListBulleted,
                            iconBg = Color(0xFFFDECE8),
                            title = "List",
                            subtitle = "Kelola ${categories.size} list (edit nama, ganti ikon & warna).",
                            onClick = { showManageListsSheet = true }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Kelola Tag
                        SettingRowItem(
                            icon = Icons.Default.Label,
                            iconBg = Color(0xFFEDE8FD),
                            title = "Tag",
                            subtitle = "Kelola tag tugas dengan ikon squircle lucu.",
                            onClick = { showManageTagsSheet = true }
                        )
                    }
                }
            }

            // SECTION 3: Pencadangan Data Manual (Ekspor & Impor Offline)
            item {
                SectionHeader("Pencadangan Data (Manual)")
            }

            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBF3)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Ekspor Backup JSON
                        SettingRowItem(
                            icon = Icons.Default.FileUpload,
                            iconBg = PastelGreen,
                            title = "Ekspor Cadangan (JSON)",
                            subtitle = "Simpan semua tugas & list ke file backup JSON.",
                            onClick = {
                                val file = onExportBackupJson(context)
                                if (file != null && file.exists()) {
                                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/json"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Bagikan Cadangan Ciro Task"))
                                } else {
                                    Toast.makeText(context, "Gagal membuat file cadangan", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Impor Backup JSON
                        SettingRowItem(
                            icon = Icons.Default.FileDownload,
                            iconBg = Color(0xFFD6F6F5),
                            title = "Impor Cadangan (JSON)",
                            subtitle = "Pulihkan tugas & list dari file JSON yang ada.",
                            onClick = {
                                importBackupLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                            }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Ekspor CSV
                        SettingRowItem(
                            icon = Icons.Default.Share,
                            iconBg = Color(0xFFF3ECE0),
                            title = "Task Export (CSV) 📦",
                            subtitle = "Ekspor seluruh daftar tugas ke format spreadsheet CSV.",
                            onClick = {
                                val file = onExportCsv(context)
                                if (file != null && file.exists()) {
                                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/csv"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Ekspor Tugas CSV"))
                                } else {
                                    Toast.makeText(context, "Gagal mengekspor CSV", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }

            // SECTION 4: Ekstrak File APK (Tanpa PC)
            item {
                SectionHeader("Ekstrak Aplikasi")
            }

            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF6FAF8)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(PastelGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    tint = Color(0xFF277B43),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Ekstrak File APK Ciro Task",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = WarmText,
                                        fontSize = 15.sp
                                    )
                                )
                                Text(
                                    text = "${apkInfo.fileName} • ${apkInfo.sizeFormatted}",
                                    fontSize = 11.sp,
                                    color = WarmTextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { ApkExtractor.shareApk(context) },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Bagikan APK", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { saveApkLauncher.launch("CiroTask_v1.0.apk") },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Simpan ke HP", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WarmText)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(110.dp))
            }
        }
    }

    // Modal Sheet 1: Manage Lists (Category Management)
    if (showManageListsSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showManageListsSheet = false },
            sheetState = sheetState,
            containerColor = Color(0xFFFFFDF8),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 36.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Kelola List (Lists)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = WarmText,
                            fontSize = 18.sp
                        )
                    )
                    IconButton(
                        onClick = {
                            editingCategoryForPicker = null
                            showCategoryPickerModal = true
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Tambah List", tint = WarmPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                categories.forEach { cat ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFF6F0E6))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CategoryIconBadge(
                            iconType = cat.iconType,
                            size = 32.dp,
                            shapeRadius = 10.dp
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = cat.name,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = WarmText,
                                fontSize = 14.sp
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        // Edit icon
                        IconButton(
                            onClick = {
                                editingCategoryForPicker = cat
                                showCategoryPickerModal = true
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = WarmTextSecondary, modifier = Modifier.size(18.dp))
                        }

                        // Delete icon (if more than 1 category)
                        if (categories.size > 1) {
                            IconButton(
                                onClick = { onDeleteCategory(cat) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color(0xFFC76F69), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        editingCategoryForPicker = null
                        showCategoryPickerModal = true
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WarmPrimary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tambah List Baru ✨", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Modal Sheet 2: Manage Tags
    if (showManageTagsSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showManageTagsSheet = false },
            sheetState = sheetState,
            containerColor = Color(0xFFFFFDF8),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 36.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Kelola Tag (Tags)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = WarmText,
                            fontSize = 18.sp
                        )
                    )
                    IconButton(
                        onClick = {
                            editingTagForPicker = null
                            showTagPickerModal = true
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Tambah Tag", tint = WarmPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                TagHelper.CustomTagList.forEach { tagData ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFF6F0E6))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TagIconBadge(tag = tagData, size = 28.dp, shapeRadius = 8.dp)

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = tagData.name,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = WarmText,
                                fontSize = 14.sp
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(
                            onClick = {
                                editingTagForPicker = tagData
                                showTagPickerModal = true
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Tag", tint = WarmTextSecondary, modifier = Modifier.size(18.dp))
                        }

                        IconButton(
                            onClick = { TagHelper.deleteTag(tagData) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Hapus Tag", tint = Color(0xFFC76F69), modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        editingTagForPicker = null
                        showTagPickerModal = true
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WarmPrimary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tambah Tag Baru ✨", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Category Customizer Modal
    if (showCategoryPickerModal) {
        CategoryTagPickerModal(
            initialName = editingCategoryForPicker?.name ?: "",
            initialIcon = editingCategoryForPicker?.iconType ?: "🍔",
            initialColorHex = editingCategoryForPicker?.colorHex ?: "#5CD8D3",
            titleDialog = if (editingCategoryForPicker != null) "Edit List" else "Tambah List Baru",
            onDismiss = {
                showCategoryPickerModal = false
                editingCategoryForPicker = null
            },
            onConfirm = { name, icon, hex ->
                onSaveCategory(name, icon, hex)
                showCategoryPickerModal = false
                editingCategoryForPicker = null
            }
        )
    }

    // Tag Customizer Modal
    if (showTagPickerModal) {
        CategoryTagPickerModal(
            initialName = editingTagForPicker?.name ?: "",
            initialIcon = editingTagForPicker?.icon ?: "🥦",
            initialColorHex = "#5CD8D3",
            titleDialog = if (editingTagForPicker != null) "Edit Tag" else "Tambah Tag Baru",
            onDismiss = {
                showTagPickerModal = false
                editingTagForPicker = null
            },
            onConfirm = { name, icon, hex ->
                val color = try { Color(android.graphics.Color.parseColor(hex)) } catch (_: Exception) { Color(0xFF5CD8D3) }
                TagHelper.saveOrUpdateTag(editingTagForPicker, name, icon, color)
                showTagPickerModal = false
                editingTagForPicker = null
            }
        )
    }

    // Desktop Widget Preview Dialog
    if (showWidgetPreviewDialog) {
        Dialog(onDismissRequest = { showWidgetPreviewDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBF3)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📱 Preview Desktop Widget Ciro Task",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = WarmText
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Mock Widget Card
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F0E6)),
                        modifier = Modifier.fillMaxWidth().padding(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CuteMascotAvatar(size = 28.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Ciro Task Widget", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = WarmText)
                                }
                                Text("Hari Ini", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = WarmPrimary)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            tasks.take(3).forEach { task ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier.size(10.dp).clip(CircleShape).background(if (task.isCompleted) PastelGreen else WarmPrimary)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = task.title,
                                        fontSize = 12.sp,
                                        color = WarmText,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Tekan lama layar Home Screen HP Anda, pilih menu 'Widget' lalu cari Ciro Task untuk menambahkan widget ini ke desktop!",
                        fontSize = 11.sp,
                        color = WarmTextSecondary,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showWidgetPreviewDialog = false },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = WarmPrimary)
                    ) {
                        Text("Tutup", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Alarm Logs Diagnostic Dialog
    if (showAlarmLogsDialog) {
        val logs = remember(showAlarmLogsDialog) { com.example.util.AlarmLogger.getLogs(context) }
        Dialog(onDismissRequest = { showAlarmLogsDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBF3)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📋 Log Diagnostik Alarm",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = WarmText,
                                fontSize = 16.sp
                            )
                        )
                        IconButton(onClick = { showAlarmLogsDialog = false }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Tutup", tint = WarmTextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF222831))
                            .padding(12.dp)
                    ) {
                        if (logs.isEmpty()) {
                            Text(
                                text = "Belum ada log alarm tersimpan.\nCoba buat tugas baru dengan alarm untuk melihat catatan di sini.",
                                color = Color.LightGray,
                                fontSize = 12.sp
                            )
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(logs.size) { idx ->
                                    Text(
                                        text = logs[idx],
                                        color = if (logs[idx].contains("❌")) Color(0xFFFF6B6B) else if (logs[idx].contains("🚀") || logs[idx].contains("✅")) Color(0xFF51CF66) else Color(0xFFE0E0E0),
                                        fontSize = 11.sp,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                        modifier = Modifier.padding(vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                com.example.util.AlarmLogger.clearLogs(context)
                                showAlarmLogsDialog = false
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Hapus Log", fontSize = 12.sp, color = WarmText)
                        }

                        Button(
                            onClick = { showAlarmLogsDialog = false },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = WarmPrimary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Tutup", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.ExtraBold,
            color = WarmPrimary,
            fontSize = 15.sp
        )
    )
}

@Composable
private fun SettingRowItem(
    icon: ImageVector,
    iconBg: Color,
    title: String,
    subtitle: String,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = WarmText,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = WarmText,
                    fontSize = 14.sp
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = WarmTextSecondary,
                    fontSize = 12.sp
                )
            )
        }

        if (trailing != null) {
            trailing()
        } else if (onClick != null) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFFAA9E95),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
