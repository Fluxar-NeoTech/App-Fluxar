package com.aula.app_fluxar.API.service

import com.aula.app_fluxar.API.model.Batch
import com.aula.app_fluxar.API.model.CapacityStockRequest
import com.aula.app_fluxar.API.model.CapacityStockResponse
import com.aula.app_fluxar.API.model.Employee
import com.aula.app_fluxar.API.model.GeocodingResponse
import com.aula.app_fluxar.API.model.LoginRequest
import com.aula.app_fluxar.API.model.Profile
import com.aula.app_fluxar.API.model.UpdatePhotoRequest
import com.aula.app_fluxar.API.model.Unit as UnitModel
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

    @POST("/api/capacityStock/add")
    suspend fun addCapacityStock(@Body capacityStockRequest: CapacityStockRequest): Response<CapacityStockResponse>

    @GET("/api/unit/search/all/by/industry/{id}")
    suspend fun getUnitsByIndustryID(@Path("id") id: Long): Response<List<UnitModel>>

    @GET("/api/employee/profile/{id}")
    suspend fun getProfileInfos(@Path("id") id: Long): Response<Profile>

    @GET("/api/batch/search/all/product/by/unit/sector")
    suspend fun getBatchesEmployee(
        @Query("unitId") unitID: Long,
        @Query("sectorId") sectorID: Long
    ): Response<List<Batch>>

    // MAPS- API
    @GET("geocode/json")
    suspend fun getLocation(
        @Query("address") address: String,
        @Query("key") apiKey: String
    ): GeocodingResponse

}