package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.NavTab
import com.example.ui.theme.PastelBlue
import com.example.ui.theme.PastelOrange
import com.example.ui.theme.WarmText

@Composable
fun FloatingBottomNavBar(
    currentTab: NavTab,
    onTabSelected: (NavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(32.dp),
        color = Color(0xFFFBF6EC),
        shadowElevation = 8.dp,
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CuteNavItem(
                emoji = "📒",
                label = "Tasks",
                isSelected = currentTab == NavTab.TASKS,
                selectedBg = PastelOrange,
                onClick = { onTabSelected(NavTab.TASKS) }
            )

            CuteNavItem(
                emoji = "📅",
                label = "Calend.",
                isSelected = currentTab == NavTab.CALENDAR,
                selectedBg = PastelBlue,
                onClick = { onTabSelected(NavTab.CALENDAR) }
            )

            CuteNavItem(
                emoji = "📊",
                label = "Stats",
                isSelected = currentTab == NavTab.STATS,
                selectedBg = Color(0xFFD4EFE8),
                onClick = { onTabSelected(NavTab.STATS) }
            )

            CuteProfileNavItem(
                label = "Setting",
                isSelected = currentTab == NavTab.SETTINGS,
                selectedBg = Color(0xFFEBDCFC),
                onClick = { onTabSelected(NavTab.SETTINGS) }
            )
        }
    }
}

@Composable
private fun CuteNavItem(
    emoji: String,
    label: String,
    isSelected: Boolean,
    selectedBg: Color,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) selectedBg else Color.Transparent,
        label = "navBg"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(horizontal = if (isSelected) 14.dp else 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = emoji,
                fontSize = 20.sp
            )

            AnimatedVisibility(visible = isSelected) {
                Row {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = WarmText
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun CuteProfileNavItem(
    label: String,
    isSelected: Boolean,
    selectedBg: Color,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) selectedBg else Color.Transparent,
        label = "navBg"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(horizontal = if (isSelected) 14.dp else 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            CuteMascotAvatar(size = 24.dp)

            AnimatedVisibility(visible = isSelected) {
                Row {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = WarmText
                        )
                    )
                }
            }
        }
    }
}
