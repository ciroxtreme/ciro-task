package com.example.util

import android.content.Context
import android.net.Uri
import com.example.data.model.CategoryEntity
import com.example.data.model.TaskEntity
import com.example.data.repository.TaskRepository
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileWriter
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BackupManager {

    fun exportBackupJson(
        context: Context,
        tasks: List<TaskEntity>,
        categories: List<CategoryEntity>
    ): File? {
        return try {
            val root = JSONObject()
            root.put("version", 1)
            root.put("timestamp", System.currentTimeMillis())

            // Categories
            val catsArray = JSONArray()
            for (c in categories) {
                val catObj = JSONObject()
                catObj.put("id", c.id)
                catObj.put("name", c.name)
                catObj.put("iconType", c.iconType)
                catObj.put("colorHex", c.colorHex)
                catObj.put("orderIndex", c.orderIndex)
                catsArray.put(catObj)
            }
            root.put("categories", catsArray)

            // Tasks
            val tasksArray = JSONArray()
            for (t in tasks) {
                val tObj = JSONObject()
                tObj.put("id", t.id)
                tObj.put("title", t.title)
                tObj.put("remark", t.remark)
                tObj.put("categoryId", t.categoryId)
                tObj.put("priority", t.priority)
                tObj.put("tags", t.tags)
                tObj.put("isCompleted", t.isCompleted)
                tObj.put("completedAt", t.completedAt ?: 0L)
                tObj.put("dueTimestamp", t.dueTimestamp)
                tObj.put("isRecurring", t.isRecurring)
                tObj.put("recurringType", t.recurringType)
                tObj.put("hasReminder", t.hasReminder)
                tObj.put("subtasksJson", t.subtasksJson)
                tObj.put("createdAt", t.createdAt)
                tasksArray.put(tObj)
            }
            root.put("tasks", tasksArray)

            val dir = File(context.cacheDir, "backups").apply { mkdirs() }
            val file = File(dir, "ciro_task_backup.json")
            val writer = FileWriter(file)
            writer.write(root.toString(2))
            writer.flush()
            writer.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun importBackupJson(
        context: Context,
        uri: Uri,
        repository: TaskRepository
    ): Int {
        var count = 0
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return 0
            val reader = BufferedReader(InputStreamReader(inputStream))
            val sb = java.lang.StringBuilder()
            var line: String? = reader.readLine()
            while (line != null) {
                sb.append(line)
                line = reader.readLine()
            }
            reader.close()
            inputStream.close()

            val root = JSONObject(sb.toString())

            // Restore Categories
            if (root.has("categories")) {
                val catsArray = root.getJSONArray("categories")
                for (i in 0 until catsArray.length()) {
                    val obj = catsArray.getJSONObject(i)
                    val cat = CategoryEntity(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        iconType = obj.optString("iconType", "book"),
                        colorHex = obj.optString("colorHex", "#5CD8D3"),
                        orderIndex = obj.optInt("orderIndex", i)
                    )
                    repository.insertCategory(cat)
                }
            }

            // Restore Tasks
            if (root.has("tasks")) {
                val tasksArray = root.getJSONArray("tasks")
                for (i in 0 until tasksArray.length()) {
                    val obj = tasksArray.getJSONObject(i)
                    val completedAtVal = obj.optLong("completedAt", 0L)
                    val task = TaskEntity(
                        title = obj.getString("title"),
                        remark = obj.optString("remark", ""),
                        categoryId = obj.optString("categoryId", "default"),
                        priority = obj.optString("priority", "Medium"),
                        tags = obj.optString("tags", ""),
                        isCompleted = obj.optBoolean("isCompleted", false),
                        completedAt = if (completedAtVal > 0) completedAtVal else null,
                        dueTimestamp = obj.optLong("dueTimestamp", System.currentTimeMillis()),
                        isRecurring = obj.optBoolean("isRecurring", false),
                        recurringType = obj.optString("recurringType", "none"),
                        hasReminder = obj.optBoolean("hasReminder", true),
                        subtasksJson = obj.optString("subtasksJson", "[]"),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                    repository.insertTask(task)
                    count++
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return count
    }

    fun exportTasksCsv(
        context: Context,
        tasks: List<TaskEntity>,
        categories: List<CategoryEntity>
    ): File? {
        return try {
            val categoriesMap = categories.associateBy { it.id }
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val file = File(exportDir, "ciro_tasks_export.csv")
            val writer = FileWriter(file)
            writer.append("ID,Title,Category,Priority,Due Date,Status,Remark,Tags\n")
            for (t in tasks) {
                val catName = categoriesMap[t.categoryId]?.name ?: "Default"
                val status = if (t.isCompleted) "Completed" else "Active"
                val dateStr = dateFormat.format(Date(t.dueTimestamp))
                val titleEscaped = "\"${t.title.replace("\"", "\"\"")}\""
                val remarkEscaped = "\"${t.remark.replace("\"", "\"\"")}\""
                val tagsEscaped = "\"${t.tags.replace("\"", "\"\"")}\""
                writer.append("${t.id},$titleEscaped,$catName,${t.priority},$dateStr,$status,$remarkEscaped,$tagsEscaped\n")
            }
            writer.flush()
            writer.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
