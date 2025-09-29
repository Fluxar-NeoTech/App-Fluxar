package com.aula.app_fluxar.API.service

import com.aula.app_fluxar.API.model.CapacityStockRequest
import com.aula.app_fluxar.API.model.CapacityStockResponse
import com.aula.app_fluxar.API.model.Employee
import com.aula.app_fluxar.API.model.LoginRequest
import com.aula.app_fluxar.API.model.UpdatePhotoRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT

interface APIService {
    @POST("api/employee/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<Employee>

    @PUT("api/employee/update/photo")
    suspend fun updatePhoto(@Body employee: UpdatePhotoRequest): Response<Map<String, String>>

    @POST("/api/capacityStock/addCapacityStock")
    suspend fun addCapacityStock(@Body capacityStockRequest: CapacityStockRequest): Response<CapacityStockResponse>
}