package com.example.util

import android.content.Context
import com.example.reminder.TaskReminderReceiver
import com.google.firebase.messaging.FirebaseMessaging

object FcmHelper {
    fun fetchFcmToken(context: Context, onTokenReceived: (String) -> Unit) {
        try {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null) {
                    val token = task.result
                    AlarmLogger.log(context, "🔑 Google Push FCM Token: $token")
                    onTokenReceived(token)
                } else {
                    onTokenReceived("Gagal mengambil FCM Token: ${task.exception?.message}")
                }
            }
        } catch (e: Exception) {
            onTokenReceived("Error FCM: ${e.message}")
        }
    }
}
