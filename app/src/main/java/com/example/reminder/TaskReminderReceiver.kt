package com.example.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class TaskReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
        val taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE) ?: "Pengingat Tugas!"
        val categoryName = intent.getStringExtra(EXTRA_CATEGORY_NAME) ?: "Tugas"
        val remark = intent.getStringExtra(EXTRA_REMARK) ?: ""

        // Delegate to Foreground Service (bypasses Android background & process killed restrictions)
        val serviceIntent = Intent(context, ReminderService::class.java).apply {
            putExtra(EXTRA_TASK_ID, taskId)
            putExtra(EXTRA_TASK_TITLE, taskTitle)
            putExtra(EXTRA_CATEGORY_NAME, categoryName)
            putExtra(EXTRA_REMARK, remark)
        }

        try {
            ContextCompat.startForegroundService(context, serviceIntent)
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback for edge cases
            ReminderService.stop(context)
        }
    }

    companion object {
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_TASK_TITLE = "extra_task_title"
        const val EXTRA_CATEGORY_NAME = "extra_category_name"
        const val EXTRA_REMARK = "extra_remark"
    }
}
