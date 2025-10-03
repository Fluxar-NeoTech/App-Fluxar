package com.aula.app_fluxar.API.model

import java.time.LocalDate

data class Batch(
    val batchCode: String,
    val expirationDate: String,
    val height: Double,
    val length: Double,
    val width: Double,
    val productName: String
)
