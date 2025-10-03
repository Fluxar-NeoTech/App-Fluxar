package com.aula.app_fluxar.API.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aula.app_fluxar.API.RetrofitClient
import com.aula.app_fluxar.API.model.BatchRequest
import kotlinx.coroutines.launch

class AddBatchViewModel : ViewModel() {
    private val _addBatchResult = MutableLiveData<String>()
    val addBatchResult: LiveData<String> = _addBatchResult

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun addBatch(batchRequest: BatchRequest) {
        _isLoading.value = true
        _errorMessage.value = ""

        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.addBatch(batchRequest)

                if (response.isSuccessful) {
                    _addBatchResult.value = "Lote adicionado com sucesso!"
                    _errorMessage.value = ""
                    Log.d("AddBatch", "Lote adicionado com sucesso: ${response.body()}")
                } else {
                    val errorMessage = "Erro ao adicionar lote: ${response.code()} - ${response.message()}"
                    _errorMessage.value = errorMessage
                    Log.e("AddBatch", errorMessage)
                }
            } catch (e: Exception) {
                val errorMessage = "Erro de conexão: ${e.message ?: "Erro desconhecido"}"
                _errorMessage.value = errorMessage
                Log.e("AddBatch", errorMessage, e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}