package com.aula.app_fluxar.API.model

data class Employee(
    val id: Int,
    val nome: String,
    val sobrenome: String,
    val email: String,
    val fotoPerfil: String
)

data class LoginRequest(
    val email: String,
    val senha: String
)