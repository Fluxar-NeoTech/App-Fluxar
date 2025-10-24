package com.aula.app_fluxar.API.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aula.app_fluxar.API.RetrofitClient
import com.aula.app_fluxar.API.model.UnitInfos
import kotlinx.coroutines.launch


class GetUnitsViewModel : ViewModel() {
    private val _getUnitsResult = MutableLiveData<List<UnitInfos>>()
    val getUnitsResult: LiveData<List<UnitInfos>> = _getUnitsResult

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun getUnits(industryID: Long) {
        _errorMessage.value = ""
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getUnitsByIndustryID(industryID)

                if (response.isSuccessful) {
                    _getUnitsResult.value = response.body()
                    _errorMessage.value = ""
                    Log.d("GetUnits", "Unidades retornadas: ${response.body()}")
                } else {
                    _errorMessage.value = "Erro ao buscar unidades: ${response.code()} - ${response.message()}"
                    Log.e("GetUnits", "Erro na API: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erro de conexão: ${e.message ?: "Erro desconhecido"}"
                Log.e("GetUnits", "Exceção: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}