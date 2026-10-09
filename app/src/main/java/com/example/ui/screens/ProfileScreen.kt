package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TaskEntity
import com.example.ui.components.CuteMascotAvatar
import com.example.ui.theme.CardBg
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

@Composable
fun ProfileScreen(
    tasks: List<TaskEntity>,
    onTestReminder: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val apkInfo = remember { ApkExtractor.getApkInfo(context) }

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
            } else {
                Toast.makeText(context, "Gagal menyimpan APK", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Box(modifier = modifier.fillMaxSize().background(CreamBg)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(14.dp))
                // Mascot Header
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CuteMascotAvatar(size = 64.dp)

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = "Ciro Task Companion 🐱",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = WarmText,
                                    fontSize = 17.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tugas Selesai: $completedCount dari $totalCount",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = WarmTextSecondary,
                                    fontSize = 13.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(PastelGreen)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (completedCount >= 5) "Semangat Produktif! 🔥" else "Ayo mulai tugasmu! ✨",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF277B43)
                                )
                            }
                        }
                    }
                }
            }

            // Featured Card 1: Reminder Pop-up Tester
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9F5)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(PastelPink),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = WarmPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Alarm & Notifikasi Aktif",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = WarmText,
                                    fontSize = 15.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Pengingat tugas akan berbunyi tepat waktu dengan notifikasi alarm interaktif (opsi Selesai, Tunda 5 mnt, atau Matikan).",
                            fontSize = 12.sp,
                            color = WarmTextSecondary,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = onTestReminder,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = WarmPrimary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "⚡ Tes Alarm & Notifikasi Sekarang",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // Featured Card 2: APK Extractor (Khusus pengguna HP tanpa PC)
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
                                    text = "Ekstrak File APK (Tanpa PC)",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = WarmText,
                                        fontSize = 15.sp
                                    )
                                )
                                Text(
                                    text = "Simpan ke file HP atau upload ke file.wiki",
                                    fontSize = 11.sp,
                                    color = WarmTextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Info Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Nama File:", fontSize = 12.sp, color = WarmTextSecondary)
                                    Text(apkInfo.fileName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WarmText)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Perkiraan Ukuran:", fontSize = 12.sp, color = WarmTextSecondary)
                                    Text(apkInfo.sizeFormatted, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WarmText)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Versi Aplikasi:", fontSize = 12.sp, color = WarmTextSecondary)
                                    Text("v${apkInfo.versionName}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WarmText)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Button 1: Share directly (can choose browser / upload to file.wiki / WhatsApp / Telegram)
                        Button(
                            onClick = { ApkExtractor.shareApk(context) },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "🚀 Bagikan / Upload APK Langsung",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Button 2: Save directly to phone storage (e.g. Download folder)
                        OutlinedButton(
                            onClick = {
                                saveApkLauncher.launch("CiroTask_v1.0.apk")
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.SaveAlt,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "💾 Simpan ke Folder Download HP",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = WarmText
                            )
                        }
                    }
                }
            }

            // Info Tips Box
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.TipsAndUpdates,
                            contentDescription = null,
                            tint = PastelOrange,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Tips Cara Upload ke file.wiki:",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = WarmText
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "1. Klik tombol 'Simpan ke Folder Download HP' di atas.\n" +
                                        "2. Buka browser HP Anda lalu kunjungi file.wiki.\n" +
                                        "3. Pilih file CiroTask_v1.0.apk dari folder Download untuk diupload!",
                                fontSize = 12.sp,
                                color = WarmTextSecondary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(110.dp))
            }
        }
    }
}
