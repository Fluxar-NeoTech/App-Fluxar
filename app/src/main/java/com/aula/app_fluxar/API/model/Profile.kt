package com.aula.app_fluxar.API.model

data class Profile(
    val nome: String,
    val sobrenome: String,
    val email: String,
    val fotoPerfil: String,
    val setor: Sector,
    val unit: Unit,
    val capacidadeMaxima: Double
)
