package com.example.qunlchitiu.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class NhacNho(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val amount: Double,
    val dueDate: Long,
    val isPaid: Boolean = false,
    val category: String = "Khác"
)
