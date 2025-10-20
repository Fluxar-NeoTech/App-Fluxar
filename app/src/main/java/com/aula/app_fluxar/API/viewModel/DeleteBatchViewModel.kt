package com.aula.app_fluxar.API.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aula.app_fluxar.API.RetrofitClient
import kotlinx.coroutines.launch

class DeleteBatchViewModel : ViewModel() {
    private val _deleteBatchResult = MutableLiveData<String?>()
    val deleteBatchResult: LiveData<String?> = _deleteBatchResult

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun deleteBatch(batchCode: String) {
        _isLoading.value = true
        _deleteBatchResult.value = null

        viewModelScope.launch {
            try {
                Log.d("DeleteBatch", "Deletando lote: $batchCode")

                val response = RetrofitClient.instance.deleteBatch(batchCode)

                Log.d("DeleteBatch", "Response code: ${response.code()}")
                Log.d("DeleteBatch", "Response message: ${response.message()}")

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    Log.d("DeleteBatch", "Resposta da API: $responseBody")

                    val message = responseBody?.get("message") ?: "Lote deletado com sucesso!"

                    _deleteBatchResult.value = message
                    _errorMessage.value = ""
                    Log.d("DeleteBatch", "Lote deletado com sucesso: $message")
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Sem detalhes"
                    val errorMessage = "Erro ao deletar lote: ${response.code()} - ${response.message()}\nDetalhes: $errorBody"
                    _errorMessage.value = errorMessage
                    Log.e("DeleteBatch", errorMessage)
                }
            } catch (e: Exception) {
                val errorMessage = "Erro de conexão: ${e.message ?: "Erro desconhecido"}"
                _errorMessage.value = errorMessage
                Log.e("DeleteBatch", errorMessage, e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearResults() {
        _deleteBatchResult.value = null
        _errorMessage.value = ""
    }
}