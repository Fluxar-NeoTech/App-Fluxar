package com.aula.app_fluxar.API.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Employee(
    val id: Int,
    val nome: String,
    val sobrenome: String,
    val email: String,
    val cargo: Char,
    val fotoPerfil: String,
    val setor: Sector,
    val unit: Unit,
    val capacidadeMaxima: Double
) : Parcelable

data class LoginRequest(
    val email: String,
    val senha: String
)

data class UpdatePhotoRequest(
    val email: String,
    val fotoPerfil: String
)