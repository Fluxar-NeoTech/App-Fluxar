package com.aula.app_fluxar.ui.fragment

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aula.app_fluxar.API.viewModel.NotificationsViewModel
import com.aula.app_fluxar.R
import com.aula.app_fluxar.adapters.NotificationsAdapter
import com.aula.app_fluxar.ui.activity.MainActivity

class Notifications : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotificationsAdapter
    private lateinit var clearNotifications: Button
    private val viewModel: NotificationsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_nav_notificacoes, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.notificacoes_RV)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        clearNotifications = view.findViewById(R.id.limparNotificacoes)

        adapter = NotificationsAdapter(mutableListOf()) { index ->
            viewModel.removeNotification(index)
        }
        recyclerView.adapter = adapter

        viewModel.notifications.observe(viewLifecycleOwner) { newNotifications ->
            adapter.apply {
                notifications.addAll(newNotifications)
                notifyDataSetChanged()
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            (activity as? MainActivity)?.requestNotificationPermissionLauncher?.launch(
                Manifest.permission.POST_NOTIFICATIONS
            )
        }

        clearNotifications.setOnClickListener({
            viewModel.clearAll()
            adapter.apply {
                notifications.clear()
                notifyDataSetChanged()
            }
        })
    }
}

