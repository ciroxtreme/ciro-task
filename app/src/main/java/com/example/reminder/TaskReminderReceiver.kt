package com.example.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.util.AlarmLogger

class TaskReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
        val taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE) ?: "Pengingat Tugas!"
        val categoryName = intent.getStringExtra(EXTRA_CATEGORY_NAME) ?: "Tugas"
        val remark = intent.getStringExtra(EXTRA_REMARK) ?: ""

        AlarmLogger.log(context, "⚡ [BroadcastReceiver] Alarm terpicu dari sistem! ID: $taskId, Judul: $taskTitle")

        val serviceIntent = Intent(context, ReminderService::class.java).apply {
            putExtra(EXTRA_TASK_ID, taskId)
            putExtra(TaskReminderReceiver.EXTRA_TASK_TITLE, taskTitle)
            putExtra(TaskReminderReceiver.EXTRA_CATEGORY_NAME, categoryName)
            putExtra(TaskReminderReceiver.EXTRA_REMARK, remark)
        }

        try {
            ContextCompat.startForegroundService(context, serviceIntent)
            AlarmLogger.log(context, "🚀 [ForegroundService] Berhasil meluncurkan ReminderService!")
        } catch (e: Exception) {
            AlarmLogger.log(context, "❌ [Gagal Start Service] Error: ${e.message}")
            e.printStackTrace()
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
