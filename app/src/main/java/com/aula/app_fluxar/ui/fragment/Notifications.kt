package com.aula.app_fluxar.ui.fragment

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.aula.app_fluxar.ui.activity.MainActivity

class Notifications : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotificationsAdapter
    private val notificationsList = mutableListOf<NotificationResponse>()

    private val viewModel: NotificationsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_nav_notificacoes, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // RecyclerView
        recyclerView = view.findViewById(R.id.notificacoes_RV)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = NotificationsAdapter(notificationsList)
        recyclerView.adapter = adapter

        // Pedir permissão se necessário
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            (activity as? MainActivity)?.requestNotificationPermissionLauncher?.launch(
                Manifest.permission.POST_NOTIFICATIONS
            )
        }

        val profile = SessionManager.getCurrentProfile()
        if (profile != null) {
            viewModel.fetchNotification(
                NotificationRequest(profile.unit.industry.id, profile.sector.id)
            )
        } else {
            Log.e("NotificationsFragment", "Profile nulo, não é possível buscar notificações")
            return
        }

        viewModel.notification.observe(viewLifecycleOwner) { notification ->
            notification?.let {
                notificationsList.add(it)
                adapter.notifyItemInserted(notificationsList.size - 1)

                Log.d("Notifications", "Notificação recebida. Dias para ruptura: ${it.days_to_stockout_pred}")

                Log.d("Notifications", "Condição de <= 7 dias atendida.")

                context?.let { ctx ->
                    showNotification(
                        ctx,
                        "ALERTA DE ESTOQUE BAIXO!",
                        "Ruptura de estoque iminente! Restam apenas ${it.days_to_stockout_pred} dias de suprimento!"
                    )
                    Log.d("Notifications", "Tentando notificação de estoque baixo")
                }

            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrEmpty()) Log.e("NotificationsFragment", error)
        }
    }

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
