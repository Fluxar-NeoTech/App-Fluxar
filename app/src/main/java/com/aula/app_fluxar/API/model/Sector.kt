package com.aula.app_fluxar.API.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Sector(
    val id: Long,
    val name: String,
    val description: String
) : Parcelable
