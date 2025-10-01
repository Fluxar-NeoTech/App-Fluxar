package com.aula.app_fluxar.sessionManager

import android.util.Log
import com.aula.app_fluxar.API.model.Employee
import com.aula.app_fluxar.API.model.Profile

object SessionManager {
    private var currentEmployee: Employee? = null
    private var currentProfile: Profile? = null

    fun saveLoginData(employee: Employee) {
        currentEmployee = employee
        Log.d("SessionManager", "Login salvo - Employee ID: ${employee.id}")
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
        Log.d("SessionManager", "Sessão limpa")
    }

}