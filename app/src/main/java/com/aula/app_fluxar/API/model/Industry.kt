package com.aula.app_fluxar.API.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Industry(
    val nome: String,
    val cnpj: String
) : Parcelable