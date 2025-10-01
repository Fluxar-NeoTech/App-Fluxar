package com.aula.app_fluxar.API.model

data class CapacityStockRequest(
    val width: Double,
    val height: Double,
    val length: Double,
    val sectorId: Long,
    val unitId: Long
)

data class CapacityStockResponse(
    val width: Double,
    val height: Double,
    val length: Double,
    val maxCapacity: Double,
    val sector: Sector,
    val unit: Unit
)