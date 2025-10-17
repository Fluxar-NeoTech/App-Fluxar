package com.aula.app_fluxar.API.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aula.app_fluxar.API.RetrofitClient
import com.aula.app_fluxar.API.model.StockHistory
import kotlinx.coroutines.launch

class GetStockHistoryViewModel : ViewModel() {
    private val _getStockHistoryResult = MutableLiveData<StockHistory?>()
    val getStockHistoryResult: LiveData<StockHistory?> = _getStockHistoryResult

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun getStockHistory(unitId: Long) {
        _errorMessage.value = ""
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getStockHistory(unitId)

                if (response.isSuccessful) {
                    _getStockHistoryResult.value = response.body()
                    _errorMessage.value = ""
                    Log.d("GetStockHistory", "Histórico de estoque retornado com sucesso")
                } else {
                    _errorMessage.value = "Erro ao buscar histórico de estoque: ${response.code()} - ${response.message()}"
                    Log.e("GetStockHistory", "Erro na API: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erro de conexão: ${e.message ?: "Erro desconhecido"}"
                Log.e("GetStockHistory", "Exceção: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}