package com.aula.app_fluxar.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
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
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import com.aula.app_fluxar.API.model.NotificationRequest
import com.aula.app_fluxar.API.model.Profile
import com.aula.app_fluxar.API.model.UserLogRequest
import com.aula.app_fluxar.API.viewModel.AddUserLogsViewModel
import com.aula.app_fluxar.API.viewModel.NotificationsViewModel
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
    private var currentProfile: Profile? = null
    private val addUserLogsViewModel: AddUserLogsViewModel by viewModels()
    private val notificationsViewModel: NotificationsViewModel by viewModels()

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadProfile()

        initStateViews()

        showMainLoadingState("Carregando aplicação...")

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        Handler(Looper.getMainLooper()).postDelayed({
            initializeApp()
        }, 800)

        checkAndRequestNotificationPermission()
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

        // Observa o resultado do perfil
        profileViewModel.profileResult.observe(this) { profile ->
            profile?.let {
                currentProfile = it
                Log.d("MainActivity", "✅ Perfil carregado: ${it.firstName}")

                setupNavigation()
                showMainContentState()

                fetchAndShowStockPrediction(it)
            }
        }

        // Observa erros
        profileViewModel.errorMessage.observe(this) { error ->
            if (!error.isNullOrEmpty()) {
                Log.e("MainActivity", "Erro ao carregar perfil: $error")
            }
        }
    }

    private fun fetchAndShowStockPrediction(profile: Profile) {
        val request = NotificationRequest(
            profile.unit.industry.id,
            profile.sector.id
        )

        notificationsViewModel.fetchNotification(request)

        notificationsViewModel.notification.observe(this) { notification ->
            notification?.let {
                Log.d("MainActivity", "📦 Previsão: ${it.days_to_stockout_pred} dias restantes")
                if (it.days_to_stockout_pred <= 7) {
                    showNotification(
                        this,
                        "ALERTA DE ESTOQUE BAIXO!",
                        "Ruptura iminente: restam apenas ${"%.1f".format(it.days_to_stockout_pred)} dias!"
                    )
                }
            }
        }

        notificationsViewModel.errorMessage.observe(this) { error ->
            if (!error.isNullOrEmpty()) {
                Log.e("MainActivity", "Erro na previsão: $error")
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

    // Solicitação de permissão de notificações
    val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.d("Notification", "✅ Permissão concedida!")
            } else {
                Log.w("Notification", "❌ Permissão negada!")
            }
        }


    // Verifica e solicita permissão de notificações
    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                Log.d("MainActivity", "Permissão já concedida")
            }
        }
    }

    fun showNotification(context: Context, title: String, message: String) {
        val channelId = "fluxar_channel"
        val notificationId = System.currentTimeMillis().toInt()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Notificações do Fluxar",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notify(notificationId, builder.build())
            } else {
                Log.w("MainActivity", "⚠️ Permissão de notificação não concedida.")
            }
        }
    }
}