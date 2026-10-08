package com.example.reminder

import android.content.Intent
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
        val taskId = data["taskId"]?.toLongOrNull() ?: (System.currentTimeMillis() % 100000)

        AlarmLogger.log(this, "🔥 [Google FCM Push] Diterima dari server Google Cloud! Task: $title")

        // 1. Play sound & vibration
        AlarmSoundPlayer.play(this)

        // 2. Directly show Heads-Up & Full-Screen Notification
        AlarmNotificationHelper.showAlarmNotification(
            context = this,
            taskId = taskId,
            taskTitle = title,
            categoryName = category,
            remark = remark
        )

        // 3. Try to start popup directly
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
        } catch (_: Exception) {}
    }
}
