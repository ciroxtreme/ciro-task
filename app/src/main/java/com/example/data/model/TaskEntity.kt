package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val remark: String = "",
    val categoryId: String = "default",
    val priority: String = "Medium", // High, Medium, Low, None
    val tags: String = "", // Comma-separated
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val dueTimestamp: Long = System.currentTimeMillis(),
    val isRecurring: Boolean = false,
    val recurringType: String = "none", // daily, weekly, none
    val hasReminder: Boolean = true,
    val subtasksJson: String = "[]",
    val createdAt: Long = System.currentTimeMillis()
)

data class SubTask(
    val id: String,
    val title: String,
    val isCompleted: Boolean
)

object SubTaskConverter {
    fun fromJson(json: String): List<SubTask> {
        if (json.isBlank()) return emptyList()
        return try {
            val list = mutableListOf<SubTask>()
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    SubTask(
                        id = obj.optString("id", i.toString()),
                        title = obj.optString("title", ""),
                        isCompleted = obj.optBoolean("isCompleted", false)
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun toJson(subtasks: List<SubTask>): String {
        val array = JSONArray()
        for (item in subtasks) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("title", item.title)
            obj.put("isCompleted", item.isCompleted)
            array.put(obj)
        }
        return array.toString()
    }
}
