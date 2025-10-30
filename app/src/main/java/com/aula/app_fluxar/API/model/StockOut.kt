package com.aula.app_fluxar.API.model

data class StockOutListResponse(
    val predictions: List<StockOutResponse>
)

data class StockOutResponse(
    val data: String,
    val produto_id: Long,
    val unidade_id: Long,
    val days_to_stockout_pred: Double
)

data class StockOutRequest(
    val industria_id: Long,
    val setor_id: Long
)
