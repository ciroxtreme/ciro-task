package com.example.reminder

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.util.AlarmLogger

object AlarmNotificationHelper {
    const val CHANNEL_ID = "ciro_task_alarm_silent_channel_v5"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channel = NotificationChannel(
                CHANNEL_ID,
                "Ciro Task Alarm Ringtone",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alarm & notifikasi pengingat tugas Ciro Task (Tembus DND)"
                // Tanpa suara bawaan agar MediaPlayer di AlarmSoundPlayer menjadi satu-satunya sumber audio
                setSound(null, null)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 800, 400, 800, 400)
                enableLights(true)
                setShowBadge(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                setBypassDnd(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun buildAlarmNotification(
        context: Context,
        taskId: Long,
        taskTitle: String,
        categoryName: String,
        remark: String
    ): Notification {
        createNotificationChannel(context)
        val notifId = if (taskId > 0) taskId.toInt().coerceAtLeast(1) else (System.currentTimeMillis() % 100000).toInt()

        // Intent to open MainActivity when notification body is tapped
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            this.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(TaskReminderReceiver.EXTRA_TASK_ID, taskId)
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            notifId,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        // Full-screen intent directed to ReminderAlertActivity
        val alertActivityIntent = Intent(context, ReminderAlertActivity::class.java).apply {
            this.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(TaskReminderReceiver.EXTRA_TASK_ID, taskId)
            putExtra(TaskReminderReceiver.EXTRA_TASK_TITLE, taskTitle)
            putExtra(TaskReminderReceiver.EXTRA_CATEGORY_NAME, categoryName)
            putExtra(TaskReminderReceiver.EXTRA_REMARK, remark)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            notifId + 500000,
            alertActivityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        // Action: Selesai
        val completeIntent = Intent(context, TaskReminderReceiver::class.java).apply {
            this.action = TaskReminderReceiver.ACTION_COMPLETE_TASK
            putExtra(TaskReminderReceiver.EXTRA_TASK_ID, taskId)
            putExtra(TaskReminderReceiver.EXTRA_NOTIFICATION_ID, notifId)
        }
        val completePendingIntent = PendingIntent.getBroadcast(
            context,
            notifId + 100000,
            completeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        // Action: Tunda 5 Menit
        val snoozeIntent = Intent(context, TaskReminderReceiver::class.java).apply {
            this.action = TaskReminderReceiver.ACTION_SNOOZE_TASK
            putExtra(TaskReminderReceiver.EXTRA_TASK_ID, taskId)
            putExtra(TaskReminderReceiver.EXTRA_TASK_TITLE, taskTitle)
            putExtra(TaskReminderReceiver.EXTRA_CATEGORY_NAME, categoryName)
            putExtra(TaskReminderReceiver.EXTRA_REMARK, remark)
            putExtra(TaskReminderReceiver.EXTRA_NOTIFICATION_ID, notifId)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            notifId + 200000,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        // Action: Matikan Alarm
        val dismissIntent = Intent(context, TaskReminderReceiver::class.java).apply {
            this.action = TaskReminderReceiver.ACTION_DISMISS_ALARM
            putExtra(TaskReminderReceiver.EXTRA_NOTIFICATION_ID, notifId)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            notifId + 300000,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("⏰ Alarm Tugas: $taskTitle")
            .setContentText("Kategori: $categoryName ${if (remark.isNotBlank()) "• $remark" else ""}")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setOngoing(true)
            .setContentIntent(openAppPendingIntent)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .addAction(0, "✓ Selesai", completePendingIntent)
            .addAction(0, "⏰ Tunda 5 Mnt", snoozePendingIntent)
            .addAction(0, "✕ Matikan", dismissPendingIntent)

        return notificationBuilder.build()
    }

    fun showAlarmNotification(
        context: Context,
        taskId: Long,
        taskTitle: String,
        categoryName: String,
        remark: String
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notifId = if (taskId > 0) taskId.toInt().coerceAtLeast(1) else (System.currentTimeMillis() % 100000).toInt()
        val notification = buildAlarmNotification(context, taskId, taskTitle, categoryName, remark)
        try {
            notificationManager.notify(notifId, notification)
            AlarmLogger.log(context, "🔔 [Notification] Berhasil memunculkan notifikasi alarm (ID: $notifId)!")
        } catch (e: Exception) {
            AlarmLogger.log(context, "❌ [Notification] Gagal memunculkan notifikasi: ${e.message}")
            e.printStackTrace()
        }
    }

    fun cancelAlarmNotification(context: Context, taskId: Long) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notifId = if (taskId > 0) taskId.toInt().coerceAtLeast(1) else (System.currentTimeMillis() % 100000).toInt()
            notificationManager.cancel(notifId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancelNotificationById(context: Context, notifId: Int) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(notifId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
