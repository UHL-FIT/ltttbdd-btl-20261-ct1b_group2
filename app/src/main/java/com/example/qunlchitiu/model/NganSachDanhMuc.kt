package com.example.qunlchitiu.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "category_budgets")
data class NganSachDanhMuc(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val monthYear: String, // Format: "MM/yyyy"
    val categoryName: String,
    val limitAmount: Double
)
