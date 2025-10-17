package com.aula.app_fluxar.API.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aula.app_fluxar.API.RetrofitClient
import com.aula.app_fluxar.API.model.CapacitySectorInfos
import kotlinx.coroutines.launch

class CapacitySectorInfosViewModel : ViewModel() {
    private val _capacitySectorInfosResult = MutableLiveData<CapacitySectorInfos?>()
    val capacitySectorInfosResult: LiveData<CapacitySectorInfos?> = _capacitySectorInfosResult

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun getSectorCapacityInfos(sectorId: Long, employeeId: Long) {
        _errorMessage.value = ""
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getSectorCapacityInfos(sectorId, employeeId)

                if (response.isSuccessful) {
                    _capacitySectorInfosResult.value = response.body()
                    Log.d("CapacitySectorInfosViewModel", "Informações de capacidade do setor obtidas com sucesso")
                } else {
                    when (response.code()) {
                        400 -> _errorMessage.value = "Dados inválidos fornecidos"
                        403 -> _errorMessage.value = "Acesso negado"
                        404 -> _errorMessage.value = "Setor não encontrado"
                        500 -> _errorMessage.value = "Erro interno do servidor"
                        504 -> _errorMessage.value = "Timeout do servidor"
                        else -> _errorMessage.value = "Erro: ${response.code()} - ${response.message()}"
                    }
                    Log.e("CapacitySectorInfosViewModel", "Erro na resposta: ${response.code()} - ${response.message()}")
                }
            } catch (e: java.net.SocketTimeoutException) {
                _errorMessage.value = "Timeout: O servidor demorou muito para responder"
                Log.e("CapacitySectorInfosViewModel", "Timeout exception", e)
            } catch (e: java.net.ConnectException) {
                _errorMessage.value = "Não foi possível conectar ao servidor"
                Log.e("CapacitySectorInfosViewModel", "Connect exception", e)
            } catch (e: java.net.UnknownHostException) {
                _errorMessage.value = "Servidor não encontrado. Verifique sua conexão"
                Log.e("CapacitySectorInfosViewModel", "Unknown host exception", e)
            } catch (e: Exception) {
                _errorMessage.value = "Erro: ${e.message ?: "Erro desconhecido"}"
                Log.e("CapacitySectorInfosViewModel", "Exception", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearResults() {
        _capacitySectorInfosResult.value = null
        _errorMessage.value = ""
        _isLoading.value = false
    }
}