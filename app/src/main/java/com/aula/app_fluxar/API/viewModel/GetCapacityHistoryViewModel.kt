package com.aula.app_fluxar.API.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aula.app_fluxar.API.RetrofitClient
import com.aula.app_fluxar.API.model.CapacityHistory
import kotlinx.coroutines.launch

class GetCapacityHistoryViewModel : ViewModel() {
    private val _getCapacityHistoryResult = MutableLiveData<CapacityHistory>()
    val getCapacityHistoryResult: LiveData<CapacityHistory> = _getCapacityHistoryResult

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun getCapacityHistory(unitId: Long) {
        _errorMessage.value = ""
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getCapacityHistory(unitId)

                if (response.isSuccessful) {
                    _getCapacityHistoryResult.value = response.body()
                    _errorMessage.value = ""
                    Log.d("GetCapacityHistory", "Histórico de capacidade retornado com sucesso")
                } else {
                    _errorMessage.value = "Erro ao buscar histórico de capacidade: ${response.code()} - ${response.message()}"
                    Log.e("GetCapacityHistory", "Erro na API: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erro de conexão: ${e.message ?: "Erro desconhecido"}"
                Log.e("GetCapacityHistory", "Exceção: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}