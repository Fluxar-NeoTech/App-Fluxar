package com.aula.app_fluxar.API.model

data class CapacityHistory(
    val fullDate: String,
    val totalCapacity: Double,
    val occupancyPercentage: Double
)

data class CapacitySectorInfos(
    val occupancyPercentage: Double,
    val remainingVolume: Double
)