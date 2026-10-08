package com.example.reminder

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.R
import com.example.util.AlarmLogger

class ReminderService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_STOP_ALARM) {
            AlarmSoundPlayer.stop(this)
            stopSelf()
            return START_NOT_STICKY
        }

        val taskId = intent?.getLongExtra(TaskReminderReceiver.EXTRA_TASK_ID, -1L) ?: -1L
        val taskTitle = intent?.getStringExtra(TaskReminderReceiver.EXTRA_TASK_TITLE) ?: "Pengingat Tugas!"
        val categoryName = intent?.getStringExtra(TaskReminderReceiver.EXTRA_CATEGORY_NAME) ?: "Tugas"
        val remark = intent?.getStringExtra(TaskReminderReceiver.EXTRA_REMARK) ?: ""

        AlarmLogger.log(this, "🚀 [ReminderService] Dipanggil untuk Task: $taskTitle")

        // 1. Show Notification directly
        AlarmNotificationHelper.showAlarmNotification(
            context = this,
            taskId = taskId,
            taskTitle = taskTitle,
            categoryName = categoryName,
            remark = remark
        )

        // 2. Play Alarm Audio via AlarmSoundPlayer
        AlarmSoundPlayer.play(this)

        // 3. Promote to foreground service safely with mediaPlayback type
        val notification = NotificationCompat.Builder(this, AlarmNotificationHelper.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("⏰ Alarm Tugas: $taskTitle")
            .setContentText("Kategori: $categoryName")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .build()

        val notificationId = (if (taskId > 0) taskId else System.currentTimeMillis()).toInt().coerceAtLeast(1)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    notificationId,
                    notification,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                    else 0
                )
            } else {
                startForeground(notificationId, notification)
            }
        } catch (_: Exception) {
            try {
                startForeground(notificationId, notification)
            } catch (_: Exception) {}
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        AlarmSoundPlayer.stop(this)
    }

    companion object {
        const val ACTION_STOP_ALARM = "com.example.reminder.ACTION_STOP_ALARM"

        fun stop(context: Context) {
            try {
                AlarmSoundPlayer.stop(context)
                val intent = Intent(context, ReminderService::class.java).apply {
                    action = ACTION_STOP_ALARM
                }
                context.startService(intent)
                context.stopService(Intent(context, ReminderService::class.java))
            } catch (_: Exception) {}
        }
    }
}
