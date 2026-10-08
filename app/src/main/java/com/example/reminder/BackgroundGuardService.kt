package com.example.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.db.AppDatabase
import com.example.data.repository.TaskRepository
import com.example.util.AlarmLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class BackgroundGuardService : Service() {

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private val triggeredTaskIds = mutableSetOf<Long>()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        AlarmLogger.log(this, "🛡️ BackgroundGuardService dimulai!")
        val notification = createGuardNotification()
        startForeground(GUARD_NOTIFICATION_ID, notification)
        startMonitoringLoop()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_STOP_GUARD) {
            AlarmLogger.log(this, "🛡️ BackgroundGuardService dihentikan pengguna.")
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    private fun startMonitoringLoop() {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = TaskRepository(database.taskDao(), database.categoryDao())

        serviceScope.launch {
            while (isActive) {
                try {
                    val now = System.currentTimeMillis()
                    val tasks = repository.getUncompletedTasksWithReminders()
                    val categories = database.categoryDao().getAllCategoriesList()
                    val categoryMap = categories.associate { it.id to it.name }

                    for (task in tasks) {
                        // If due within 2 seconds or already reached, and hasn't been triggered in this guard loop
                        if (task.dueTimestamp <= now + 2000 && !triggeredTaskIds.contains(task.id)) {
                            triggeredTaskIds.add(task.id)
                            val catName = categoryMap[task.categoryId] ?: "Tugas"
                            AlarmLogger.log(
                                applicationContext,
                                "🛡️ [Penjaga Background] Pemicu Backup Mengabaikan Pembatasan OS! Task: ${task.title}"
                            )

                            // Launch ReminderService directly
                            val serviceIntent = Intent(applicationContext, ReminderService::class.java).apply {
                                putExtra(TaskReminderReceiver.EXTRA_TASK_ID, task.id)
                                putExtra(TaskReminderReceiver.EXTRA_TASK_TITLE, task.title)
                                putExtra(TaskReminderReceiver.EXTRA_CATEGORY_NAME, catName)
                                putExtra(TaskReminderReceiver.EXTRA_REMARK, task.remark)
                            }
                            androidx.core.content.ContextCompat.startForegroundService(applicationContext, serviceIntent)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(5000L) // Check every 5 seconds
            }
        }
    }

    private fun createGuardNotification(): android.app.Notification {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                GUARD_CHANNEL_ID,
                "Ciro Task Background Guard",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Menjaga alarm tetap aktif di HP Infinix/Xiaomi saat aplikasi ditutup"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openIntent = Intent(this, MainActivity::class.java)
        val openPendingIntent = PendingIntent.getActivity(
            this,
            88888,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        return NotificationCompat.Builder(this, GUARD_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("🛡️ Ciro Task Penjaga Alarm Aktif")
            .setContentText("Alarm dijamin 100% berbunyi tepat waktu saat aplikasi ditutup")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(openPendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }

    companion object {
        private const val GUARD_NOTIFICATION_ID = 777111
        const val GUARD_CHANNEL_ID = "ciro_task_guard_channel"
        const val ACTION_STOP_GUARD = "com.example.reminder.ACTION_STOP_GUARD"

        fun start(context: Context) {
            try {
                val intent = Intent(context, BackgroundGuardService::class.java)
                androidx.core.content.ContextCompat.startForegroundService(context, intent)
            } catch (_: Exception) {}
        }

        fun stop(context: Context) {
            try {
                val intent = Intent(context, BackgroundGuardService::class.java).apply {
                    action = ACTION_STOP_GUARD
                }
                context.startService(intent)
            } catch (_: Exception) {}
        }
    }
}
