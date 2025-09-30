package com.aula.app_fluxar.API.viewModel

import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aula.app_fluxar.API.RetrofitClient
import com.aula.app_fluxar.API.model.CapacityStockRequest
import com.aula.app_fluxar.API.model.CapacityStockResponse
import kotlinx.coroutines.launch

class CapacityStockViewModel : ViewModel() {
    private val _capacityStockResult = MutableLiveData<CapacityStockResponse?>()
    val capacityStockResult: LiveData<CapacityStockResponse?> = _capacityStockResult

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun updateCapacityStock(largura: Double, altura: Double, comprimento: Double, setorId: Long, unidadeId: Long) {
        _errorMessage.value = ""

        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.addCapacityStock(
                    CapacityStockRequest(largura, altura, comprimento, setorId, unidadeId)
                )

                if (response.isSuccessful) {
                    _capacityStockResult.value = response.body()
                } else {
                    when (response.code()) {
                        403 -> _errorMessage.value = "Email ou senha incorretos"
                        404 -> _errorMessage.value = "Requisição não encontrada"
                        500 -> _errorMessage.value = "Erro interno do servidor"
                        504 -> _errorMessage.value = "Gateway Timeout"
                        else -> _errorMessage.value =
                            "Erro: ${response.code()} - ${response.message()}"
                    }
                }
            } catch (e: java.net.SocketTimeoutException) {
                _errorMessage.value = "Timeout: O servidor demorou muito para responder"
            } catch (e: java.net.ConnectException) {
                _errorMessage.value = "Não foi possível conectar ao servidor"
            } catch (e: java.net.UnknownHostException) {
                _errorMessage.value = "Servidor não encontrado. Verifique sua conexão"
            } catch (e: Exception) {
                _errorMessage.value = "Erro: ${e.message ?: "Erro desconhecido"}"
            }
        }
    }

    fun clearResult() {
        _capacityStockResult.value = null
        _errorMessage.value = ""
    }
}
