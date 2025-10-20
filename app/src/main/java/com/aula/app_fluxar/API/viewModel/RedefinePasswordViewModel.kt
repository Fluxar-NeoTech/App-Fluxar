package com.aula.app_fluxar.API.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aula.app_fluxar.API.RetrofitClient
import com.aula.app_fluxar.API.model.EmailRequest
import kotlinx.coroutines.launch

class RedefinePasswordViewModel : ViewModel() {
    private val _redefinePasswordResult = MutableLiveData<Map<String, String>?>()
    val redefinePasswordResult: LiveData<Map<String, String>?> = _redefinePasswordResult

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _successMessage = MutableLiveData<String>()
    val successMessage: LiveData<String> = _successMessage

    fun redefinePassword(email: String) {
        _errorMessage.value = ""
        _successMessage.value = ""
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val emailRequest = EmailRequest(email)
                val response = RetrofitClient.instance.redefinePassword(emailRequest)

                if (response.isSuccessful) {
                    _redefinePasswordResult.value = response.body()
                    _successMessage.value = "Email de redefinição enviado com sucesso!"
                    Log.d("RedefinePasswordViewModel", "Email de redefinição enviado com sucesso")
                } else {
                    when (response.code()) {
                        400 -> _errorMessage.value = "Email inválido ou mal formatado"
                        401 -> _errorMessage.value = "Email não encontrado no sistema"
                        500 -> _errorMessage.value = "Erro interno do servidor"
                        503 -> _errorMessage.value = "Serviço de email indisponível"
                        504 -> _errorMessage.value = "Timeout do servidor"
                        else -> _errorMessage.value = "Erro: ${response.code()} - ${response.message()}"
                    }
                    Log.e("RedefinePasswordViewModel", "Erro na resposta: ${response.code()} - ${response.message()}")
                }
            } catch (e: java.net.SocketTimeoutException) {
                _errorMessage.value = "Timeout: O servidor demorou muito para responder"
                Log.e("RedefinePasswordViewModel", "Timeout exception", e)
            } catch (e: java.net.ConnectException) {
                _errorMessage.value = "Não foi possível conectar ao servidor"
                Log.e("RedefinePasswordViewModel", "Connect exception", e)
            } catch (e: java.net.UnknownHostException) {
                _errorMessage.value = "Servidor não encontrado. Verifique sua conexão"
                Log.e("RedefinePasswordViewModel", "Unknown host exception", e)
            } catch (e: Exception) {
                _errorMessage.value = "Erro: ${e.message ?: "Erro desconhecido"}"
                Log.e("RedefinePasswordViewModel", "Exception", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearResults() {
        _redefinePasswordResult.value = null
        _errorMessage.value = ""
        _successMessage.value = ""
        _isLoading.value = false
    }
}