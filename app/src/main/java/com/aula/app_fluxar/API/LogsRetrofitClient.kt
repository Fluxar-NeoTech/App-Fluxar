package com.aula.app_fluxar.API

import com.aula.app_fluxar.API.service.LogsAPIService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object LogsRetrofitClient {
    private const val BASE_URL = "https://api-logs-fluxar-1.onrender.com"
    private const val TIMEOUT_SECONDS = 30L

    val instance: LogsAPIService by lazy {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(LogsAPIService::class.java)
    }
}