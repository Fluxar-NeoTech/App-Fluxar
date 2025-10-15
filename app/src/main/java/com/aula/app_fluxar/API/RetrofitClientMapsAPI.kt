package com.aula.app_fluxar.API

import com.aula.app_fluxar.API.service.APIService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClientMapsAPI {
    private const val BASE_URL = "https://maps.googleapis.com/maps/api/"

    val instance: APIService by lazy {
        val retrofitMapsApi = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofitMapsApi.create(APIService::class.java)
    }

}

