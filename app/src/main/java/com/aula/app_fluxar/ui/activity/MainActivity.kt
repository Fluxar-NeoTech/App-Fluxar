package com.aula.app_fluxar.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import com.aula.app_fluxar.R
import com.aula.app_fluxar.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home,R.id.nav_relatorio,R.id.nav_unidades,R.id.nav_perfil
            )
        )

        // Configuração dos ícones da navbar secundária
        val backButton = binding.iconVoltar
        val logoNavSecundaria = binding.logoNavSecundaria

        // Calculando altura da navbar para abrir menu lateral
        binding.root.post {
            val toolbarHeight = binding.materialToolbar.height
            val navigationView = binding.navigationView
            val layoutParams = navigationView.layoutParams as ViewGroup.MarginLayoutParams

            layoutParams.topMargin = toolbarHeight
            navigationView.layoutParams = layoutParams
        }

        // Configurando o menu lateral
        val drawerLayout = binding.drawerLayout
        val menuIcon = binding.iconMenu

        menuIcon.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.END)
        }

        // Configuração do icone de notificações
        val notificationIcon = binding.iconNotificacoes
        notificationIcon.setOnClickListener {
            navController.navigate(R.id.nav_notificacoes)
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.nav_notificacoes -> {
                    backButton.visibility = View.VISIBLE
                    logoNavSecundaria.visibility = View.VISIBLE
                    backButton.setOnClickListener {
                        navController.popBackStack()
                    }

                    // Esconder bottom navigation
                    binding.navView.visibility = View.GONE

                    // Esconder todos os elementos da antiga navbar
                    binding.logo.visibility = View.GONE
                    binding.iconCalendario.visibility = View.GONE
                    binding.iconNotificacoes.visibility = View.GONE
                    binding.iconMenu.visibility = View.GONE
                }
                else -> {
                    // Remover icones da navbar secundária
                    backButton.visibility = View.GONE
                    logoNavSecundaria.visibility = View.GONE

                    // Mostrar bottom navigation
                    binding.navView.visibility = View.VISIBLE

                    // Mostrar todos os elementos da navbar
                    binding.logo.visibility = View.VISIBLE
                    binding.iconCalendario.visibility = View.VISIBLE
                    binding.iconNotificacoes.visibility = View.VISIBLE
                    binding.iconMenu.visibility = View.VISIBLE
                }
            }
        }
    }
}