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
    private val _getBatchesResult = MutableLiveData<List<Batch>?>()
    val getBatchesResult: LiveData<List<Batch>?> = _getBatchesResult

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _noBatchesFound = MutableLiveData<Boolean>()
    val noBatchesFound: LiveData<Boolean> = _noBatchesFound

    fun getBatches(unitID: Long, sectorID: Long) {
        _isLoading.value = true
        _errorMessage.value = ""
        _noBatchesFound.value = false

        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getBatchesEmployee(unitID, sectorID)

                if (response.isSuccessful) {
                    val batches = response.body()
                    if (!batches.isNullOrEmpty()) {
                        _getBatchesResult.value = batches
                        _errorMessage.value = ""
                        _noBatchesFound.value = false
                        Log.d("GetBatches", "Lotes retornados: ${batches.size}")
                    } else {
                        _getBatchesResult.value = emptyList()
                        _noBatchesFound.value = true
                        Log.d("GetBatches", "Lista de lotes vazia")
                    }
                } else {
                    if (response.code() == 400) {
                        Log.w("GetBatches", "Status 400 - Nenhum lote encontrado")
                        _getBatchesResult.value = emptyList()
                        _noBatchesFound.value = true
                        _errorMessage.value = ""
                    } else {
                        _errorMessage.value = "Erro ao buscar lotes: ${response.code()} - ${response.message()}"
                        _noBatchesFound.value = false
                        Log.e("GetBatches", "Erro na API: ${response.code()} - ${response.message()}")
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erro de conexão: ${e.message ?: "Erro desconhecido"}"
                _noBatchesFound.value = false
                Log.e("GetBatches", "Exceção: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}