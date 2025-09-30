package com.aula.app_fluxar.API.viewModel

import android.util.Log
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

    private val _updateSuccess = MutableLiveData<Boolean>()
    val updateSuccess: LiveData<Boolean> = _updateSuccess

    fun updateCapacityStock(largura: Double, altura: Double, comprimento: Double, setorId: Long, unidadeId: Long) {
        _errorMessage.value = ""
        _updateSuccess.value = false

        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.addCapacityStock(
                    CapacityStockRequest(largura, altura, comprimento, setorId, unidadeId)
                )

                if (response.isSuccessful) {
                    _capacityStockResult.value = response.body()
                    _updateSuccess.value = true

                    Log.d("CapacityStockViewModel", "Capacidade atualizada com sucesso")
                } else {
                    when (response.code()) {
                        403 -> _errorMessage.value = "Acesso negado"
                        404 -> _errorMessage.value = "Recurso não encontrado"
                        500 -> _errorMessage.value = "Erro interno do servidor"
                        504 -> _errorMessage.value = "Timeout do servidor"
                        else -> _errorMessage.value = "Erro: ${response.code()} - ${response.message()}"
                    }
                    _updateSuccess.value = false
                }
            } catch (e: java.net.SocketTimeoutException) {
                _errorMessage.value = "Timeout: O servidor demorou muito para responder"
                _updateSuccess.value = false
            } catch (e: java.net.ConnectException) {
                _errorMessage.value = "Não foi possível conectar ao servidor"
                _updateSuccess.value = false
            } catch (e: java.net.UnknownHostException) {
                _errorMessage.value = "Servidor não encontrado. Verifique sua conexão"
                _updateSuccess.value = false
            } catch (e: Exception) {
                _errorMessage.value = "Erro: ${e.message ?: "Erro desconhecido"}"
                _updateSuccess.value = false
            }
        }
    }

    fun clearResult() {
        _capacityStockResult.value = null
        _errorMessage.value = ""
        _updateSuccess.value = false
    }
}