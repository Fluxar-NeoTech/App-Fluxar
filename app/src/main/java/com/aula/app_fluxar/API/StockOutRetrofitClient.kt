package com.aula.app_fluxar.API

import com.aula.app_fluxar.API.service.StockOutAPIService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object StockOutRetrofitClient {
    private const val BASE_URL = "http://34.229.184.54:8001"

    val instance: StockOutAPIService by lazy {
        val retrofitStockOutAPI = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofitStockOutAPI.create(StockOutAPIService::class.java)
    }
}
