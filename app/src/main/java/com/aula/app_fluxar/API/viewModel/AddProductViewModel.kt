package com.aula.app_fluxar.API.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aula.app_fluxar.API.RetrofitClient
import com.aula.app_fluxar.API.model.ProductRequest
import kotlinx.coroutines.launch

class AddProductViewModel : ViewModel() {
    private val _addProductResult = MutableLiveData<String>()
    val addProductResult: LiveData<String> = _addProductResult

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun addProduct(productRequest: ProductRequest) {
        _isLoading.value = true
        _errorMessage.value = ""

        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.addProduct(productRequest)

                if (response.isSuccessful) {
                    _addProductResult.value = response.body()
                    _errorMessage.value = ""
                    Log.d("AddProduct", "Produto adicionado com sucesso: ${response.body()}")
                } else {
                    val errorMessage = "Erro ao adicionar produto: ${response.code()} - ${response.message()}"
                    _errorMessage.value = errorMessage
                    Log.e("AddProduct", errorMessage)
                }
            } catch (e: Exception) {
                val errorMessage = "Erro de conexão: ${e.message ?: "Erro desconhecido"}"
                _errorMessage.value = errorMessage
                Log.e("AddProduct", errorMessage, e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}