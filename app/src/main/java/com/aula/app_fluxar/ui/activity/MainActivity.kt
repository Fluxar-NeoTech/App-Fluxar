package com.aula.app_fluxar.ui.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.aula.app_fluxar.R
import com.aula.app_fluxar.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
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

        // Navegação entre as páginas da navbar principal
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

        // Configuração dos ícones da navbar secundária
        val backButton = binding.iconVoltar
        val secundaryNavLogo = binding.logoNavSecundaria

        // Calculando altura da navbar para abrir menu lateral
        val navigationView = binding.navigationView
        binding.root.post {
            val toolbarHeight = binding.materialToolbar.height
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

        menuIcon.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.END)
        }

        // Navegação entre páginas do menu lateral
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


        // Configuração do icone de notificações
        val notificationIcon = binding.iconNotificacoes
        notificationIcon.setOnClickListener {
            navController.navigate(R.id.nav_notificacoes)
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.nav_notificacoes, R.id.nav_infos -> {
                    backButton.visibility = View.VISIBLE
                    secundaryNavLogo.visibility = View.VISIBLE
                    backButton.setOnClickListener {
                        navController.popBackStack()
                    }

                    // Esconder bottom navigation
                    binding.navView.visibility = View.GONE

                    // Esconder todos os elementos da antiga navbar
                    binding.logo.visibility = View.GONE
                    binding.iconNotificacoes.visibility = View.GONE
                    binding.iconMenu.visibility = View.GONE
                }

                else -> {
                    // Remover icones da navbar secundária
                    backButton.visibility = View.GONE
                    secundaryNavLogo.visibility = View.GONE

                    // Mostrar bottom navigation
                    binding.navView.visibility = View.VISIBLE

                    // Mostrar todos os elementos da navbar
                    binding.logo.visibility = View.VISIBLE
                    binding.iconNotificacoes.visibility = View.VISIBLE
                    binding.iconMenu.visibility = View.VISIBLE
                }
            }
        }
    }

    // Função para abrir o dialog personalizado (sair_da_conta.xml)
    fun showDialogLogOut() {
        val dialogLogOut = layoutInflater.inflate(R.layout.sair_da_conta, null)
        val positiveButton = dialogLogOut.findViewById<Button>(R.id.sairContaS)
        val negativeButton = dialogLogOut.findViewById<Button>(R.id.sairContaN)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogLogOut)
            .create()

        positiveButton.setOnClickListener {
            // Lógica para sair da conta
            Toast.makeText(this, "Você saiu da conta", Toast.LENGTH_SHORT).show()
        }

        negativeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }
}