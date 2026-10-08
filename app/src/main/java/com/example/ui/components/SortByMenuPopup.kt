package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.KeyboardDoubleArrowUp
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.ui.theme.WarmPrimary
import com.example.ui.theme.WarmText
import com.example.ui.theme.WarmTextSecondary

enum class SortOption {
    DATE,
    PRIORITY,
    LIST,
    NONE
}

@Composable
fun SortByMenuPopup(
    selectedSort: SortOption,
    onSortSelected: (SortOption) -> Unit,
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    Popup(
        alignment = Alignment.TopEnd,
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBF3)),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            modifier = Modifier
                .width(230.dp)
                .padding(top = 40.dp, end = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Section Title: Sort By
                Text(
                    text = "Sort By",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = WarmPrimary,
                        fontSize = 16.sp
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Sort Options List
                SortMenuItem(
                    label = "Date",
                    icon = Icons.Default.CalendarToday,
                    isSelected = selectedSort == SortOption.DATE,
                    onClick = {
                        onSortSelected(SortOption.DATE)
                        onDismiss()
                    }
                )

                Spacer(modifier = Modifier.height(6.dp))

                SortMenuItem(
                    label = "Priority",
                    icon = Icons.Default.KeyboardDoubleArrowUp,
                    isSelected = selectedSort == SortOption.PRIORITY,
                    onClick = {
                        onSortSelected(SortOption.PRIORITY)
                        onDismiss()
                    }
                )

                Spacer(modifier = Modifier.height(6.dp))

                SortMenuItem(
                    label = "List",
                    icon = Icons.Default.FormatListBulleted,
                    isSelected = selectedSort == SortOption.LIST,
                    onClick = {
                        onSortSelected(SortOption.LIST)
                        onDismiss()
                    }
                )

                Spacer(modifier = Modifier.height(6.dp))

                SortMenuItem(
                    label = "None",
                    icon = Icons.Default.Remove,
                    isSelected = selectedSort == SortOption.NONE,
                    onClick = {
                        onSortSelected(SortOption.NONE)
                        onDismiss()
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Section Title: Setting
                Text(
                    text = "Setting",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = WarmPrimary,
                        fontSize = 16.sp
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                SortMenuItem(
                    label = "Custom Style",
                    icon = Icons.Default.Palette,
                    isSelected = false,
                    onClick = {
                        onOpenSettings()
                        onDismiss()
                    }
                )
            }
        }
    }
}

@Composable
private fun SortMenuItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) WarmPrimary else Color(0xFFF7F2E8))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) Color.White.copy(alpha = 0.25f) else Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isSelected) Color.White else WarmText,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else WarmText
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
