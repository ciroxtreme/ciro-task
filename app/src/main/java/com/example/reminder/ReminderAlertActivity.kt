package com.example.reminder

import android.app.KeyguardManager
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.AppDatabase
import com.example.data.repository.TaskRepository
import com.example.ui.components.CuteMascotAvatar
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PastelPurple
import com.example.ui.theme.WarmPrimary
import com.example.ui.theme.WarmText
import com.example.ui.theme.WarmTextSecondary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderAlertActivity : ComponentActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ensure window wakes up device and displays above keyguard
        turnScreenOnAndUnlock()

        val taskId = intent.getLongExtra(TaskReminderReceiver.EXTRA_TASK_ID, -1L)
        val taskTitle = intent.getStringExtra(TaskReminderReceiver.EXTRA_TASK_TITLE) ?: "Pengingat Tugas!"
        val categoryName = intent.getStringExtra(TaskReminderReceiver.EXTRA_CATEGORY_NAME) ?: "Tugas"
        val remark = intent.getStringExtra(TaskReminderReceiver.EXTRA_REMARK) ?: ""

        val repository = TaskRepository(
            AppDatabase.getDatabase(applicationContext).taskDao(),
            AppDatabase.getDatabase(applicationContext).categoryDao()
        )

        setContent {
            MyApplicationTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.55f))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ReminderPopupCard(
                        title = taskTitle,
                        category = categoryName,
                        remark = remark,
                        onComplete = {
                            AlarmSoundPlayer.stop(applicationContext)
                            ReminderService.stop(applicationContext)
                            AlarmNotificationHelper.cancelAlarmNotification(applicationContext, taskId)
                            if (taskId > 0) {
                                CoroutineScope(Dispatchers.IO).launch {
                                    val task = repository.getTaskById(taskId)
                                    if (task != null) {
                                        repository.updateTask(task.copy(isCompleted = true, completedAt = System.currentTimeMillis()))
                                    }
                                }
                            }
                            finish()
                        },
                        onSnooze = { minutes ->
                            AlarmSoundPlayer.stop(applicationContext)
                            ReminderService.stop(applicationContext)
                            AlarmNotificationHelper.cancelAlarmNotification(applicationContext, taskId)
                            if (taskId > 0) {
                                CoroutineScope(Dispatchers.IO).launch {
                                    val task = repository.getTaskById(taskId)
                                    if (task != null) {
                                        val newTime = System.currentTimeMillis() + (minutes * 60 * 1000)
                                        val updated = task.copy(dueTimestamp = newTime)
                                        repository.updateTask(updated)
                                        ReminderManager.scheduleTaskReminder(applicationContext, updated, categoryName)
                                    }
                                }
                            }
                            finish()
                        },
                        onDismiss = {
                            AlarmSoundPlayer.stop(applicationContext)
                            ReminderService.stop(applicationContext)
                            AlarmNotificationHelper.cancelAlarmNotification(applicationContext, taskId)
                            finish()
                        }
                    )
                }
            }
        }
    }

    private fun turnScreenOnAndUnlock() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            keyguardManager?.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onDestroy() {
        super.onDestroy()
        AlarmSoundPlayer.stop(applicationContext)
        ReminderService.stop(applicationContext)
        val taskId = intent.getLongExtra(TaskReminderReceiver.EXTRA_TASK_ID, -1L)
        AlarmNotificationHelper.cancelAlarmNotification(applicationContext, taskId)
    }
}

@Composable
fun ReminderPopupCard(
    title: String,
    category: String,
    remark: String,
    onComplete: () -> Unit,
    onSnooze: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBF3)),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top row with close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Tutup",
                        tint = Color(0xFFAA9E95)
                    )
                }
            }

            // Cute Mascot or Animated Bell
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(PastelPurple),
                contentAlignment = Alignment.Center
            ) {
                CuteMascotAvatar(size = 72.dp)
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "⏰ Waktunya Tugas!",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = WarmPrimary,
                    fontSize = 20.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = WarmText,
                    fontSize = 17.sp,
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Kategori: $category ${if (remark.isNotBlank()) "\n$remark" else ""}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = WarmTextSecondary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = Modifier.height(22.dp))

            // Action Buttons
            Button(
                onClick = onComplete,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WarmPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Tandai Selesai ✨",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = { onSnooze(10) },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Snooze,
                    contentDescription = null,
                    tint = WarmText,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Tunda 10 Menit ⏰",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = WarmText,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                )
            }
        }
    }
}
