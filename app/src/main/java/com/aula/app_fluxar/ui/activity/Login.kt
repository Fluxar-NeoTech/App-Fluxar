package com.aula.app_fluxar.ui.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView
import com.aula.app_fluxar.API.model.UserLogRequest
import com.aula.app_fluxar.API.viewModel.AddUserLogsViewModel
import com.aula.app_fluxar.R
import com.aula.app_fluxar.databinding.ActivityLoginBinding
import com.aula.app_fluxar.API.viewModel.LoginViewModel
import com.aula.app_fluxar.API.viewModel.ProfileViewModel
import com.aula.app_fluxar.API.viewModel.RedefinePasswordViewModel
import com.aula.app_fluxar.sessionManager.SessionManager
import com.google.android.material.textfield.TextInputEditText

class Login : AppCompatActivity() {
    private val origin = "APP"
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()
    private val redefinePasswordViewModel: RedefinePasswordViewModel by viewModels()
    private val addUserLogsViewModel: AddUserLogsViewModel by viewModels()

    private lateinit var loginLoadingLayout: LinearLayout
    private lateinit var loginErrorLayout: LinearLayout
    private lateinit var loginContentLayout: NestedScrollView
    private lateinit var loginLoadingText: TextView
    private lateinit var loginErrorText: TextView
    private lateinit var loginRetryButton: Button

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

        initStateViews()
        setupObservers()
        setupListeners()
        showLoginContentState()
    }

    private fun initStateViews() {
        loginLoadingLayout = findViewById(R.id.loginLoadingLayout)
        loginErrorLayout = findViewById(R.id.loginErrorLayout)
        loginContentLayout = findViewById(R.id.loginContentLayout)
        loginLoadingText = findViewById(R.id.loginLoadingText)
        loginErrorText = findViewById(R.id.loginErrorText)
        loginRetryButton = findViewById(R.id.loginRetryButton)

        loginRetryButton.setOnClickListener {
            showLoginContentState()
        }
    }

    private fun setupObservers() {
        viewModel.errorMessage.observe(this) { error ->
            if (error.isNotEmpty()) {
                Log.e("Login", "Erro no login: $error")
                showLoginErrorState("Erro ao fazer login: $error")
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                showLoginLoadingState("Fazendo login...")
            }
        }

        viewModel.navigateToMain.observe(this) { shouldNavigate ->
            if (shouldNavigate) {
                loadProfileAndNavigate()
            }
        }

        profileViewModel.profileResult.observe(this) { profile ->
            if (profile != null) {
                Log.d("Login", "✅ Profile carregado com sucesso: ${profile.firstName}")

                val action = "Usuário realizou login"
                addUserLogsViewModel.addUserLogs(UserLogRequest(SessionManager.getEmployeeId(), action))

                navigateToMainActivity()
            } else {
                Log.e("Login", "❌ Profile é null")
                showLoginErrorState("Erro ao carregar perfil do usuário")
            }
        }

        profileViewModel.errorMessage.observe(this) { error ->
            if (error.isNotEmpty()) {
                Log.e("Login", "Erro no profile: $error")
                showLoginErrorState("Erro ao carregar perfil: $error")
            }
        }

        redefinePasswordViewModel.successMessage.observe(this) { message ->
            if (message.isNotEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                Log.d("Login", "Email de redefinição enviado: $message")
                Toast.makeText(this, "Você receberá um e-mail com informações para a redefinição de senha.", Toast.LENGTH_LONG).show()
            }
        }

        redefinePasswordViewModel.errorMessage.observe(this) { error ->
            if (error.isNotEmpty()) {
                Toast.makeText(this, "Erro: $error", Toast.LENGTH_LONG).show()
                Log.e("Login", "Erro ao enviar email: $error")
            }
        }

        redefinePasswordViewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                Log.d("Login", "Enviando email de redefinição...")
            }
        }

        addUserLogsViewModel.errorMessage.observe(this) { error ->
            if (error.isNotEmpty()) {
                Log.e("Login", "❌ Erro ao registrar log: $error")
            }
        }
    }

    private fun setupListeners() {
        binding.btEntrarLogin.setOnClickListener {
            val email = binding.inputEmailGestor.text.toString().trim()
            val senha = binding.inputSenhaGestor.text.toString().trim()

            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Por favor, preencha todos os campos!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isValidEmail(email)) {
                Toast.makeText(this, "Por favor, insira um email válido!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.login(email, senha, origin)
        }

        binding.redefinirSenha.setOnClickListener {
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
        Log.d("Login", "🔄 Carregando perfil do usuário...")
        profileViewModel.loadProfile()
    }

    private fun navigateToMainActivity() {
        val employee = viewModel.getUser()
        if (employee != null) {
            if (employee.role == 'A') {
                showLoginErrorState("Você não tem permissão para acessar este aplicativo.")
                Log.w("Login", "❌ Usuário sem permissão (role: ${employee.role})")
            } else {
                Log.d("Login", "✅ Navegando para MainActivity - Usuário: ${employee.email}")
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        } else {
            showLoginErrorState("Erro ao carregar dados do usuário")
            Log.e("Login", "❌ Employee é null ao navegar")
        }

        viewModel.onNavigationComplete()
    }

    private fun showLoginLoadingState(message: String = "Carregando...") {
        runOnUiThread {
            loginLoadingLayout.visibility = View.VISIBLE
            loginErrorLayout.visibility = View.GONE
            loginContentLayout.visibility = View.GONE
            loginLoadingText.text = message
            Log.d("Login", "📱 Mostrando estado de loading: $message")
        }
    }

    private fun showLoginContentState() {
        runOnUiThread {
            loginLoadingLayout.visibility = View.GONE
            loginErrorLayout.visibility = View.GONE
            loginContentLayout.visibility = View.VISIBLE
            Log.d("Login", "✅ Mostrando conteúdo do login")
        }
    }

    private fun showLoginErrorState(errorMessage: String) {
        runOnUiThread {
            loginLoadingLayout.visibility = View.GONE
            loginErrorLayout.visibility = View.VISIBLE
            loginContentLayout.visibility = View.GONE
            loginErrorText.text = errorMessage
            Log.e("Login", "❌ Mostrando estado de erro: $errorMessage")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        redefinePasswordViewModel.clearResults()
        viewModel.clearResults()
    }
}