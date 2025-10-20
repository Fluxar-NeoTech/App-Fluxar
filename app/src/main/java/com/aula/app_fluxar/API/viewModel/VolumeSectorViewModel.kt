package com.aula.app_fluxar.API.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aula.app_fluxar.API.RetrofitClient
import kotlinx.coroutines.launch

class VolumeSectorViewModel : ViewModel() {
    private val _remainingVolume = MutableLiveData<Double?>()
    val remainingVolume: LiveData<Double?> = _remainingVolume

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun getRemainingVolumeBySector(sectorId: Long, employeeId: Long) {
        _isLoading.value = true
        _errorMessage.value = ""
        _remainingVolume.value = null

        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getRemainingVolumeBySector(sectorId, employeeId)

                if (response.isSuccessful) {
                    val volume = response.body()

                    if (volume != null) {
                        _remainingVolume.value = volume
                        _errorMessage.value = ""
                        Log.d("VolumeSector", "Volume restante obtido com sucesso: $volume")
                    } else {
                        val errorMessage = "Volume retornou nulo"
                        _errorMessage.value = errorMessage
                        Log.e("VolumeSector", errorMessage)
                    }
                } else {
                    val errorMessage = "Erro ao buscar volume restante: ${response.code()} - ${response.message()}"
                    _errorMessage.value = errorMessage
                    Log.e("VolumeSector", errorMessage)

                    val errorBody = response.errorBody()?.string()
                    Log.e("VolumeSector", "Error body: $errorBody")
                }
            } catch (e: Exception) {
                val errorMessage = "Erro de conexão: ${e.message ?: "Erro desconhecido"}"
                _errorMessage.value = errorMessage
                Log.e("VolumeSector", errorMessage, e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearResults() {
        _remainingVolume.value = null
        _errorMessage.value = ""
    }
}