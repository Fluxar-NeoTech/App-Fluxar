package com.aula.app_fluxar.API.model

data class CapacityStockRequest(
    val largura: Double,
    val altura: Double,
    val comprimento: Double,
    val setorId: Long,
    val unidadeId: Long
)

data class CapacityStockResponse(
    val altura: Double,
    val capacidadeMaxima: Double,
    val comprimento: Double,
    val largura: Double,
    val setor: Sector,
    val unidade: Unit
)