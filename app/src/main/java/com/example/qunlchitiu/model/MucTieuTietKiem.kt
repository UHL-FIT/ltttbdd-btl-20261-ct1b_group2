package com.example.qunlchitiu.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "savings_goals")
data class MucTieuTietKiem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val targetDate: Long, // Timestamp for deadline
    val icon: String = "💰",
    val color: Int = 0xFF00796B.toInt()
)
