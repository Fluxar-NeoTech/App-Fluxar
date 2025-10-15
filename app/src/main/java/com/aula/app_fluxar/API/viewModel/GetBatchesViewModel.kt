package com.aula.app_fluxar.API.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aula.app_fluxar.API.RetrofitClient
import com.aula.app_fluxar.API.model.Batch
import com.aula.app_fluxar.API.model.Unit
import kotlinx.coroutines.launch

class GetBatchesViewModel : ViewModel() {
    private val _getBatchesResult = MutableLiveData<List<Batch>>()
    val getBatchesResult: LiveData<List<Batch>> = _getBatchesResult

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun getBatches(unitID: Long, sectorID: Long) {
        _errorMessage.value = ""

        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getBatchesEmployee(unitID, sectorID)

                if (response.isSuccessful) {
                    _getBatchesResult.value = response.body()
                    _errorMessage.value = ""
                    Log.d("GetBatches", "Lotes retornados: ${response.body()}")
                } else {
                    _errorMessage.value = "Erro ao buscar lotes: ${response.code()} - ${response.message()}"
                    Log.e("GetBatches", "Erro na API: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erro de conexão: ${e.message ?: "Erro desconhecido"}"
                Log.e("GetBatches", "Exceção: ${e.message}", e)
            }
        }
    }
}