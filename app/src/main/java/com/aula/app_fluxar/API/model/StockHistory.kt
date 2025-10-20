package com.aula.app_fluxar.API.model

data class StockHistory(
    val movement: String,
    val volumeHandled: Double,
    val date: String
)
