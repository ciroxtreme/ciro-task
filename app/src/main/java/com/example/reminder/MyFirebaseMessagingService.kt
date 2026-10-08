package com.example.reminder

import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.util.AlarmLogger
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        AlarmLogger.log(this, "🔑 [Google FCM Token] $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val data = remoteMessage.data
        val title = data["title"] ?: remoteMessage.notification?.title ?: "Pengingat Tugas Google Push"
        val category = data["category"] ?: "Tugas"
        val remark = data["remark"] ?: remoteMessage.notification?.body ?: ""
        val taskId = data["taskId"]?.toLongOrNull() ?: System.currentTimeMillis()

        AlarmLogger.log(this, "🔥 [Google FCM Push] Diterima dari server Google Cloud! Task: $title")

        // Trigger Foreground ReminderService (works 100% when app closed!)
        val serviceIntent = Intent(this, ReminderService::class.java).apply {
            putExtra(TaskReminderReceiver.EXTRA_TASK_ID, taskId)
            putExtra(TaskReminderReceiver.EXTRA_TASK_TITLE, title)
            putExtra(TaskReminderReceiver.EXTRA_CATEGORY_NAME, category)
            putExtra(TaskReminderReceiver.EXTRA_REMARK, remark)
        }

        try {
            ContextCompat.startForegroundService(this, serviceIntent)
        } catch (e: Exception) {
            AlarmLogger.log(this, "❌ [FCM Error] Gagal memicu service: ${e.message}")
            e.printStackTrace()
        }
    }
}
