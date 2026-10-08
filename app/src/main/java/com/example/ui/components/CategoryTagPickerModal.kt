package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.WarmPrimary
import com.example.ui.theme.WarmText
import com.example.ui.theme.WarmTextSecondary

val CutePaletteColors = listOf(
    Color(0xFFF6A59A), // Coral Peach
    Color(0xFFF9D3C7), // Pale Blush
    Color(0xFFFDE49C), // Butter Yellow
    Color(0xFFA8E6CF), // Mint Green
    Color(0xFF5CD8D3), // Cyan/Teal
    Color(0xFF7ED6DF), // Sky Blue
    Color(0xFFE8C5F8)  // Soft Lavender
)

val CuteIconList = listOf(
    // Food & Treats (Matching Screenshot 2)
    "🍔", "🌭", "🍟", "🍗", "🍳",
    "🌮", "🌯", "🍕", "🍜", "🥞",
    "🥡", "🥩", "🐟", "☕", "🍺",
    "🥐", "🍯", "🥫", "🥪", "🍩",
    "🍰", "🥤", "🍣", "🥓", "🍖",
    "🧁", "🍿", "🧃", "🥛", "🍹",
    "🧋", "🍨", "🍦", "🍧", "🍞",
    "🍪", "🥣", "🍫", "🥢", "🍛",
    "⚡", "🍇", "🍓", "🍎", "🍉",
    // Daily Life, Study & Hobbies
    "🎒", "📖", "🖊", "📚", "🐱",
    "🐶", "🐾", "🩺", "💖", "🦷",
    "🏋️", "🏸", "⚽", "🎮", "💼",
    "🎨", "🎵", "✈️", "⭐", "🌿"
)

@Composable
fun CategoryTagPickerModal(
    initialName: String = "Default",
    initialIcon: String = "🍔",
    initialColorHex: String = "#5CD8D3",
    titleDialog: String = "Edit Kategori & Ikon",
    onDismiss: () -> Unit,
    onConfirm: (name: String, icon: String, colorHex: String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var selectedIcon by remember { mutableStateOf(initialIcon) }
    var selectedColor by remember {
        mutableStateOf(
            try {
                Color(android.graphics.Color.parseColor(initialColorHex))
            } catch (_: Exception) {
                CutePaletteColors[4]
            }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBF3)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Top Row: Selected Icon + Name Input + Rounded Red Checkmark Button (Screenshot 2)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Selected Icon Preview
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(selectedColor.copy(alpha = 0.35f))
                            .border(2.dp, selectedColor, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = selectedIcon, fontSize = 24.sp)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Name Input
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Nama Kategori / Tag", fontSize = 13.sp) },
                        shape = RoundedCornerShape(18.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = selectedColor,
                            unfocusedBorderColor = Color(0xFFEBE4D8),
                            focusedContainerColor = Color(0xFFF7F2E8),
                            unfocusedContainerColor = Color(0xFFF7F2E8)
                        )
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    // Red/Coral Squircle Confirm Button (Screenshot 2)
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(WarmPrimary)
                            .clickable {
                                if (name.isNotBlank()) {
                                    val hex = String.format("#%06X", (0xFFFFFF and selectedColor.hashCode()))
                                    onConfirm(name.trim(), selectedIcon, hex)
                                    onDismiss()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Confirm",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Color Palette Row (Screenshot 2)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CutePaletteColors.forEach { color ->
                        val isSelected = selectedColor == color
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { selectedColor = color }
                                .then(
                                    if (isSelected) {
                                        Modifier.border(3.dp, WarmText, CircleShape)
                                    } else Modifier
                                )
                        )
                    }

                    // Edit / Custom color pencil icon
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(WarmPrimary.copy(alpha = 0.2f))
                            .clickable { selectedColor = WarmPrimary },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Custom Color",
                            tint = WarmPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Pilih Ikon Lucu:",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = WarmText
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Huge 5-column Cute Icon Grid (Matching Screenshot 2)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    items(CuteIconList) { emoji ->
                        val isSelected = selectedIcon == emoji
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) selectedColor.copy(alpha = 0.35f) else Color(0xFFF7F2E8))
                                .clickable { selectedIcon = emoji }
                                .then(
                                    if (isSelected) Modifier.border(2.dp, selectedColor, RoundedCornerShape(12.dp))
                                    else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 22.sp)
                        }
                    }
                }
            }
        }
    }
}
