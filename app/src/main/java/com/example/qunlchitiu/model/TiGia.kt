package com.example.qunlchitiu.model

data class ResponseTiGia(
    val base: String,
    val date: String,
    val rates: Map<String, Double>
)
