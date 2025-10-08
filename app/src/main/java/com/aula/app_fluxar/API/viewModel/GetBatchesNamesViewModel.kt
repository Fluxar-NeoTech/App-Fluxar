package com.aula.app_fluxar.API.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aula.app_fluxar.API.RetrofitClient
import kotlinx.coroutines.launch

class GetBatchesNamesViewModel : ViewModel() {
    private val _getBatchesNamesResult = MutableLiveData<List<String>>()
    val getBatchesNamesResult: LiveData<List<String>> = _getBatchesNamesResult

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun getBatchesNamesByProduct(productId: Long) {
        _errorMessage.value = ""

        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getBatchesNamesByProduct(productId)

                if (response.isSuccessful) {
                    _getBatchesNamesResult.value = response.body()
                    _errorMessage.value = ""
                    Log.d("GetBatchesNames", "Lotes retornados: ${response.body()?.size ?: 0}")
                } else {
                    _errorMessage.value = "Erro ao buscar lotes: ${response.code()} - ${response.message()}"
                    Log.e("GetBatchesNames", "Erro na API: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erro de conexão: ${e.message ?: "Erro desconhecido"}"
                Log.e("GetBatchesNames", "Exceção: ${e.message}", e)
            }
        }
    }
}