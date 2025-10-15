package com.aula.app_fluxar.API.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Unit(
    val id: Long,
    val name: String,
    val postalCode: String,
    val street: String,
    val city: String,
    val state: String,
    val number: String,
    val neighborhood: String,
    val industry: Industry
) : Parcelable {
    fun enderecoCompleto(): String {
        return "$street, $number - $neighborhood, $city - $state, $postalCode, Brasil"
    }
}