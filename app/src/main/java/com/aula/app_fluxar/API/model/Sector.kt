package com.aula.app_fluxar.API.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Sector(
    val id: Long,
    val nome: String,
    val descricao: String
) : Parcelable
