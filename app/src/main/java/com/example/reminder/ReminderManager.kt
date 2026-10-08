package com.example.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.MainActivity
import com.example.data.model.TaskEntity

object ReminderManager {

    fun scheduleTaskReminder(context: Context, task: TaskEntity, categoryName: String) {
        if (!task.hasReminder || task.isCompleted) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, TaskReminderReceiver::class.java).apply {
            action = "com.example.reminder.ACTION_TASK_${task.id}"
            putExtra(TaskReminderReceiver.EXTRA_TASK_ID, task.id)
            putExtra(TaskReminderReceiver.EXTRA_TASK_TITLE, task.title)
            putExtra(TaskReminderReceiver.EXTRA_CATEGORY_NAME, categoryName)
            putExtra(TaskReminderReceiver.EXTRA_REMARK, task.remark)
        }

        val requestCode = task.id.toInt().coerceAtLeast(1)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val showIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(TaskReminderReceiver.EXTRA_TASK_ID, task.id)
        }
        val showPendingIntent = PendingIntent.getActivity(
            context,
            requestCode,
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val now = System.currentTimeMillis()
        var triggerTime = task.dueTimestamp

        // If time was set for the current minute or just passed within the last 2 minutes, trigger in 1.5 seconds!
        if (triggerTime <= now) {
            if (triggerTime >= now - 120_000) {
                triggerTime = now + 1500
            } else {
                return // Old past task, don't alert
            }
        }

        try {
            // Priority 1: setAlarmClock (bypasses Doze mode and guarantees exact second firing even when app is closed)
            val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerTime, showPendingIntent)
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
        } catch (_: Exception) {
            try {
                // Priority 2: setExactAndAllowWhileIdle
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            } catch (_: Exception) {
                try {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                } catch (_: Exception) {}
            }
        }
    }

    fun cancelTaskReminder(context: Context, taskId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, TaskReminderReceiver::class.java).apply {
            action = "com.example.reminder.ACTION_TASK_$taskId"
        }
        val requestCode = taskId.toInt().coerceAtLeast(1)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    fun triggerImmediateTestReminder(context: Context, title: String = "Meeting with friends", category: String = "Family") {
        // Direct launch single popup dialog activity
        try {
            val popupIntent = Intent(context, ReminderAlertActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra(TaskReminderReceiver.EXTRA_TASK_ID, 999999L)
                putExtra(TaskReminderReceiver.EXTRA_TASK_TITLE, title)
                putExtra(TaskReminderReceiver.EXTRA_CATEGORY_NAME, category)
                putExtra(TaskReminderReceiver.EXTRA_REMARK, "Ini contoh Pop-up Reminder aktif Ciro Task!")
            }
            context.startActivity(popupIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Show Heads-Up Notification
        TaskReminderReceiver.showNotification(
            context = context,
            taskId = 999999L,
            title = title,
            category = category,
            remark = "Ini contoh Pop-up Reminder aktif Ciro Task!"
        )
    }
}
