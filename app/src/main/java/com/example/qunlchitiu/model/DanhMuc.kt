package com.example.qunlchitiu.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class DanhMuc(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val icon: String,
    val isIncome: Boolean,
    val isDefault: Boolean = false
)
