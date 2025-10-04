package com.aula.app_fluxar.API.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aula.app_fluxar.API.RetrofitClient
import com.aula.app_fluxar.API.model.ProductResponse
import kotlinx.coroutines.launch

class GetProductsViewModel : ViewModel() {
    private val _getProductsResult = MutableLiveData<List<ProductResponse>>()
    val getProductsResult: LiveData<List<ProductResponse>> = _getProductsResult

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun getProductsByEmployee(employeeId: Long) {
        _errorMessage.value = ""

        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getProductsByEmployee(employeeId)

                if (response.isSuccessful) {
                    _getProductsResult.value = response.body()
                    _errorMessage.value = ""
                    Log.d("GetProducts", "Produtos retornados: ${response.body()?.size ?: 0}")
                } else {
                    _errorMessage.value = "Erro ao buscar produtos: ${response.code()} - ${response.message()}"
                    Log.e("GetProducts", "Erro na API: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erro de conexão: ${e.message ?: "Erro desconhecido"}"
                Log.e("GetProducts", "Exceção: ${e.message}", e)
            }
        }
    }
}