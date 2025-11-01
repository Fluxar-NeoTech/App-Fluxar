package com.aula.app_fluxar.API.service


import com.aula.app_fluxar.API.model.StockOutListResponse
import com.aula.app_fluxar.API.model.StockOutRequest
import com.aula.app_fluxar.API.model.StockOutResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface StockOutAPIService {
    @POST("/predict")
    suspend fun getNotification(@Body stockOutRequest: StockOutRequest):
            Response<StockOutListResponse>
}
