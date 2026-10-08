package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val iconType: String, // book, burger, backpack, pet, health, star, food, sport
    val colorHex: String,
    val orderIndex: Int = 0
)
