package com.aula.app_fluxar.API.service

import com.aula.app_fluxar.API.model.Employee
import com.aula.app_fluxar.API.model.GeocodingResponse
import com.aula.app_fluxar.API.model.LoginRequest
import com.aula.app_fluxar.API.model.UpdatePhotoRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface APIService {
    @POST("api/employee/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<Employee>

    @PUT("api/employee/update/photo")
    suspend fun updatePhoto(@Body employee: UpdatePhotoRequest): Response<Map<String, String>>


    // MAPS- API
    @GET("geocode/json")
    suspend fun getLocation(
        @Query("address") address: String,
        @Query("key") apiKey: String
    ): GeocodingResponse

}