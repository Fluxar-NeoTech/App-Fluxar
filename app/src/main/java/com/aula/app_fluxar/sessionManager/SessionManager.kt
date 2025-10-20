package com.aula.app_fluxar.sessionManager

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.util.Log
import com.aula.app_fluxar.API.model.Employee
import com.aula.app_fluxar.API.model.Profile

object SessionManager {
    private var currentEmployee: Employee? = null
    private var currentProfile: Profile? = null

    private fun getSharedPreferences(): android.content.SharedPreferences {
        val context = com.aula.app_fluxar.AppApplication.instance.applicationContext
        return context.getSharedPreferences("app_fluxar_prefs", MODE_PRIVATE)
    }

    fun saveLoginData(employee: Employee) {
        currentEmployee = employee
        Log.d("SessionManager", "Login salvo - Employee ID: ${employee.id}")
        employee.token?.let { token ->
            saveAuthToken(token)
        }
    }

    fun saveAuthToken(token: String) {
        val sharedPref = getSharedPreferences()
        sharedPref.edit().putString("auth_token", token).apply()
        Log.d("SessionManager", "Token salvo: ${token.take(20)}...")
    }

    fun getAuthToken(): String? {
        val sharedPref = getSharedPreferences()
        val token = sharedPref.getString("auth_token", null)
        Log.d("SessionManager", "Token recuperado: ${token?.take(20)}...")
        return token
    }

    fun clearAuthToken() {
        val sharedPref = getSharedPreferences()
        sharedPref.edit().remove("auth_token").apply()
        Log.d("SessionManager", "Token limpo")
    }

    fun saveProfile(profile: Profile) {
        currentProfile = profile
        Log.d("SessionManager", "Perfil salvo - Nome: ${profile.firstName}")
    }

    fun getEmployeeId(): Long {
        return currentEmployee?.id ?: throw IllegalStateException("Usuário não está logado")
    }

    fun getCurrentProfile(): Profile? = currentProfile

    fun clear() {
        currentEmployee = null
        currentProfile = null
        clearAuthToken()
        Log.d("SessionManager", "Sessão completamente limpa")
    }
}