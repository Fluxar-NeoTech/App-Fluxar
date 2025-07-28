package com.aula.app_fluxar.ui.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.aula.app_fluxar.R

class ErroConexaoInternet : AppCompatActivity() {
    private lateinit var connectivityReceiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_erro_conexao_internet)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Criar receiver para escutar reconexão
        connectivityReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (isConnectedToInternet()) {
                    startActivity(Intent(this@ErroConexaoInternet, Login::class.java))
                    finish()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(
            connectivityReceiver,
            IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        )
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(connectivityReceiver)
    }

    private fun isConnectedToInternet(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val network = cm.activeNetworkInfo
        return network != null && network.isConnected
    }
}