package com.aula.app_fluxar.API.service

import com.aula.app_fluxar.API.model.NotificationRequest
import com.aula.app_fluxar.API.model.NotificationResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface NotificationAPIService {
    @POST("predict")
    suspend fun getNotification(@Body request: NotificationRequest): Response<NotificationResponse>
}