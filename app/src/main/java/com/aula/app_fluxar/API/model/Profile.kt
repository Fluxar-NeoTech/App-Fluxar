package com.aula.app_fluxar.API.model

data class Profile(
    val firstName: String,
    val lastName: String,
    val email: String,
    val profilePhoto: String?,
    val sector: Sector,
    val unit: Unit,
    val maxCapacity: Double,
    val plan: Plan
)
