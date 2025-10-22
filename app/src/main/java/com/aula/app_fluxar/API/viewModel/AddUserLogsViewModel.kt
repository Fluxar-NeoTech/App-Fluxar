package com.aula.app_fluxar.API.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aula.app_fluxar.API.LogsRetrofitClient
import com.aula.app_fluxar.API.model.UserLogRequest
import kotlinx.coroutines.launch

class AddUserLogsViewModel : ViewModel() {
    private val _addLogsResult = MutableLiveData<String?>()
    val addLogsResult: LiveData<String?> = _addLogsResult

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun addUserLogs(userLogRequest: UserLogRequest) {
        _errorMessage.value = ""
        _addLogsResult.value = null

        viewModelScope.launch {
            try {
                val response = LogsRetrofitClient.instance.addUserLog(userLogRequest)

                if (response.isSuccessful) {
                    val message = if (response.body() != null) {
                        response.body()?.action ?: "Log adicionado com sucesso!"
                    } else {
                        "Log adicionado com sucesso!"
                    }

                    _addLogsResult.value = message
                    _errorMessage.value = ""

                    Log.d("AddLogs", "$message")
                } else {
                    val errorMessage = "Erro ao adicionar log: ${response.code()} - ${response.message()}"
                    _errorMessage.value = errorMessage
                    Log.e("AddLogs", errorMessage)

                    val errorBody = response.errorBody()?.string()
                    Log.e("AddLogs", "Error body: $errorBody")
                }
            } catch (e: Exception) {
                val errorMessage = "Erro de conexão: ${e.message ?: "Erro desconhecido"}"
                _errorMessage.value = errorMessage
                Log.e("AddLogs", errorMessage, e)
            }
        }
    }
}