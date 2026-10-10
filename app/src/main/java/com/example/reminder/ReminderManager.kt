package com.example.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import com.example.MainActivity
import com.example.data.model.TaskEntity
import com.example.util.AlarmLogger
import com.example.data.db.AppDatabase
import com.example.data.repository.TaskRepository
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AlarmDiagnostics {
    fun logEvent(context: Context, eventName: String, details: Map<String, Any?> = emptyMap()) {
        val detailsFormatted = if (details.isEmpty()) "" else details.entries.joinToString(" | ") { "${it.key}=${it.value}" }
        val message = "📊 [$eventName] $detailsFormatted"
        AlarmLogger.log(context, message)
    }
}

object ReminderManager {

    fun canScheduleExactAlarms(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            alarmManager?.canScheduleExactAlarms() ?: true
        } else {
            true
        }
    }

    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            pm?.isIgnoringBatteryOptimizations(context.packageName) ?: true
        } else {
            true
        }
    }

    fun canDrawOverlays(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    fun verifyAlarmExists(context: Context, taskId: Long): Boolean {
        val intent = Intent(context, TaskReminderReceiver::class.java).apply {
            action = TaskReminderReceiver.ACTION_TASK_ALARM
        }
        val requestCode = taskId.toInt().coerceAtLeast(1)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )
        val exists = pendingIntent != null
        AlarmDiagnostics.logEvent(
            context,
            "VERIFY_ALARM_IN_SYSTEM",
            mapOf("taskId" to taskId, "requestCode" to requestCode, "registeredInOS" to exists)
        )
        return exists
    }

    fun openExactAlarmSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                return
            } catch (_: Exception) {
                try {
                    // Fallback to the general Alarms & Reminders screen if package URI is rejected by OEM
                    val fallbackIntent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(fallbackIntent)
                    return
                } catch (_: Exception) {}
            }
        }
        openAppDetailsSettings(context)
    }

    suspend fun rescheduleAllReminders(context: Context) {
        try {
            val db = AppDatabase.getDatabase(context)
            val repository = TaskRepository(db.taskDao(), db.categoryDao())
            val tasks = repository.allTasks.firstOrNull() ?: emptyList()
            val categories = repository.allCategories.firstOrNull() ?: emptyList()
            val categoryMap = categories.associate { it.id to it.name }

            val now = System.currentTimeMillis()
            var count = 0
            for (task in tasks) {
                if (task.hasReminder && !task.isCompleted && task.dueTimestamp > now) {
                    val catName = categoryMap[task.categoryId] ?: "Tugas"
                    scheduleTaskReminder(context, task, catName)
                    count++
                }
            }
            AlarmDiagnostics.logEvent(context, "RESCHEDULE_ALL_SUCCESS", mapOf("count" to count))
        } catch (e: Exception) {
            AlarmDiagnostics.logEvent(context, "RESCHEDULE_ALL_FAILED", mapOf("error" to e.message))
        }
    }

    fun openBatteryOptimizationSettings(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            }
        } catch (_: Exception) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                }
            } catch (_: Exception) {
                openAppDetailsSettings(context)
            }
        }
    }

    fun openAutoStartSettings(context: Context) {
        val manufacturer = Build.MANUFACTURER.lowercase()
        val intents = mutableListOf<Intent>()

        when {
            manufacturer.contains("xiaomi") || manufacturer.contains("redmi") || manufacturer.contains("poco") -> {
                intents.add(Intent().setComponent(ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")))
            }
            manufacturer.contains("transsion") || manufacturer.contains("infinix") || manufacturer.contains("tecno") || manufacturer.contains("itel") -> {
                intents.add(Intent().setComponent(ComponentName("com.transsion.phonemaster", "com.cyin.himgr.autostart.AutoStartActivity")))
                intents.add(Intent().setComponent(ComponentName("com.transsion.phonemaster", "com.cyin.himgr.applicationmanager.view.activities.LockScreenCleanupActivity")))
                intents.add(Intent().setComponent(ComponentName("com.transsion.phonemaster", "com.cyin.himgr.powermanager.views.activity.PowerManagerActivity")))
                intents.add(Intent().setComponent(ComponentName("com.transsion.phonemaster", "com.cyin.himgr.widget.activity.MainActivity")))
                intents.add(Intent().setComponent(ComponentName("com.transsion.phonemaster", "com.transsion.phonemaster.startupmanager.StartupManagerActivity")))
                intents.add(Intent().setComponent(ComponentName("com.transsion.phonemaster", "com.transsion.phonemaster.MainActivity")))
                intents.add(Intent().setComponent(ComponentName("com.transsion.phonemaster", "com.transsion.phonemaster.autostart.AutoStartActivity")))
            }
            manufacturer.contains("oppo") || manufacturer.contains("realme") -> {
                intents.add(Intent().setComponent(ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")))
            }
            manufacturer.contains("vivo") || manufacturer.contains("iqoo") -> {
                intents.add(Intent().setComponent(ComponentName("com.iqoo.secure", "com.iqoo.secure.safeguard.PurviewTabActivity")))
            }
            manufacturer.contains("huawei") || manufacturer.contains("honor") -> {
                intents.add(Intent().setComponent(ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity")))
            }
            manufacturer.contains("samsung") -> {
                intents.add(Intent().setComponent(ComponentName("com.samsung.android.lool", "com.samsung.android.sm.battery.ui.BatteryActivity")))
            }
        }
        intents.add(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
        })

        for (intent in intents) {
            try {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                return
            } catch (_: Exception) {}
        }
    }

    fun openOverlaySettings(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            }
        } catch (_: Exception) {
            openAppDetailsSettings(context)
        }
    }

    fun openAppDetailsSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun scheduleTaskReminder(context: Context, task: TaskEntity, categoryName: String) {
        if (!task.hasReminder || task.isCompleted) return

        AlarmNotificationHelper.createNotificationChannel(context)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        
        val intent = Intent(context, TaskReminderReceiver::class.java).apply {
            action = TaskReminderReceiver.ACTION_TASK_ALARM
            addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
            addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
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

        // Don't schedule past reminders, but give a 60s tolerance for quick-test scheduling
        if (triggerTime <= now) {
            if (now - triggerTime < 60_000L) {
                triggerTime = now + 10_000L
            } else {
                return
            }
        }

        val formattedTime = SimpleDateFormat("HH:mm:ss dd/MM", Locale.getDefault()).format(Date(triggerTime))
        AlarmDiagnostics.logEvent(
            context,
            "SCHEDULE_TASK_ALARM",
            mapOf(
                "taskId" to task.id,
                "title" to task.title,
                "triggerTime" to formattedTime,
                "canExact" to canScheduleExactAlarms(context),
                "isIgnoreBattery" to isIgnoringBatteryOptimizations(context)
            )
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
                AlarmDiagnostics.logEvent(context, "SET_AND_ALLOW_WHILE_IDLE_FALLBACK", mapOf("reason" to "exact_alarm_permission_disabled"))
                verifyAlarmExists(context, task.id)
                return
            }

            // Priority 1: setAlarmClock (bypasses Doze mode and guarantees exact second firing even when app is closed)
            val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerTime, showPendingIntent)
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            AlarmDiagnostics.logEvent(context, "SET_ALARM_CLOCK_SUCCESS", mapOf("taskId" to task.id))
        } catch (e: Exception) {
            AlarmDiagnostics.logEvent(context, "SET_ALARM_CLOCK_FAILED", mapOf("error" to e.message))
            try {
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
            } catch (ex: Exception) {
                AlarmDiagnostics.logEvent(context, "SET_EXACT_FALLBACK_FAILED", mapOf("error" to ex.message))
            }
        }

        // Verify alarm presence in system
        verifyAlarmExists(context, task.id)
    }

    fun cancelTaskReminder(context: Context, taskId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, TaskReminderReceiver::class.java).apply {
            action = TaskReminderReceiver.ACTION_TASK_ALARM
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
            AlarmDiagnostics.logEvent(context, "CANCEL_ALARM_SUCCESS", mapOf("taskId" to taskId))
        }
        AlarmNotificationHelper.cancelAlarmNotification(context, taskId)
        AlarmSoundPlayer.stop(context)
    }

    fun triggerImmediateTestReminder(context: Context, title: String = "Meeting with friends", category: String = "Family") {
        AlarmDiagnostics.logEvent(context, "TEST_ALARM_TRIGGERED_DIRECTLY", mapOf("title" to title))
        val intent = Intent(context, TaskReminderReceiver::class.java).apply {
            action = TaskReminderReceiver.ACTION_TASK_ALARM
            putExtra(TaskReminderReceiver.EXTRA_TASK_ID, 999999L)
            putExtra(TaskReminderReceiver.EXTRA_TASK_TITLE, title)
            putExtra(TaskReminderReceiver.EXTRA_CATEGORY_NAME, category)
            putExtra(TaskReminderReceiver.EXTRA_REMARK, "Ini contoh Alarm & Notifikasi aktif Ciro Task!")
        }
        context.sendBroadcast(intent)
    }
}
