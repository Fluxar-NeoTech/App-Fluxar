package com.aula.app_fluxar.API

import android.util.Log
import com.aula.app_fluxar.API.service.APIService
import com.aula.app_fluxar.sessionManager.SessionManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "https://api-fluxar.onrender.com/"
    private const val TIMEOUT_SECONDS = 30L

    private var authToken: String? = null

    init {
        loadSavedToken()
    }

    private fun loadSavedToken() {
        val savedToken = SessionManager.getAuthToken()
        authToken = savedToken?.let { "Bearer $it" }
    }

    private class AuthInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest = chain.request()

            val token = authToken

            if (token == null) {
                Log.d("AuthInterceptor", "Sem token, prosseguindo sem header Authorization")
                return chain.proceed(originalRequest)
            }

            val requestWithToken = originalRequest.newBuilder()
                .header("Authorization", token)
                .build()

            Log.d("AuthInterceptor", "Header Authorization adicionado")
            return chain.proceed(requestWithToken)
        }
    }

    fun setAuthToken(token: String?) {
        authToken = token
        Log.d("RetrofitClient", "Token definido no RetrofitClient: ${token?.take(20)}...")
    }

    val instance: APIService by lazy {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(AuthInterceptor())
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(APIService::class.java)
    }
}