package com.aula.app_fluxar.API.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aula.app_fluxar.API.RetrofitClient
import com.aula.app_fluxar.API.model.UpdatePhotoRequest
import kotlinx.coroutines.launch

class UpdateFotoViewModel : ViewModel() {
    private val _updateFotoResult = MutableLiveData<Map<String, String>>()
    val updateFotoResult: LiveData<Map<String, String>> = _updateFotoResult

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun updatePhoto(employee: UpdatePhotoRequest) {
        _isLoading.value = true
        _errorMessage.value = ""

        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.updatePhoto(employee)

                if (response.isSuccessful) {
                    _updateFotoResult.value = response.body()
                    _errorMessage.value = ""
                    Log.d("UpdateFoto", "Foto atualizada com sucesso: ${response.body()}")
                } else {
                    _errorMessage.value = "Erro ao atualizar foto: ${response.code()} - ${response.message()}"
                    Log.e("UpdateFoto", "Erro na API: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erro de conexão: ${e.message ?: "Erro desconhecido"}"
                Log.e("UpdateFoto", "Exceção: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}