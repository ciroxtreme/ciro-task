package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = WarmPrimary,
    onPrimary = Color.White,
    primaryContainer = WarmPrimaryLight,
    onPrimaryContainer = Color(0xFF561E1A),
    secondary = WarmSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF9E7D2),
    onSecondaryContainer = Color(0xFF4C3214),
    tertiary = PastelBlueDark,
    onTertiary = Color.White,
    background = CreamBg,
    onBackground = WarmText,
    surface = CardBg,
    onSurface = WarmText,
    surfaceVariant = WarmSurfaceVariant,
    onSurfaceVariant = WarmTextSecondary,
    outline = CardBorder
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
