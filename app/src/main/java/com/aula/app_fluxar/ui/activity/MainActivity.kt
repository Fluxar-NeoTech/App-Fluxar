package com.aula.app_fluxar.ui.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.aula.app_fluxar.API.viewModel.ProfileViewModel
import com.aula.app_fluxar.R
import com.aula.app_fluxar.sessionManager.SessionManager
import androidx.activity.viewModels
import com.aula.app_fluxar.API.model.UserLogRequest
import com.aula.app_fluxar.API.viewModel.AddUserLogsViewModel
import com.aula.app_fluxar.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private val profileViewModel: ProfileViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding
    private lateinit var mainLoadingLayout: LinearLayout
    private lateinit var mainErrorLayout: LinearLayout
    private lateinit var mainContentLayout: ConstraintLayout
    private lateinit var mainErrorText: TextView
    private lateinit var mainRetryButton: Button
    private lateinit var mainLoadingProgress: ProgressBar
    private lateinit var mainLoadingText: TextView
    private val addUserLogsViewModel: AddUserLogsViewModel by viewModels()

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initStateViews()

        showMainLoadingState("Carregando aplicação...")

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        Handler(Looper.getMainLooper()).postDelayed({
            initializeApp()
        }, 800)
    }

    private fun initStateViews() {
        mainLoadingLayout = findViewById(R.id.mainLoadingLayout)
        mainErrorLayout = findViewById(R.id.mainErrorLayout)
        mainContentLayout = findViewById(R.id.mainContentLayout)
        mainErrorText = findViewById(R.id.mainErrorText)
        mainRetryButton = findViewById(R.id.mainRetryButton)

        try {
            mainLoadingProgress = mainLoadingLayout.findViewById<ProgressBar>(R.id.mainLoadingProgress)
            mainLoadingText = mainLoadingLayout.findViewById<TextView>(R.id.mainLoadingText)
        } catch (e: Exception) {
            Log.d("MainActivity", "Elementos de loading específicos não encontrados, usando layout padrão")
        }

        mainRetryButton.setOnClickListener {
            restartApp()
        }
    }

    private fun initializeApp() {
        try {
            if (SessionManager.getCurrentProfile() != null) {
                loadProfile()
            } else {
                setupNavigation()
                showMainContentState()
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Erro ao inicializar aplicação: ${e.message}", e)
            showMainErrorState("Erro ao carregar aplicação: ${e.message}")
        }
    }

    private fun loadProfile() {
        profileViewModel.loadProfile()

        profileViewModel.profileResult.observe(this) { profile ->
            if (profile != null) {
                Log.d("MainActivity", "✅ Profile carregado: ${profile.firstName}")
                SessionManager.saveProfile(profile)

                setupNavigation()
                showMainContentState()

            } else {
                Log.e("MainActivity", "❌ Profile não carregado")
                showMainErrorState("Erro ao carregar perfil do usuário")
            }
        }

        profileViewModel.errorMessage.observe(this) { error ->
            if (error.isNotEmpty()) {
                Log.e("MainActivity", "❌ Erro no profile: $error")
                setupNavigation()
                showMainContentState()
            }
        }

        profileViewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                showMainLoadingState("Carregando perfil...")
            }
        }
    }

    private fun setupNavigation() {
        try {
            val navView: BottomNavigationView = binding.navView
            val navController = findNavController(R.id.nav_host_fragment_activity_main)

            val appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.nav_home,
                    R.id.nav_relatorio,
                    R.id.nav_unidades,
                    R.id.nav_perfil
                ),
                binding.drawerLayout
            )
            navView.setupWithNavController(navController)

            navView.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_home -> {
                        navController.navigate(R.id.nav_home)
                        true
                    }
                    R.id.nav_relatorio -> {
                        navController.navigate(R.id.nav_relatorio)
                        true
                    }
                    R.id.nav_unidades -> {
                        navController.navigate(R.id.nav_unidades)
                        true
                    }
                    R.id.nav_perfil -> {
                        navController.navigate(R.id.nav_perfil)
                        true
                    }
                    else -> false
                }
            }

            val backButton = binding.iconVoltar
            val secondaryNavLogo = binding.logoNavSecundaria

            val navigationView = binding.navigationView
            binding.root.post {
                val toolbarHeight = binding.materialToolbar.height
                val layoutParams = navigationView.layoutParams as ViewGroup.MarginLayoutParams
                layoutParams.topMargin = toolbarHeight
                navigationView.layoutParams = layoutParams
            }

            val drawerLayout = binding.drawerLayout
            val menuIcon = binding.iconMenu

            menuIcon.setOnClickListener {
                drawerLayout.openDrawer(GravityCompat.END)
            }

            navigationView.setNavigationItemSelectedListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.nav_info -> {
                        navController.navigate(R.id.nav_infos)
                    }
                    R.id.nav_sair -> {
                        showDialogLogOut()
                    }
                    R.id.nav_tema -> {
                        Toast.makeText(this, "Disponível nas próximas versões!", Toast.LENGTH_SHORT).show()
                    }
                    R.id.nav_limite_estoque -> {
                        navController.navigate(R.id.nav_limite_estoque)
                    }
                    R.id.nav_fabricas -> {
                        navController.navigate(R.id.nav_unidades)
                    }
                }
                drawerLayout.closeDrawer(GravityCompat.END)
                true
            }

            val notificationIcon = binding.iconNotificacoes
            notificationIcon.setOnClickListener {
                navController.navigate(R.id.nav_notificacoes)
            }

            navController.addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.nav_notificacoes, R.id.nav_infos, R.id.nav_limite_estoque, R.id.navigationUnitDetails -> {
                        backButton.visibility = View.VISIBLE
                        secondaryNavLogo.visibility = View.VISIBLE
                        backButton.setOnClickListener {
                            navController.popBackStack()
                        }
                        binding.navView.visibility = View.GONE
                        binding.logo.visibility = View.GONE
                        binding.iconNotificacoes.visibility = View.GONE
                        binding.iconMenu.visibility = View.GONE
                    }
                    else -> {
                        backButton.visibility = View.GONE
                        secondaryNavLogo.visibility = View.GONE
                        binding.navView.visibility = View.VISIBLE
                        binding.logo.visibility = View.VISIBLE
                        binding.iconNotificacoes.visibility = View.VISIBLE
                        binding.iconMenu.visibility = View.VISIBLE
                    }
                }
            }

            Log.d("MainActivity", "✅ Navegação configurada com sucesso")

        } catch (e: Exception) {
            Log.e("MainActivity", "❌ Erro ao configurar navegação: ${e.message}", e)
            throw e
        }
    }

    private fun showMainLoadingState(message: String = "Carregando...") {
        runOnUiThread {
            mainLoadingLayout.visibility = View.VISIBLE
            mainErrorLayout.visibility = View.GONE
            mainContentLayout.visibility = View.GONE

            try {
                mainLoadingText.text = message
            } catch (e: Exception) {
            }
        }
    }

    private fun showMainContentState() {
        runOnUiThread {
            mainLoadingLayout.visibility = View.GONE
            mainErrorLayout.visibility = View.GONE
            mainContentLayout.visibility = View.VISIBLE

            Log.d("MainActivity", "✅ Conteúdo principal exibido")
        }
    }

    private fun showMainErrorState(errorMessage: String) {
        runOnUiThread {
            mainLoadingLayout.visibility = View.GONE
            mainErrorLayout.visibility = View.VISIBLE
            mainContentLayout.visibility = View.GONE

            mainErrorText.text = errorMessage
            Log.e("MainActivity", "❌ Estado de erro: $errorMessage")
        }
    }

    private fun restartApp() {
        showMainLoadingState("Reiniciando aplicação...")

        Handler(Looper.getMainLooper()).postDelayed({
            initializeApp()
        }, 1000)
    }

    fun showDialogLogOut() {
        val dialogLogOut = layoutInflater.inflate(R.layout.sair_da_conta, null)
        val positiveButton = dialogLogOut.findViewById<Button>(R.id.sairContaS)
        val negativeButton = dialogLogOut.findViewById<Button>(R.id.sairContaN)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogLogOut)
            .create()

        positiveButton.setOnClickListener {
            SessionManager.clear()
            Toast.makeText(this, "Você saiu da conta", Toast.LENGTH_SHORT).show()

            val action = "Usuário realizou logout"
            addUserLogsViewModel.addUserLogs(UserLogRequest(SessionManager.getEmployeeId(), action))

            val intent = Intent(this@MainActivity, Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        negativeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
            binding.drawerLayout.closeDrawer(GravityCompat.END)
        } else {
            super.onBackPressed()
        }
    }
}