package com.aula.app_fluxar.API.model

data class NotificationResponse (
    val data: String,
    val produto_id: Long,
    val unidade_id: Long,
    val days_to_stockout_pred: Double
)

data class NotificationRequest (
    val industria_id: Long,
    val setor_id: Long
)

