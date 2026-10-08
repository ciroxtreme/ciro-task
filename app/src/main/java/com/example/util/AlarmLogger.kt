package com.example.util

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AlarmLogger {
    private const val PREF_NAME = "ciro_alarm_debug_logs"
    private const val KEY_LOGS = "logs_history"

    fun log(context: Context, message: String) {
        try {
            val prefs = getPrefs(context)
            val time = SimpleDateFormat("HH:mm:ss (dd/MM)", Locale.getDefault()).format(Date())
            val entry = "[$time] $message"
            val currentLogs = getLogs(context)
            val updatedLogs = (listOf(entry) + currentLogs).take(50) // Keep last 50 entries
            prefs.edit().putString(KEY_LOGS, updatedLogs.joinToString("\n")).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getLogs(context: Context): List<String> {
        val raw = getPrefs(context).getString(KEY_LOGS, "") ?: ""
        return if (raw.isBlank()) emptyList() else raw.split("\n")
    }

    fun clearLogs(context: Context) {
        getPrefs(context).edit().clear().apply()
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }
}
