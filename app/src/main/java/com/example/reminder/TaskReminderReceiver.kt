package com.example.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.R

class TaskReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
        val taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE) ?: "Pengingat Tugas!"
        val categoryName = intent.getStringExtra(EXTRA_CATEGORY_NAME) ?: "Tugas"
        val remark = intent.getStringExtra(EXTRA_REMARK) ?: ""

        // 1. Acquire WakeLock to turn screen on and wake CPU
        var wakeLock: PowerManager.WakeLock? = null
        try {
            val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            @Suppress("DEPRECATION")
            wakeLock = pm?.newWakeLock(
                PowerManager.FULL_WAKE_LOCK or
                        PowerManager.ACQUIRE_CAUSES_WAKEUP or
                        PowerManager.ON_AFTER_RELEASE,
                "ciro:reminder_wakelock"
            )
            wakeLock?.acquire(15000L) // 15s wake lock
        } catch (_: Exception) {
            try {
                val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
                wakeLock = pm?.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                    "ciro:reminder_wakelock"
                )
                wakeLock?.acquire(15000L)
            } catch (_: Exception) {}
        }

        // 2. Try launching popup activity directly
        val launchedDirectly = launchPopupActivity(context, taskId, taskTitle, categoryName, remark)

        // 3. Show high-priority heads-up notification with full-screen intent
        showNotification(context, taskId, taskTitle, categoryName, remark)
    }

    private fun launchPopupActivity(
        context: Context,
        taskId: Long,
        taskTitle: String,
        categoryName: String,
        remark: String
    ): Boolean {
        return try {
            val popupIntent = Intent(context, ReminderAlertActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra(EXTRA_TASK_ID, taskId)
                putExtra(EXTRA_TASK_TITLE, taskTitle)
                putExtra(EXTRA_CATEGORY_NAME, categoryName)
                putExtra(EXTRA_REMARK, remark)
            }
            context.startActivity(popupIntent)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    companion object {
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_TASK_TITLE = "extra_task_title"
        const val EXTRA_CATEGORY_NAME = "extra_category_name"
        const val EXTRA_REMARK = "extra_remark"
        const val CHANNEL_ID = "ciro_task_reminders_v5"

        fun showNotification(
            context: Context,
            taskId: Long,
            title: String,
            category: String,
            remark: String
        ) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Ciro Task Pop-up Alarms",
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

            // Fullscreen popup intent to launch ReminderAlertActivity
            val fullScreenIntent = Intent(context, ReminderAlertActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra(EXTRA_TASK_ID, taskId)
                putExtra(EXTRA_TASK_TITLE, title)
                putExtra(EXTRA_CATEGORY_NAME, category)
                putExtra(EXTRA_REMARK, remark)
            }
            val fullScreenPendingIntent = PendingIntent.getActivity(
                context,
                taskId.toInt().coerceAtLeast(1),
                fullScreenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
            )

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("⏰ Waktunya Tugas: $title")
                .setContentText("Kategori: $category ${if (remark.isNotBlank()) "• $remark" else ""}")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVibrate(longArrayOf(0, 500, 250, 500))
                .setAutoCancel(true)
                .setContentIntent(fullScreenPendingIntent)
                .setFullScreenIntent(fullScreenPendingIntent, true) // Launches pop-up even when app is closed
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

            val notificationId = (if (taskId > 0) taskId else System.currentTimeMillis()).toInt().coerceAtLeast(1)
            notificationManager.notify(notificationId, builder.build())
        }
    }
}
