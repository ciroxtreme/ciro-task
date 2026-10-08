package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Locale

data class ApkInfo(
    val fileName: String,
    val sizeFormatted: String,
    val sourcePath: String,
    val packageName: String,
    val versionName: String
)

object ApkExtractor {

    fun getApkInfo(context: Context): ApkInfo {
        val sourceDir = context.applicationInfo.sourceDir
        val file = File(sourceDir)
        val sizeBytes = if (file.exists()) file.length() else 0L
        val sizeFormatted = formatFileSize(sizeBytes)
        val pInfo = try {
            context.packageManager.getPackageInfo(context.packageName, 0)
        } catch (_: Exception) {
            null
        }
        val versionName = pInfo?.versionName ?: "1.0"

        return ApkInfo(
            fileName = "CiroTask_v${versionName}.apk",
            sizeFormatted = sizeFormatted,
            sourcePath = sourceDir,
            packageName = context.packageName,
            versionName = versionName
        )
    }

    private fun formatFileSize(size: Long): String {
        if (size <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        val index = digitGroups.coerceIn(0, units.size - 1)
        return String.format(Locale.US, "%.1f %s", size / Math.pow(1024.0, index.toDouble()), units[index])
    }

    fun shareApk(context: Context) {
        try {
            val sourceFile = File(context.applicationInfo.sourceDir)
            if (!sourceFile.exists()) {
                Toast.makeText(context, "File APK tidak ditemukan", Toast.LENGTH_SHORT).show()
                return
            }

            // Copy to cache dir
            val targetDir = File(context.cacheDir, "extracted_apks")
            if (!targetDir.exists()) targetDir.mkdirs()
            val targetFile = File(targetDir, "CiroTask.apk")

            FileInputStream(sourceFile).use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }

            val apkUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                targetFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/vnd.android.package-archive"
                putExtra(Intent.EXTRA_STREAM, apkUri)
                putExtra(Intent.EXTRA_SUBJECT, "Ciro Task - APK File")
                putExtra(Intent.EXTRA_TEXT, "File APK aplikasi Ciro Task untuk diupload atau dibagikan.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Bagikan / Upload APK Ciro Task")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal mengekstrak APK: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun exportApkToUri(context: Context, destinationUri: Uri): Boolean {
        return try {
            val sourceFile = File(context.applicationInfo.sourceDir)
            if (!sourceFile.exists()) return false

            context.contentResolver.openOutputStream(destinationUri)?.use { outStream ->
                FileInputStream(sourceFile).use { inStream ->
                    inStream.copyTo(outStream)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
