package com.example.util

import androidx.compose.ui.graphics.Color

data class TagData(
    val name: String,
    val icon: String,
    val bgColor: Color
)

object TagHelper {

    val PredefinedTags = listOf(
        TagData("Tree", "🥦", Color(0xFFD8F3DF)),
        TagData("Bag", "🎒", Color(0xFFFDE5C5)),
        TagData("Tooth", "🦷", Color(0xFFD6F6F5)),
        TagData("Sport", "🏋️", Color(0xFFFCDADC)),
        TagData("Design", "🖊", Color(0xFFD6E9FA)),
        TagData("Food", "🍔", Color(0xFFFFF0D6)),
        TagData("Pet", "🐾", Color(0xFFE9DEFC)),
        TagData("Study", "📚", Color(0xFFFCDADC)),
        TagData("Health", "🩺", Color(0xFFD9F4DF)),
        TagData("Work", "💼", Color(0xFFE2E2F9)),
        TagData("Coffee", "☕", Color(0xFFFCE6CA)),
        TagData("Game", "🎮", Color(0xFFFFDFD3)),
        TagData("Star", "⭐", Color(0xFFFFF2C2)),
        TagData("Music", "🎵", Color(0xFFFDE8C7))
    )

    fun getTagData(rawTag: String): TagData {
        val trimmed = rawTag.trim()
        if (trimmed.isEmpty()) return TagData("Tag", "🏷️", Color(0xFFEAE5DE))

        // Check if format is "Name:Icon:ColorHex"
        val parts = trimmed.split(":")
        if (parts.size >= 3) {
            val name = parts[0]
            val icon = parts[1]
            val color = try {
                Color(android.graphics.Color.parseColor(parts[2]))
            } catch (_: Exception) {
                Color(0xFFEAE5DE)
            }
            return TagData(name, icon, color)
        }

        // Match predefined tags
        val matched = PredefinedTags.find { it.name.equals(trimmed, ignoreCase = true) }
        if (matched != null) return matched

        // Keyword based fallback
        return when (trimmed.lowercase()) {
            "pohon", "tree", "plant", "sayur", "broccoli" -> TagData(trimmed, "🥦", Color(0xFFD8F3DF))
            "tas", "bag", "makanan_hewan", "pet_bag" -> TagData(trimmed, "🎒", Color(0xFFFDE5C5))
            "gigi", "tooth", "dental" -> TagData(trimmed, "🦷", Color(0xFFD6F6F5))
            "olahraga", "sport", "gym" -> TagData(trimmed, "🏋️", Color(0xFFFCDADC))
            "desain", "design", "pen", "gambar" -> TagData(trimmed, "🖊", Color(0xFFD6E9FA))
            "makan", "food", "burger" -> TagData(trimmed, "🍔", Color(0xFFFFF0D6))
            "hewan", "pet", "kucing", "cat", "dog" -> TagData(trimmed, "🐾", Color(0xFFE9DEFC))
            "belajar", "study", "school" -> TagData(trimmed, "📚", Color(0xFFFCDADC))
            "kesehatan", "health", "obat" -> TagData(trimmed, "🩺", Color(0xFFD9F4DF))
            "kerja", "work", "office" -> TagData(trimmed, "💼", Color(0xFFE2E2F9))
            "kopi", "coffee" -> TagData(trimmed, "☕", Color(0xFFFCE6CA))
            else -> TagData(trimmed, "🏷️", Color(0xFFEDE7DE))
        }
    }
}
