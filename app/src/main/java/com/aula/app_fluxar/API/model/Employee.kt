package com.aula.app_fluxar.API.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Employee(
    val id: Long,
    val email: String,
    val role: Char,
) : Parcelable

data class LoginRequest(
    val email: String,
    val senha: String
)

data class UpdatePhotoRequest(
    val email: String,
    val fotoPerfil: String
)