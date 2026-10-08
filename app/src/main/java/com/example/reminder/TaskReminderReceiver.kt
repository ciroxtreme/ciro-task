package com.example.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import com.example.data.db.AppDatabase
import com.example.data.repository.TaskRepository
import com.example.util.AlarmLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TaskReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: ACTION_TASK_ALARM
        val notifId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)

        when (action) {
            ACTION_COMPLETE_TASK -> {
                AlarmLogger.log(context, "✅ [Aksi Notifikasi] Tombol Selesai diklik untuk Task ID: $taskId")
                AlarmSoundPlayer.stop(context)
                if (notifId > 0) {
                    AlarmNotificationHelper.cancelNotificationById(context, notifId)
                } else if (taskId > 0) {
                    AlarmNotificationHelper.cancelAlarmNotification(context, taskId)
                }

                if (taskId > 0) {
                    val pendingResult = goAsync()
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val db = AppDatabase.getDatabase(context)
                            val repo = TaskRepository(db.taskDao(), db.categoryDao())
                            val task = repo.getTaskById(taskId)
                            if (task != null) {
                                repo.updateTask(task.copy(isCompleted = true, completedAt = System.currentTimeMillis()))
                                AlarmLogger.log(context, "✅ Task $taskId berhasil ditandai selesai!")
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        } finally {
                            pendingResult.finish()
                        }
                    }
                }
            }

            ACTION_SNOOZE_TASK -> {
                val taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE) ?: "Tugas"
                val categoryName = intent.getStringExtra(EXTRA_CATEGORY_NAME) ?: "Tugas"
                AlarmLogger.log(context, "⏰ [Aksi Notifikasi] Tombol Tunda 5 Menit diklik untuk Task ID: $taskId")
                AlarmSoundPlayer.stop(context)
                if (notifId > 0) {
                    AlarmNotificationHelper.cancelNotificationById(context, notifId)
                } else if (taskId > 0) {
                    AlarmNotificationHelper.cancelAlarmNotification(context, taskId)
                }

                if (taskId > 0) {
                    val pendingResult = goAsync()
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val db = AppDatabase.getDatabase(context)
                            val repo = TaskRepository(db.taskDao(), db.categoryDao())
                            val task = repo.getTaskById(taskId)
                            if (task != null) {
                                val newTime = System.currentTimeMillis() + (5 * 60 * 1000)
                                val updated = task.copy(dueTimestamp = newTime)
                                repo.updateTask(updated)
                                ReminderManager.scheduleTaskReminder(context, updated, categoryName)
                                AlarmLogger.log(context, "⏰ Task $taskId ditunda 5 menit!")
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        } finally {
                            pendingResult.finish()
                        }
                    }
                }
            }

            ACTION_DISMISS_ALARM -> {
                AlarmLogger.log(context, "✕ [Aksi Notifikasi] Tombol Matikan Alarm diklik.")
                AlarmSoundPlayer.stop(context)
                if (notifId > 0) {
                    AlarmNotificationHelper.cancelNotificationById(context, notifId)
                } else if (taskId > 0) {
                    AlarmNotificationHelper.cancelAlarmNotification(context, taskId)
                }
            }

            ACTION_TASK_ALARM -> {
                val taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE) ?: "Pengingat Tugas!"
                val categoryName = intent.getStringExtra(EXTRA_CATEGORY_NAME) ?: "Tugas"
                val remark = intent.getStringExtra(EXTRA_REMARK) ?: ""

                AlarmLogger.log(context, "⚡ [BroadcastReceiver] Alarm RTC terpicu dari sistem! ID: $taskId, Judul: $taskTitle")

                // 1. Acquire WakeLock
                var wakeLock: PowerManager.WakeLock? = null
                try {
                    val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
                    @Suppress("DEPRECATION")
                    wakeLock = pm?.newWakeLock(
                        PowerManager.FULL_WAKE_LOCK or
                                PowerManager.ACQUIRE_CAUSES_WAKEUP or
                                PowerManager.ON_AFTER_RELEASE,
                        "ciro:task_alarm_receiver_wakelock"
                    )
                    wakeLock?.acquire(15000L) // 15s wake lock
                } catch (_: Exception) {}

                // 2. Play Audio & Vibration
                AlarmSoundPlayer.play(context)

                // 3. Directly show Heads-Up & Full-Screen Notification (Never blocked by background limits)
                AlarmNotificationHelper.showAlarmNotification(
                    context = context,
                    taskId = taskId,
                    taskTitle = taskTitle,
                    categoryName = categoryName,
                    remark = remark
                )

                // 4. Try starting activity directly (if permitted / screen on)
                try {
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
                    AlarmLogger.log(context, "🚀 [Activity] Berhasil meluncurkan ReminderAlertActivity langsung!")
                } catch (e: Exception) {
                    AlarmLogger.log(context, "ℹ️ Activity langsung tidak dapat dibuka (ditangani oleh FullScreenIntent): ${e.message}")
                }
            }
        }
    }

    companion object {
        const val ACTION_TASK_ALARM = "com.example.reminder.ACTION_TASK_ALARM"
        const val ACTION_COMPLETE_TASK = "com.example.reminder.ACTION_COMPLETE_TASK"
        const val ACTION_SNOOZE_TASK = "com.example.reminder.ACTION_SNOOZE_TASK"
        const val ACTION_DISMISS_ALARM = "com.example.reminder.ACTION_DISMISS_ALARM"

        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_TASK_TITLE = "extra_task_title"
        const val EXTRA_CATEGORY_NAME = "extra_category_name"
        const val EXTRA_REMARK = "extra_remark"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    }
}
