package com.aula.app_fluxar.API.service

import com.aula.app_fluxar.API.model.Employee
import com.aula.app_fluxar.API.model.LoginRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface APIService {
    @POST("api/employee/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<Employee>
}