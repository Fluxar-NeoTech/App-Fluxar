package com.aula.app_fluxar.ui.fragment

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aula.app_fluxar.API.model.NotificationRequest
import com.aula.app_fluxar.API.model.NotificationResponse
import com.aula.app_fluxar.API.viewModel.NotificationsViewModel
import com.aula.app_fluxar.R
import com.aula.app_fluxar.adapters.NotificationsAdapter
import com.aula.app_fluxar.sessionManager.SessionManager

class Notifications : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotificationsAdapter
    private val notificationsList = mutableListOf<NotificationResponse>()

    private val viewModel: NotificationsViewModel by viewModels()

    // Solicitação de permissão de notificações
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("Notification", "✅ Permissão concedida para notificações.")
        } else {
            Log.w("Notification", "❌ Permissão negada para notificações.")
        }
    }

    // Verifica e solicita permissão de notificações
    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Log.d("Notification", "🔔 Permissão já concedida.")
                }

                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkAndRequestNotificationPermission()

        // Configura RecyclerView
        recyclerView = view.findViewById(R.id.notificacoes_RV)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = NotificationsAdapter(notificationsList)
        recyclerView.adapter = adapter

        // Observa LiveData da ViewModel
        viewModel.notification.observe(viewLifecycleOwner) { notification ->
            notification?.let {
                // Adiciona à lista e atualiza o RecyclerView
                notificationsList.add(it)
                adapter.notifyItemInserted(notificationsList.size - 1)

                // Mostra notificação do Android
                showNotification(
                    requireContext(),
                    "ATENÇÃO!",
                    "Seu estoque está ficando cheio! Restam apenas ${it.days_to_stockout_pred} dias para o estoque encher!"
                )
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrEmpty()) {
                Log.e("NotificationsFragment", error)
            }
        }

        // Chama a api de notificações
        val sessionManager = SessionManager.getCurrentProfile()
        viewModel.fetchNotification(
            NotificationRequest(
                sessionManager!!.unit.industry.id,
                sessionManager.sector.id
            )
        )
    }

    // Função para mostrar notificação
    fun showNotification(context: Context, title: String, message: String) {
        val channelId = "fluxar_channel"
        val notificationId = System.currentTimeMillis().toInt()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Notificações do Fluxar",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notify(notificationId, builder.build())
            }
        }
    }
}