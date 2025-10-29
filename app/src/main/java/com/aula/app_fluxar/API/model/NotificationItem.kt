package com.aula.app_fluxar.API.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class NotificationItem(
    val titulo: String,
    val mensagem: String,
    val data: String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
)
