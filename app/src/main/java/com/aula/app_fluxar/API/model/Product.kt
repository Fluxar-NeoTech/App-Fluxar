package com.aula.app_fluxar.API.model

data class ProductResponse(
    val id: Long,
    val type: String,
    val name: String
)

data class ProductRequest(
    val name: String,
    val type: String,
    val sectorId: Long
)
