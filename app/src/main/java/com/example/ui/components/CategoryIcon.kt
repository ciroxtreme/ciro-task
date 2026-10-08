package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.PastelBlue
import com.example.ui.theme.PastelBlueDark
import com.example.ui.theme.PastelGreen
import com.example.ui.theme.PastelGreenDark
import com.example.ui.theme.PastelOrange
import com.example.ui.theme.PastelOrangeDark
import com.example.ui.theme.PastelPink
import com.example.ui.theme.PastelPinkDark
import com.example.ui.theme.PastelPurple
import com.example.ui.theme.PastelPurpleDark

@Composable
fun CategoryIconBadge(
    iconType: String,
    modifier: Modifier = Modifier,
    size: Dp = 38.dp,
    shapeRadius: Dp = 12.dp
) {
    val (bgGradient, emojiSymbol, subBadge) = when (iconType.lowercase()) {
        "burger", "family", "food" -> Triple(
            listOf(Color(0xFFFFF0D6), Color(0xFFFCE1B6)),
            "🍔",
            "✨"
        )
        "backpack", "study", "school" -> Triple(
            listOf(Color(0xFFFFE5E8), Color(0xFFFCD0D5)),
            "🎒",
            "📚"
        )
        "pet", "cat", "dog" -> Triple(
            listOf(Color(0xFFF3EAFF), Color(0xFFE5D2FC)),
            "🐱",
            "🐾"
        )
        "health", "hospital", "heart" -> Triple(
            listOf(Color(0xFFE4F9EB), Color(0xFFCCF2D7)),
            "🩺",
            "💖"
        )
        "work" -> Triple(
            listOf(Color(0xFFEBEBFC), Color(0xFFD6D6F9)),
            "💼",
            "⭐"
        )
        "sport", "game" -> Triple(
            listOf(Color(0xFFFFECE5), Color(0xFFFFD5C7)),
            "🏸",
            "⚡"
        )
        "star" -> Triple(
            listOf(Color(0xFFFFF9DB), Color(0xFFFFF0A6)),
            "⭐",
            "✨"
        )
        else -> Triple(
            listOf(Color(0xFFE5F1FC), Color(0xFFCCE4FA)),
            "📖",
            "✏️"
        )
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(shapeRadius))
            .background(Brush.linearGradient(bgGradient))
            .border(1.dp, Color(0xFFFFFFFF), RoundedCornerShape(shapeRadius)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = emojiSymbol,
            fontSize = (size.value * 0.52f).sp
        )

        // Cute mini corner detail
        if (size >= 34.dp) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-2).dp, y = (-2).dp)
                    .size(size * 0.32f)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = subBadge,
                    fontSize = (size.value * 0.22f).sp
                )
            }
        }
    }
}

@Composable
fun CuteMascotAvatar(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Color(0xFFEBDCFC))
            .border(2.dp, Color.White, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_ciro_mascot_1791117716520),
            contentDescription = "Ciro Mascot",
            modifier = Modifier.size(size)
        )
    }
}

fun getCategoryColor(categoryId: String): Color {
    return when (categoryId.lowercase()) {
        "family", "food" -> PastelOrange
        "study", "school" -> PastelPink
        "pet" -> PastelPurple
        "health" -> PastelGreen
        else -> PastelBlue
    }
}

fun getCategoryDarkColor(categoryId: String): Color {
    return when (categoryId.lowercase()) {
        "family", "food" -> PastelOrangeDark
        "study", "school" -> PastelPinkDark
        "pet" -> PastelPurpleDark
        "health" -> PastelGreenDark
        else -> PastelBlueDark
    }
}
