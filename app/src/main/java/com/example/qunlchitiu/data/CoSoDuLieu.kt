package com.example.qunlchitiu.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.qunlchitiu.model.*

@Database(entities = [GiaoDich::class, DanhMuc::class, NganSach::class, NganSachDanhMuc::class, MucTieuTietKiem::class, NhacNho::class], version = 7, exportSchema = false)
abstract class CoSoDuLieu : RoomDatabase() {
    abstract fun expenseDao(): TruyVanDuLieu

    companion object {
        @Volatile
        private var Instance: CoSoDuLieu? = null

        fun getDatabase(context: Context): CoSoDuLieu {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, CoSoDuLieu::class.java, "expense_database")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
