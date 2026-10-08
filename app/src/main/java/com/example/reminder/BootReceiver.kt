package com.example.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.db.AppDatabase
import com.example.data.repository.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON" ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(context)
                    val repository = TaskRepository(db.taskDao(), db.categoryDao())
                    val tasks = repository.allTasks.firstOrNull() ?: emptyList()
                    val categories = repository.allCategories.firstOrNull() ?: emptyList()
                    val categoryMap = categories.associate { it.id to it.name }

                    val now = System.currentTimeMillis()
                    for (task in tasks) {
                        if (task.hasReminder && !task.isCompleted && task.dueTimestamp > now) {
                            val catName = categoryMap[task.categoryId] ?: "Tugas"
                            ReminderManager.scheduleTaskReminder(context, task, catName)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
