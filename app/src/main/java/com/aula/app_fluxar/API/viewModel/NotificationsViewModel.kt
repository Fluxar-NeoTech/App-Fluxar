package com.aula.app_fluxar.API.viewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.aula.app_fluxar.API.NotificationsRetrofitClient
import kotlinx.coroutines.Dispatchers
import androidx.lifecycle.viewModelScope
import com.aula.app_fluxar.API.model.NotificationRequest
import com.aula.app_fluxar.API.model.NotificationResponse
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.Response
import kotlin.math.log

class NotificationsViewModel : ViewModel() {
    private val _notification = MutableLiveData<NotificationResponse?>()
    val notification: LiveData<NotificationResponse?> = _notification

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun fetchNotification(notificationRequest: NotificationRequest) {
        _isLoading.value = true
        _errorMessage.value = ""
        _notification.value = null

        viewModelScope.launch {
            try {
                Log.d("NotificationsViewModel", "📡 Enviando requisição de notificação...")

                val response: Response<NotificationResponse> =
                    NotificationsRetrofitClient.instance.getNotification(notificationRequest)

                if (response.isSuccessful) {
                    val notificationResponse = response.body()
                    _notification.value = notificationResponse
                    _errorMessage.value = ""
                    Log.d("NotificationsViewModel", "✅ Notificação recebida")

                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = if (!errorBody.isNullOrEmpty()) {
                        try {
                            val json = JSONObject(errorBody)
                            json.optString("detail", "Erro desconhecido no servidor.")
                        } catch (e: Exception) {
                            "Erro inesperado (${response.code()})"
                        }
                    } else {
                        "Erro inesperado (${response.code()})"
                    }

                    _errorMessage.value = errorMsg
                    Log.e("NotificationsViewModel", "❌ Erro ao buscar notificação: $errorMsg")
                }

            } catch (e: Exception) {
                val errorMsg = "Erro de conexão: ${e.message ?: "Erro desconhecido"}"
                _errorMessage.value = errorMsg
                Log.e("NotificationsViewModel", errorMsg, e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}