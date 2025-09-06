package com.aula.app_fluxar.viewModel

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aula.app_fluxar.API.model.Employee
import com.aula.app_fluxar.API.model.LoginRequest
import com.aula.app_fluxar.RetrofitClient
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val _loginResult = MutableLiveData<Employee?>()
    val loginResult: LiveData<Employee?> = _loginResult

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // LiveData para controlar quando navegar para a MainActivity
    private val _navigateToMain = MutableLiveData<Boolean>()
    val navigateToMain: LiveData<Boolean> = _navigateToMain

    fun login(email: String, senha: String) {
        _isLoading.value = true
        _errorMessage.value = ""
        _navigateToMain.value = false

        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.login(LoginRequest(email, senha))

                if(response.isSuccessful) {
                    _loginResult.value = response.body()
                    _errorMessage.value = ""
                    // Indica que pode navegar para a MainActivity
                    _navigateToMain.value = true
                } else {
                    when (response.code()) {
                        403 -> _errorMessage.value = "Email ou senha incorretos"
                        404 -> _errorMessage.value = "Requisição não encontrada"
                        500 -> _errorMessage.value = "Erro interno do servidor"
                        504 -> _errorMessage.value = "Gateway Timeout"
                        else -> _errorMessage.value = "Erro: ${response.code()} - ${response.message()}"
                    }
                    _navigateToMain.value = false
                }
            } catch (e: java.net.SocketTimeoutException) {
                _errorMessage.value = "Timeout: O servidor demorou muito para responder"
                _navigateToMain.value = false
            } catch (e: java.net.ConnectException) {
                _errorMessage.value = "Não foi possível conectar ao servidor"
                _navigateToMain.value = false
            } catch (e: java.net.UnknownHostException) {
                _errorMessage.value = "Servidor não encontrado. Verifique sua conexão"
                _navigateToMain.value = false
            } catch (e: Exception) {
                _errorMessage.value = "Erro: ${e.message ?: "Erro desconhecido"}"
                _navigateToMain.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Método para resetar a navegação
    fun onNavigationComplete() {
        _navigateToMain.value = false
    }
}