package com.aula.app_fluxar.API.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aula.app_fluxar.API.RetrofitClient
import kotlinx.coroutines.launch

class VolumeUsedSectorViewModel : ViewModel() {
    private val _usedVolume = MutableLiveData<Double?>()
    val usedVolume: LiveData<Double?> = _usedVolume

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun getUsedVolumeBySector(sectorId: Long, employeeId: Long) {
        _isLoading.value = true
        _errorMessage.value = ""
        _usedVolume.value = null

        viewModelScope.launch {
            try {
                Log.d("VolumeUsedSectorViewModel", "🔍 Buscando volume utilizado - sectorId: $sectorId, employeeId: $employeeId")

                val response = RetrofitClient.instance.getUsedVolumeBySector(sectorId, employeeId)

                if (response.isSuccessful) {
                    val volume = response.body()

                    if (volume != null) {
                        _usedVolume.value = volume
                        _errorMessage.value = ""
                        Log.d("VolumeUsedSectorViewModel", "✅ Volume utilizado obtido: $volume m³")
                    } else {
                        val errorMessage = "Volume utilizado retornou nulo"
                        _errorMessage.value = errorMessage
                        Log.e("VolumeUsedSectorViewModel", errorMessage)
                    }
                } else {
                    val errorMessage = "Erro ao buscar volume utilizado: ${response.code()} - ${response.message()}"
                    _errorMessage.value = errorMessage
                    Log.e("VolumeUsedSectorViewModel", errorMessage)

                    val errorBody = response.errorBody()?.string()
                    Log.e("VolumeUsedSectorViewModel", "Error body: $errorBody")
                }
            } catch (e: Exception) {
                val errorMessage = "Erro de conexão: ${e.message ?: "Erro desconhecido"}"
                _errorMessage.value = errorMessage
                Log.e("VolumeUsedSectorViewModel", errorMessage, e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearResults() {
        _usedVolume.value = null
        _errorMessage.value = ""
    }
}