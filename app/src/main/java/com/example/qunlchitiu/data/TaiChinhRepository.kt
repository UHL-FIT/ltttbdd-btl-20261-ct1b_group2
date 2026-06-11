package com.example.qunlchitiu.data

import com.example.qunlchitiu.model.ResponseTiGia
import com.example.qunlchitiu.model.GiaoDich
import kotlinx.coroutines.flow.Flow

class TaiChinhRepository(
    private val dao: TruyVanDuLieu,
    private val api: TiGiaService
) {
    // Logic lấy từ Database (Room)
    fun getAllExpenses(): Flow<List<GiaoDich>> = dao.getAllExpenses()
    
    // Logic lấy từ Network (Retrofit API)
    suspend fun getExchangeRates(): ResponseTiGia {
        return api.getTiGiaVND()
    }
}
