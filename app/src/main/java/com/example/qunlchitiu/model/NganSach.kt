package com.example.qunlchitiu.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class NganSach(
    @PrimaryKey val monthYear: String, // Format: "MM/yyyy"
    val limitAmount: Double,
    val isAlertEnabled: Boolean = true
)
