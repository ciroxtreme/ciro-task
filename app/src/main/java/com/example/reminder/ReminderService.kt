package com.example.reminder

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import com.example.util.AlarmLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ReminderService : Service() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private var timeoutJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_STOP_ALARM) {
            AlarmSoundPlayer.stop(this)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } else {
                @Suppress("DEPRECATION")
                stopForeground(true)
            }
            stopSelf()
            return START_NOT_STICKY
        }

        val taskId = intent?.getLongExtra(TaskReminderReceiver.EXTRA_TASK_ID, -1L) ?: -1L
        val taskTitle = intent?.getStringExtra(TaskReminderReceiver.EXTRA_TASK_TITLE) ?: "Pengingat Tugas!"
        val categoryName = intent?.getStringExtra(TaskReminderReceiver.EXTRA_CATEGORY_NAME) ?: "Tugas"
        val remark = intent?.getStringExtra(TaskReminderReceiver.EXTRA_REMARK) ?: ""

        AlarmLogger.log(this, "🚀 [ReminderService] Dipanggil untuk Task: $taskTitle")

        // 1. Build Foreground notification with fullScreenIntent pointing to ReminderAlertActivity
        val notification = AlarmNotificationHelper.buildAlarmNotification(
            context = this,
            taskId = taskId,
            taskTitle = taskTitle,
            categoryName = categoryName,
            remark = remark
        )

        val notificationId = (if (taskId > 0) taskId else System.currentTimeMillis()).toInt().coerceAtLeast(1)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    notificationId,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                )
            } else {
                startForeground(notificationId, notification)
            }
        } catch (e: Exception) {
            AlarmLogger.log(this, "⚠️ Gagal startForeground dengan mediaPlayback: ${e.message}")
            try {
                startForeground(notificationId, notification)
            } catch (_: Exception) {}
        }

        // Release handover static WakeLock from TaskReminderReceiver
        TaskReminderReceiver.releaseWakeLock()

        // 2. Play Alarm Audio via AlarmSoundPlayer
        AlarmSoundPlayer.play(this)

        // 3. Launch popup activity directly if possible (unlocked screen / direct assist)
        try {
            val alertIntent = Intent(this, ReminderAlertActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra(TaskReminderReceiver.EXTRA_TASK_ID, taskId)
                putExtra(TaskReminderReceiver.EXTRA_TASK_TITLE, taskTitle)
                putExtra(TaskReminderReceiver.EXTRA_CATEGORY_NAME, categoryName)
                putExtra(TaskReminderReceiver.EXTRA_REMARK, remark)
            }
            startActivity(alertIntent)
        } catch (_: Exception) {}

        // 4. Setup auto-timeout (5 minutes) so alarm stops automatically to prevent battery drain
        timeoutJob?.cancel()
        timeoutJob = serviceScope.launch {
            delay(5 * 60 * 1000L)
            AlarmLogger.log(this@ReminderService, "⏱️ Alarm timeout 5 menit tercapai, otomatis menghentikan alarm.")
            stop(this@ReminderService)
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        timeoutJob?.cancel()
        serviceJob.cancel()
        AlarmSoundPlayer.stop(this)
        TaskReminderReceiver.releaseWakeLock()
    }

    companion object {
        const val ACTION_STOP_ALARM = "com.example.reminder.ACTION_STOP_ALARM"

        fun stop(context: Context) {
            try {
                AlarmSoundPlayer.stop(context)
                try {
                    val intent = Intent(context, ReminderService::class.java).apply {
                        action = ACTION_STOP_ALARM
                    }
                    context.startService(intent)
                } catch (_: Exception) {}
                context.stopService(Intent(context, ReminderService::class.java))
            } catch (_: Exception) {}
        }
    }
}
