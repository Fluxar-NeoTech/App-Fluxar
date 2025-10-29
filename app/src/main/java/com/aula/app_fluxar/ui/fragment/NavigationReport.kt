package com.aula.app_fluxar.ui.fragment

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.aula.app_fluxar.API.model.CapacitySectorInfos
import com.aula.app_fluxar.API.model.NotificationItem
import com.aula.app_fluxar.API.viewModel.CapacitySectorInfosViewModel
import com.aula.app_fluxar.API.viewModel.NotificationsViewModel
import com.aula.app_fluxar.R
import com.aula.app_fluxar.databinding.FragmentNavRelatorioBinding
import com.aula.app_fluxar.sessionManager.SessionManager
import kotlinx.coroutines.launch

class NavigationReport : Fragment() {

    private var _binding: FragmentNavRelatorioBinding? = null
    private val binding get() = _binding!!

    private val capacitySectorInfosViewModel: CapacitySectorInfosViewModel by viewModels()
    private val notificationsViewModel: NotificationsViewModel by viewModels()

    private var lastOccupancyPercentage: Double? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNavRelatorioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        loadCapacitySectorInfos()
    }

    private fun setupObservers() {
        capacitySectorInfosViewModel.capacitySectorInfosResult.observe(viewLifecycleOwner) { infos ->
            infos?.let {
                updateReportUI(it)
                notifyIfUpdated(it)
            }
        }

        capacitySectorInfosViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (error.isNotEmpty()) showErrorState(error)
        }

        capacitySectorInfosViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) showLoadingState() else hideLoadingState()
        }
    }

    private fun loadCapacitySectorInfos() {
        val profile = SessionManager.getCurrentProfile()
        profile?.let {
            capacitySectorInfosViewModel.getSectorCapacityInfos(it.sector.id, SessionManager.getEmployeeId())
        } ?: run {
            showErrorState("Usuário não logado")
        }
    }

    private fun updateReportUI(infos: CapacitySectorInfos) {
        binding.tvPorcentagem.text = "${infos.occupancyPercentage.toInt()}%"
        binding.progressBarRelatorio.progress = infos.occupancyPercentage.toInt()

        val profile = SessionManager.getCurrentProfile()
        val maxCapacity = profile?.maxCapacity ?: 0.0

        if (maxCapacity <= 0) {
            binding.metrosCubicosOcupados.text = "Indisponível"
            binding.metrosCubicosTotais.text = "Indisponível"
        } else {
            val used = maxCapacity - infos.remainingVolume
            binding.metrosCubicosOcupados.text = "%.1f".format(used)
            binding.metrosCubicosTotais.text = "%.1f".format(maxCapacity)
        }

        binding.textoSetorPodeReceber.text = Html.fromHtml(
            "O setor pode receber <b>${infos.remainingVolume}m³</b> de insumos",
            Html.FROM_HTML_MODE_LEGACY
        )
    }

    private fun notifyIfUpdated(infos: CapacitySectorInfos) {
        val currentPercentage = infos.occupancyPercentage

        val (title, message) = when {
            currentPercentage >= 100 -> "Estoque Cheio" to "Seu estoque está lotado!"
            currentPercentage >= 90 -> "Estoque Quase Cheio" to "É recomendado tomar medidas contra a situação."
            currentPercentage >= 50 -> "Estoque Moderado" to "Espaço suficiente disponível no estoque."
            currentPercentage >= 25 -> "Estoque Baixo" to "Atenção com o nível de estoque."
            else -> "Estoque Muito Baixo" to "Tome medidas urgentes para não ficar sem produtos!"
        }
        showNotification(requireContext(), title, message)

        lifecycleScope.launch {
            notificationsViewModel.addNotification(NotificationItem(title, message))
        }

        binding.textoOcupacaoEstoqueSetor3.text = message
        binding.tituloSituacaoExtoque.text = title

        lastOccupancyPercentage = currentPercentage
    }

    private fun showLoadingState() {
        binding.homeLoadingLayout.visibility = View.VISIBLE
        binding.homeContentLayout?.visibility = View.GONE
        binding.homeErrorLayout.visibility = View.GONE
    }

    private fun hideLoadingState() {
        binding.homeLoadingLayout.visibility = View.GONE
        binding.homeContentLayout?.visibility = View.VISIBLE
        binding.homeErrorLayout.visibility = View.GONE
    }

    private fun showErrorState(errorMessage: String) {
        binding.homeLoadingLayout.visibility = View.GONE
        binding.homeContentLayout?.visibility = View.GONE
        binding.homeErrorLayout.visibility = View.VISIBLE
        binding.homeErrorText.text = errorMessage
    }

    private fun showNotification(context: Context, title: String, message: String) {
        val channelId = "fluxar_channel"
        val notificationId = System.currentTimeMillis().toInt()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Notificações do Fluxar",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.estoque_cheio_icon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
