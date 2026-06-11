package com.example.qunlchitiu.data

import com.example.qunlchitiu.model.ResponseTiGia
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface TiGiaService {
    @GET("v6/latest/VND")
    suspend fun getTiGiaVND(): ResponseTiGia

    companion object {
        private const val BASE_URL = "https://open.er-api.com/"

        fun create(): TiGiaService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(TiGiaService::class.java)
        }
    }
}
