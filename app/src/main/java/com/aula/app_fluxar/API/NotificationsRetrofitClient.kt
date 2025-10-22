package com.aula.app_fluxar.API

import com.aula.app_fluxar.API.service.NotificationAPIService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NotificationsRetrofitClient {

    private const val BASE_URL = "http://SEU_IP:5000/" //url do render

    val instance: NotificationAPIService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NotificationAPIService::class.java)
    }
}