package com.aula.app_fluxar.API.service

import com.aula.app_fluxar.API.model.UserLog
import com.aula.app_fluxar.API.model.UserLogRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface LogsAPIService {
    @POST("/api/user/logs/add")
    suspend fun addUserLog(@Body userLogRequest: UserLogRequest): Response<UserLog>
}