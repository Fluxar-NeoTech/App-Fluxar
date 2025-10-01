package com.aula.app_fluxar.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import com.aula.app_fluxar.R
import com.aula.app_fluxar.databinding.ActivityLoginBinding
import com.aula.app_fluxar.API.viewModel.LoginViewModel
import com.aula.app_fluxar.API.viewModel.ProfileViewModel

class Login : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        viewModel.errorMessage.observe(this, Observer { error ->
            if (error.isNotEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                binding.progressBar.visibility = View.GONE
                binding.btEntrarLogin.isEnabled = true
            }
        })

        viewModel.isLoading.observe(this, Observer { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btEntrarLogin.isEnabled = !isLoading
        })

        viewModel.navigateToMain.observe(this, Observer { shouldNavigate ->
            if (shouldNavigate) {
                loadProfileAndNavigate()
            }
        })

        profileViewModel.profileResult.observe(this, Observer { profile ->
            if (profile != null) {
                Log.d("Login", "Profile carregado com sucesso: ${profile.firstName}")
                navigateToMainActivity()
            }
        })

        profileViewModel.errorMessage.observe(this, Observer { error ->
            if (error.isNotEmpty()) {
                Toast.makeText(this, "Erro ao carregar perfil: $error", Toast.LENGTH_SHORT).show()
                navigateToMainActivity()
            }
        })
    }

    private fun setupListeners() {
        binding.btEntrarLogin.setOnClickListener {
            val email = binding.inputEmailGestor.text.toString().trim()
            val senha = binding.inputSenhaGestor.text.toString().trim()

            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Por favor, preencha todos os campos!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.login(email, senha)
        }
    }

    private fun loadProfileAndNavigate() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btEntrarLogin.isEnabled = false

        profileViewModel.loadProfile()
    }

    private fun navigateToMainActivity() {
        val employee = viewModel.getUser()
        if (employee != null) {
            if (employee.role == 'A') {
                Toast.makeText(this, "Você não tem permissão para acessar este aplicativo.", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
                binding.btEntrarLogin.isEnabled = true
            } else {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        } else {
            Toast.makeText(this, "Erro ao carregar dados do usuário", Toast.LENGTH_SHORT).show()
            binding.progressBar.visibility = View.GONE
            binding.btEntrarLogin.isEnabled = true
        }

        viewModel.onNavigationComplete()
    }
}