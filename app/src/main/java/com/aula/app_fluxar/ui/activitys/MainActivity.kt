package com.aula.app_fluxar.ui.activitys

import android.os.Bundle
import android.view.View
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import com.aula.app_fluxar.R
import com.aula.app_fluxar.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

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

        // Configurando o menu lateral
        val drawerLayout = binding.drawerLayout
        val menuIcon = binding.iconMenu

        menuIcon.setOnClickListener {
           drawerLayout.openDrawer(GravityCompat.START)
        }
    }
}