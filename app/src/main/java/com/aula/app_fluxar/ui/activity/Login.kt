package com.aula.app_fluxar.ui.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
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
import com.aula.app_fluxar.API.viewModel.RedefinePasswordViewModel
import com.google.android.material.textfield.TextInputEditText

class Login : AppCompatActivity() {
    private val origin = "APP"
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()
    private val redefinePasswordViewModel: RedefinePasswordViewModel by viewModels()

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
        // Observers do Login
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

        redefinePasswordViewModel.successMessage.observe(this, Observer { message ->
            if (message.isNotEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                Log.d("Login", "Email de redefinição enviado: $message")
                Toast.makeText(this, "Você receberá um e-mail com informações para a redefinição de senha.", Toast.LENGTH_LONG).show()
            }
        })

        redefinePasswordViewModel.errorMessage.observe(this, Observer { error ->
            if (error.isNotEmpty()) {
                Toast.makeText(this, "Erro: $error", Toast.LENGTH_LONG).show()
                Log.e("Login", "Erro ao enviar email: $error")
            }
        })

        redefinePasswordViewModel.isLoading.observe(this, Observer { isLoading ->
            if (isLoading) {
                Log.d("Login", "Enviando email de redefinição...")
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

            viewModel.login(email, senha, origin)
        }

        binding.redefinirSenha!!.setOnClickListener {
            openRedefinePasswordDialog()
        }
    }

    private fun openRedefinePasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.pop_up_redefinir_senha, null)
        val emailInput: TextInputEditText = dialogView.findViewById(R.id.inputEmailRedefinirSenha)
        val positiveButton = dialogView.findViewById<Button>(R.id.redefinirSenhaS)
        val negativeButton = dialogView.findViewById<Button>(R.id.redefinirSenhaN)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        positiveButton.setOnClickListener {
            val email = emailInput.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Preencha o campo de e-mail!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isValidEmail(email)) {
                Toast.makeText(this, "Por favor, insira um email válido!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            redefinePasswordViewModel.redefinePassword(email)
            dialog.dismiss()
        }

        negativeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
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

    override fun onDestroy() {
        super.onDestroy()
        redefinePasswordViewModel.clearResults()
        viewModel.clearResults()
    }
}