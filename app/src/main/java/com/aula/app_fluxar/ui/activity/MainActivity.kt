package com.aula.app_fluxar.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
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
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.aula.app_fluxar.API.model.NotificationItem
import com.aula.app_fluxar.API.model.UserLogRequest
import com.aula.app_fluxar.API.viewModel.*
import com.aula.app_fluxar.R
import com.aula.app_fluxar.databinding.ActivityMainBinding
import com.aula.app_fluxar.sessionManager.SessionManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val profileViewModel: ProfileViewModel by viewModels()
    private val addUserLogsViewModel: AddUserLogsViewModel by viewModels()
    private val notificationsViewModel: NotificationsViewModel by viewModels()
    private val capacitySectorInfosViewModel: CapacitySectorInfosViewModel by viewModels()

    private lateinit var binding: ActivityMainBinding
    private lateinit var mainLoadingLayout: LinearLayout
    private lateinit var mainErrorLayout: LinearLayout
    private lateinit var mainContentLayout: ConstraintLayout
    private lateinit var mainErrorText: TextView
    private lateinit var mainRetryButton: Button
    private lateinit var mainLoadingProgress: ProgressBar
    private lateinit var mainLoadingText: TextView

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
            observeCapacitySectorNotifications()
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
            mainLoadingProgress = mainLoadingLayout.findViewById(R.id.mainLoadingProgress)
            mainLoadingText = mainLoadingLayout.findViewById(R.id.mainLoadingText)
        } catch (e: Exception) {
            Log.d("MainActivity", "Elementos de loading específicos não encontrados")
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
                loadSectorInfos()
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
                R.id.nav_home -> { navController.navigate(R.id.nav_home); true }
                R.id.nav_relatorio -> { navController.navigate(R.id.nav_relatorio); true }
                R.id.nav_unidades -> { navController.navigate(R.id.nav_unidades); true }
                R.id.nav_perfil -> { navController.navigate(R.id.nav_perfil); true }
                else -> false
            }
        }

        val backButton = binding.iconVoltar
        val secondaryNavLogo = binding.logoNavSecundaria
        val navigationView = binding.navigationView

        binding.root.post {
            try {
                val toolbar = binding.materialToolbar
                val statusBarHeight = getStatusBarHeight()
                val layoutParams = navigationView.layoutParams as ViewGroup.MarginLayoutParams
                layoutParams.topMargin = statusBarHeight + toolbar.height
                navigationView.layoutParams = layoutParams
            } catch (e: Exception) {
                Log.e("MainActivity", "Erro ao configurar margem da NavigationView: ${e.message}")
            }
        }

        val drawerLayout = binding.drawerLayout
        val menuIcon = binding.iconMenu
        menuIcon.setOnClickListener { drawerLayout.openDrawer(GravityCompat.END) }

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_info -> navController.navigate(R.id.nav_infos)
                R.id.nav_sair -> showDialogLogOut()
                R.id.nav_tema -> Toast.makeText(this, "Disponível nas próximas versões!", Toast.LENGTH_SHORT).show()
                R.id.nav_limite_estoque -> navController.navigate(R.id.nav_limite_estoque)
                R.id.nav_fabricas -> navController.navigate(R.id.nav_unidades)
            }
            drawerLayout.closeDrawer(GravityCompat.END)
            true
        }

        binding.iconNotificacoes.setOnClickListener { navController.navigate(R.id.nav_notificacoes) }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.nav_notificacoes, R.id.nav_infos, R.id.nav_limite_estoque, R.id.navigationUnitDetails -> {
                    backButton.visibility = View.VISIBLE
                    secondaryNavLogo.visibility = View.VISIBLE
                    backButton.setOnClickListener { navController.popBackStack() }
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
    }

    private fun showMainLoadingState(message: String = "Carregando...") {
        runOnUiThread {
            mainLoadingLayout.visibility = View.VISIBLE
            mainErrorLayout.visibility = View.GONE
            mainContentLayout.visibility = View.GONE
            try { mainLoadingText.text = message } catch (e: Exception) {}
        }
    }

    private fun showMainContentState() {
        runOnUiThread {
            mainLoadingLayout.visibility = View.GONE
            mainErrorLayout.visibility = View.GONE
            mainContentLayout.visibility = View.VISIBLE
        }
    }

    private fun showMainErrorState(errorMessage: String) {
        runOnUiThread {
            mainLoadingLayout.visibility = View.GONE
            mainErrorLayout.visibility = View.VISIBLE
            mainContentLayout.visibility = View.GONE
            mainErrorText.text = errorMessage
        }
    }

    private fun restartApp() {
        showMainLoadingState("Reiniciando aplicação...")
        Handler(Looper.getMainLooper()).postDelayed({ initializeApp() }, 1000)
    }

    private fun showDialogLogOut() {
        val dialogLogOut = layoutInflater.inflate(R.layout.sair_da_conta, null)
        val positiveButton = dialogLogOut.findViewById<Button>(R.id.sairContaS)
        val negativeButton = dialogLogOut.findViewById<Button>(R.id.sairContaN)

        val dialog = AlertDialog.Builder(this).setView(dialogLogOut).create()

        positiveButton.setOnClickListener {
            val action = "Usuário realizou logout"
            addUserLogsViewModel.addUserLogs(UserLogRequest(SessionManager.getEmployeeId(), action))

            SessionManager.clear()
            Toast.makeText(this, "Você saiu da conta", Toast.LENGTH_SHORT).show()

            val intent = Intent(this@MainActivity, Login::class.java)

            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        negativeButton.setOnClickListener { dialog.dismiss() }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.END)) binding.drawerLayout.closeDrawer(GravityCompat.END)
        else super.onBackPressed()
    }

    // Permissão de notificações
    val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) Toast.makeText(this, "As notificações estão desativadas", Toast.LENGTH_SHORT).show()
    }

    fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun getStatusBarHeight(): Int {
        return try {
            val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 24.dpToPx()
        } catch (e: Exception) {
            24.dpToPx()
        }
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()

    // 🔔 Observa alterações no estoque e dispara notificações
    private fun observeCapacitySectorNotifications() {
        val profile = SessionManager.getCurrentProfile()
        profile?.let {
            capacitySectorInfosViewModel.getSectorCapacityInfos(it.sector.id, SessionManager.getEmployeeId())

            capacitySectorInfosViewModel.notificationEvent.observe(this) { (title, message) ->
                showNotification(title, message)
                lifecycleScope.launch {
                    notificationsViewModel.addNotification(NotificationItem(title, message))
                }
            }
        }
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "fluxar_channel"
        val notificationId = System.currentTimeMillis().toInt()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(channelId, "Notificações do Fluxar", android.app.NotificationManager.IMPORTANCE_DEFAULT)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            manager.createNotificationChannel(channel)
        }

        val builder = androidx.core.app.NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.estoque_cheio_icon)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            androidx.core.app.NotificationManagerCompat.from(this).notify(notificationId, builder.build())
        }
    }

    private fun loadSectorInfos() {
        val profile = SessionManager.getCurrentProfile()
        profile?.let {
            capacitySectorInfosViewModel.getSectorCapacityInfos(it.sector.id, SessionManager.getEmployeeId())
        }
    }
}
