// GetDimensionsViewModel.kt
package com.aula.app_fluxar.API.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aula.app_fluxar.API.RetrofitClient
import com.aula.app_fluxar.API.model.Dimensions
import kotlinx.coroutines.launch

class GetDimensionsViewModel : ViewModel() {

    private val _dimensionsResult = MutableLiveData<Dimensions?>()
    val dimensionsResult: LiveData<Dimensions?> = _dimensionsResult

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun getDimensionsByUnitId(unitId: Long) {
        _isLoading.value = true
        _errorMessage.value = ""
        _dimensionsResult.value = null

        viewModelScope.launch {
            try {
                Log.d("GetDimensionsViewModel", "Buscando dimensões para unitID: $unitId")

                val response = RetrofitClient.instance.getDimensionsByUnitID(unitId)

                if (response.isSuccessful) {
                    val dimensions = response.body()
                    _dimensionsResult.value = dimensions
                    Log.d("GetDimensionsViewModel", "Dimensões recebidas: $dimensions")
                } else {
                    val errorMsg = "Erro ao buscar dimensões: ${response.code()} - ${response.message()}"
                    _errorMessage.value = errorMsg
                    Log.e("GetDimensionsViewModel", errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = "Erro de conexão: ${e.message}"
                _errorMessage.value = errorMsg
                Log.e("GetDimensionsViewModel", errorMsg, e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearResults() {
        _dimensionsResult.value = null
        _errorMessage.value = ""
    }
}