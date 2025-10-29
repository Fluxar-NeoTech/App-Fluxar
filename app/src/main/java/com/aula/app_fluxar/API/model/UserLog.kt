package com.aula.app_fluxar.API.model

import java.time.LocalDate
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserLog(
    val user_id: Long,
    val action: String,
    val done_at: String
) : Parcelable

data class UserLogRequest (
    val user_id: Long,
    val action: String
)
