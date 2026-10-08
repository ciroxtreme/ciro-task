package com.example.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import com.example.R

class ReminderService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_STOP_ALARM) {
            stopAlarm()
            stopSelf()
            return START_NOT_STICKY
        }

        val taskId = intent?.getLongExtra(TaskReminderReceiver.EXTRA_TASK_ID, -1L) ?: -1L
        val taskTitle = intent?.getStringExtra(TaskReminderReceiver.EXTRA_TASK_TITLE) ?: "Pengingat Tugas!"
        val categoryName = intent?.getStringExtra(TaskReminderReceiver.EXTRA_CATEGORY_NAME) ?: "Tugas"
        val remark = intent?.getStringExtra(TaskReminderReceiver.EXTRA_REMARK) ?: ""

        // 1. Acquire WakeLock to keep CPU & Screen awake
        acquireWakeLock()

        // 2. Start Foreground Notification (bypasses OS background restrictions)
        val notification = createForegroundNotification(taskId, taskTitle, categoryName, remark)
        val notificationId = (if (taskId > 0) taskId else System.currentTimeMillis()).toInt().coerceAtLeast(1)
        startForeground(notificationId, notification)

        // 3. Start Alarm Audio & Vibration
        startAlarmAudio()

        // 4. Launch FullScreen Popup Activity
        launchPopupActivity(taskId, taskTitle, categoryName, remark)

        return START_STICKY
    }

    private fun acquireWakeLock() {
        try {
            val pm = getSystemService(Context.POWER_SERVICE) as? PowerManager
            @Suppress("DEPRECATION")
            wakeLock = pm?.newWakeLock(
                PowerManager.FULL_WAKE_LOCK or
                        PowerManager.ACQUIRE_CAUSES_WAKEUP or
                        PowerManager.ON_AFTER_RELEASE,
                "ciro:reminder_service_wakelock"
            )
            wakeLock?.acquire(60000L) // 60s max wake lock
        } catch (_: Exception) {}
    }

    private fun createForegroundNotification(
        taskId: Long,
        title: String,
        category: String,
        remark: String
    ): android.app.Notification {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Ciro Task Foreground Alarm",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alarm & pop-up pengingat tugas Ciro Task"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 250, 500)
                enableLights(true)
                setShowBadge(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                setBypassDnd(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val popupIntent = Intent(this, ReminderAlertActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(TaskReminderReceiver.EXTRA_TASK_ID, taskId)
            putExtra(TaskReminderReceiver.EXTRA_TASK_TITLE, title)
            putExtra(TaskReminderReceiver.EXTRA_CATEGORY_NAME, category)
            putExtra(TaskReminderReceiver.EXTRA_REMARK, remark)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            taskId.toInt().coerceAtLeast(1),
            popupIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val stopIntent = Intent(this, ReminderService::class.java).apply {
            action = ACTION_STOP_ALARM
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            (taskId + 9999).toInt(),
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("⏰ Alarm Tugas: $title")
            .setContentText("Kategori: $category ${if (remark.isNotBlank()) "• $remark" else ""}")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)
            .setContentIntent(fullScreenPendingIntent)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .addAction(R.mipmap.ic_launcher, "Matikan Alarm", stopPendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    private fun startAlarmAudio() {
        if (mediaPlayer != null) return // Already playing
        try {
            val alertUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            mediaPlayer = MediaPlayer().apply {
                setDataSource(applicationContext, alertUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }

            vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    VibrationEffect.createWaveform(
                        longArrayOf(0, 500, 300, 500),
                        0
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 500, 300, 500), 0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun launchPopupActivity(taskId: Long, title: String, category: String, remark: String) {
        try {
            val popupIntent = Intent(this, ReminderAlertActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra(TaskReminderReceiver.EXTRA_TASK_ID, taskId)
                putExtra(TaskReminderReceiver.EXTRA_TASK_TITLE, title)
                putExtra(TaskReminderReceiver.EXTRA_CATEGORY_NAME, category)
                putExtra(TaskReminderReceiver.EXTRA_REMARK, remark)
            }
            startActivity(popupIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopAlarm() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            vibrator?.cancel()
            vibrator = null
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (_: Exception) {}
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAlarm()
    }

    companion object {
        const val CHANNEL_ID = "ciro_task_fg_alarm_v1"
        const val ACTION_STOP_ALARM = "com.example.reminder.ACTION_STOP_ALARM"

        fun stop(context: Context) {
            try {
                val intent = Intent(context, ReminderService::class.java).apply {
                    action = ACTION_STOP_ALARM
                }
                context.startService(intent)
            } catch (_: Exception) {}
        }
    }
}
