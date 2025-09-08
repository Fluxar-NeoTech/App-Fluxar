package com.aula.app_fluxar.ui.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.aula.app_fluxar.API.model.Employee
import com.aula.app_fluxar.R
import com.aula.app_fluxar.databinding.ActivityMainBinding
import com.aula.app_fluxar.ui.fragment.NavigationPerfil
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var employee: Employee? = null

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        employee = intent.getParcelableExtra("USER_DATA")

        if (employee != null) {
            println("DADOS RECEBIDOS NA MAIN: ${employee!!.nome} ${employee!!.sobrenome}")
            println("FOTO: ${employee!!.fotoPerfil}")
        } else {
            Toast.makeText(this, "Dados do usuário não encontrados", Toast.LENGTH_SHORT).show()
        }

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
                R.id.nav_notificacoes, R.id.nav_infos, R.id.nav_limite_estoque -> {
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
    }

    fun getEmployee(): Employee? {
        return employee
    }

    fun updateEmployee(updatedEmployee: Employee) {
        this.employee = updatedEmployee
    }

    fun showDialogLogOut() {
        val dialogLogOut = layoutInflater.inflate(R.layout.sair_da_conta, null)
        val positiveButton = dialogLogOut.findViewById<Button>(R.id.sairContaS)
        val negativeButton = dialogLogOut.findViewById<Button>(R.id.sairContaN)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogLogOut)
            .create()

        positiveButton.setOnClickListener {
            Toast.makeText(this, "Você saiu da conta", Toast.LENGTH_SHORT).show()
            finish()
        }

        negativeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    fun onLoginSuccess(employee: Employee) {
        this.employee = employee
        // Notifica o fragmento de perfil para atualizar a UI
        supportFragmentManager.fragments.forEach { fragment ->
            if (fragment is NavigationPerfil) {
                fragment.updateEmployeeData(employee)
            }
        }
    }
}