package com.aula.app_fluxar.API.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aula.app_fluxar.API.RetrofitClient
import com.aula.app_fluxar.API.model.Profile
import com.aula.app_fluxar.sessionManager.SessionManager
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    private val _profileResult = MutableLiveData<Profile>()
    val profileResult: LiveData<Profile> = _profileResult

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _capacityUpdateResult = MutableLiveData<Boolean>()
    val capacityUpdateResult: LiveData<Boolean> = _capacityUpdateResult

    fun loadProfile() {
        _isLoading.value = true
        _errorMessage.value = ""

        viewModelScope.launch {
            try {
                val userID = SessionManager.getEmployeeId()
                val response = RetrofitClient.instance.getProfileInfos(userID)

                if (response.isSuccessful) {
                    response.body()?.let { profile ->
                        _profileResult.value = profile
                        SessionManager.saveProfile(profile)
                        _errorMessage.value = ""
                        Log.d("ProfileViewModel", "Perfil carregado: ${profile.firstName} - Capacidade: ${profile.maxCapacity}")
                    }
                } else {
                    _errorMessage.value = "Erro ao buscar perfil: ${response.code()} - ${response.message()}"
                    Log.e("ProfileViewModel", "Erro na API: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erro de conexão: ${e.message ?: "Erro desconhecido"}"
                Log.e("ProfileViewModel", "Exceção: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}