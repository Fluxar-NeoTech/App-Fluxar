package com.aula.app_fluxar.API.service

import com.aula.app_fluxar.API.model.Batch
import com.aula.app_fluxar.API.model.BatchRequest
import com.aula.app_fluxar.API.model.CapacityHistory
import com.aula.app_fluxar.API.model.CapacitySectorInfos
import com.aula.app_fluxar.API.model.CapacityStockRequest
import com.aula.app_fluxar.API.model.CapacityStockResponse
import com.aula.app_fluxar.API.model.Employee
import com.aula.app_fluxar.API.model.GeocodingResponse
import com.aula.app_fluxar.API.model.LoginRequest
import com.aula.app_fluxar.API.model.ProductRequest
import com.aula.app_fluxar.API.model.Profile
import com.aula.app_fluxar.API.model.UpdatePhotoRequest
import com.aula.app_fluxar.API.model.ProductResponse
import com.aula.app_fluxar.API.model.StockHistory
import com.aula.app_fluxar.API.model.UnitInfos
import com.aula.app_fluxar.API.model.EmailRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
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

    @POST("/api/product/add")
    suspend fun addProduct(@Body productRequest: ProductRequest): Response<Map<String, String>>

    @DELETE("/api/batch/delete/{batchCode}")
    suspend fun deleteBatch(@Path("batchCode") batchCode: String): Response<Map<String, String>>

    @POST("/api/email/send")
    suspend fun redefinePassword(@Body email: EmailRequest): Response<Map<String, String>>

    @POST("api/batch/add")
    suspend fun addBatch(@Body batchRequest: BatchRequest): Response<Map<String, String>>

    @GET("/api/unit/search/all/by/industry/{id}")
    suspend fun getUnitsByIndustryID(@Path("id") id: Long): Response<List<UnitInfos>>

    @GET("/api/sector/search/volume/remaining/in/sector/employee")
    suspend fun getRemainingVolumeBySector(
        @Query("sectorId") sectorID: Long,
        @Query("employeeId") employeeID: Long
    ): Response<Double>

    @GET("/api/sector/search/volume/used/in/sector/employee")
    suspend fun getUsedVolumeBySector(
        @Query("sectorId") sectorID: Long,
        @Query("employeeId") employeeID: Long
    ): Response<Double>

    @GET("/api/stockHistory/search/by/unit/sector")
    suspend fun getStockHistory(
        @Query("unitId") unitID: Long,
        @Query("sectorId") sectorID: Long)
    : Response<StockHistory>

    @GET("/api/capacityHistory/search/by/unit/{unitId}")
    suspend fun getCapacityHistory(@Path("unitId") unitID: Long): Response<CapacityHistory>

    @GET("/api/employee/profile/{id}")
    suspend fun getProfileInfos(@Path("id") id: Long): Response<Profile>

    @GET("/api/batch/search/all/product/by/unit/sector")
    suspend fun getBatchesEmployee(
        @Query("unitId") unitID: Long,
        @Query("sectorId") sectorID: Long
    ): Response<List<Batch>>

    @GET("/api/capacityHistory/search/occupation/by/sector")
    suspend fun getSectorCapacityInfos(
        @Query("sectorId") sectorID: Long,
        @Query("employeeId") employeeID: Long
    ): Response<CapacitySectorInfos>

    @GET("/api/product/search/all/product/by/unit/{employeeId}")
    suspend fun getProductsByEmployee(
        @Path("employeeId") employeeId: Long
    ): Response<List<ProductResponse>>

    @GET("/api/product/search/batch/by/product/{productId}")
    suspend fun getBatchesNamesByProduct(
        @Path("productId") productId: Long
    ): Response<List<String>>

    // MAPS- API
    @GET("geocode/json")
    suspend fun getLocation(
        @Query("address") address: String,
        @Query("key") apiKey: String
    ): GeocodingResponse
}