package com.aula.app_fluxar.API.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Unit(
    val id: Long,
    val nome: String,
    val cep: String,
    val rua: String,
    val cidade: String,
    val estado: String,
    val numero: String,
    val bairro: String,
    val industry: Industry
) : Parcelable
