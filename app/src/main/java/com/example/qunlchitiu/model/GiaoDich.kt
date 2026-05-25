package com.example.qunlchitiu.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class GiaoDich(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val amount: Double,
    val category: String,
    val date: Long,
    val note: String = "",
    val isIncome: Boolean = false
)
