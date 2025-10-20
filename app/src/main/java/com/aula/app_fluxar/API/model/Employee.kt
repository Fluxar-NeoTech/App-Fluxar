package com.aula.app_fluxar.API.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Employee(
    val id: Long,
    val email: String,
    val role: Char,
    val token: String? = null
) : Parcelable

data class LoginRequest(
    val email: String,
    val password: String,
    val origin: String
)

data class UpdatePhotoRequest(
    val email: String,
    val profilePhoto: String
)