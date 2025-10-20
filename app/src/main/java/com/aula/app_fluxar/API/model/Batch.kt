package com.aula.app_fluxar.API.model

data class Batch(
    val batchCode: String,
    val expirationDate: String,
    val height: Double,
    val length: Double,
    val width: Double,
    val productName: String
)

data class BatchRequest(
    val batchCode: String,
    val expirationDate: String,
    val height: Double,
    val length: Double,
    val width: Double,
    val unitId: Long,
    val productId: Long
)