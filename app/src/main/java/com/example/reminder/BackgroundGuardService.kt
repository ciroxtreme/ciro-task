package com.example.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.util.AlarmLogger

class BackgroundGuardService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        AlarmLogger.log(this, "🛡️ BackgroundGuardService dimulai (mode hemat daya).")
        val notification = createGuardNotification()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    GUARD_NOTIFICATION_ID,
                    notification,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                    else 0
                )
            } else {
                startForeground(GUARD_NOTIFICATION_ID, notification)
            }
        } catch (_: Exception) {
            try {
                startForeground(GUARD_NOTIFICATION_ID, notification)
            } catch (_: Exception) {}
        }
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

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        AlarmLogger.log(this, "🛡️ Aplikasi di-swipe dari Recents, menjaga proses tetap hidup...")
    }

    private fun createGuardNotification(): android.app.Notification {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                GUARD_CHANNEL_ID,
                "Ciro Task Background Guard",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Menjaga proses alarm tetap aktif di HP Infinix/Xiaomi"
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
            .setContentText("Hardware RTC Alarm aktif & siap memicu tepat waktu")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(openPendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        AlarmLogger.log(this, "🛡️ BackgroundGuardService berhenti.")
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
                context.stopService(Intent(context, BackgroundGuardService::class.java))
            } catch (_: Exception) {}
        }
    }
}
