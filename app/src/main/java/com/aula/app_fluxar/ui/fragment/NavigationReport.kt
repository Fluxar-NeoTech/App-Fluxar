package com.aula.app_fluxar.ui.fragment

import StockOutViewModel
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.aula.app_fluxar.API.model.CapacitySectorInfos
import com.aula.app_fluxar.API.viewModel.CapacitySectorInfosViewModel
import com.aula.app_fluxar.API.viewModel.NotificationsViewModel
import com.aula.app_fluxar.databinding.FragmentNavRelatorioBinding
import com.aula.app_fluxar.sessionManager.SessionManager

class NavigationReport : Fragment() {

    private var _binding: FragmentNavRelatorioBinding? = null
    private val binding get() = _binding!!

    private val capacitySectorInfosViewModel: CapacitySectorInfosViewModel by viewModels()
    private val notificationViewModel: NotificationsViewModel by viewModels()
    private val notificationStockOutViewModel: StockOutViewModel by viewModels()
    private var lastStockOutDays: Int? = null


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
                notifyIfUpdatedReport(it)
            }
        }

        capacitySectorInfosViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (error.isNotEmpty()) showErrorState(error)
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


    private fun updateReportUI(infos: CapacitySectorInfos): Pair<String, String> {
        try {
            binding.tvPorcentagem.text = "${infos.occupancyPercentage.toInt()}%"
            binding.progressBarRelatorio.progress = infos.occupancyPercentage.toInt()

            val profile = SessionManager.getCurrentProfile()
            val maxCapacityValue = profile?.maxCapacity

            val usedVolumeNum = if (maxCapacityValue == null || maxCapacityValue <= 0) {
                binding.metrosCubicosOcupados.text = "Indisponível"
                binding.metrosCubicosTotais.text = "Indisponível"
                0.0
            } else {
                var used = maxCapacityValue - infos.remainingVolume
                if (infos.occupancyPercentage < 0.1 || used < 0.01) used = 0.0
                binding.metrosCubicosOcupados.text = "%.1f".format(used) + "m³"
                binding.metrosCubicosTotais.text = "%.1f".format(maxCapacityValue) + "m³"
                used
            }

            if (usedVolumeNum == 0.0) {
                binding.textoSetorPodeReceber.text = Html.fromHtml(
                    "O setor pode receber <b>${String.format("%.1f", maxCapacityValue)}m³</b> de insumos no seu estoque",
                    Html.FROM_HTML_MODE_LEGACY
                )
            } else {
                binding.textoSetorPodeReceber.text = Html.fromHtml(
                    "O setor pode receber <b>${String.format("%.1f", infos.remainingVolume)}m³</b> de insumos no seu estoque",
                    Html.FROM_HTML_MODE_LEGACY
                )
            }

            val (title, message) = when {
                infos.occupancyPercentage >= 100 -> "Estoque Cheio" to "Seu estoque está lotado!"
                infos.occupancyPercentage >= 90 -> "Estoque Quase Cheio" to "É recomendado tomar medidas contra a situação."
                infos.occupancyPercentage >= 50 -> "Estoque Moderado" to "Espaço suficiente disponível no estoque."
                infos.occupancyPercentage >= 25 -> "Estoque Baixo" to "Atenção com o nível de estoque."
                else -> "Estoque Muito Baixo" to "Tome medidas urgentes para não ficar sem produtos!"
            }

            binding.textoOcupacaoEstoqueSetor3.text = message
            binding.tituloSituacaoExtoque.text = title

            return title to message

        } catch (e: Exception) {
            Log.e("NavigationRelatorio", "❌ Erro ao atualizar UI do relatório: ${e.message}")
            return "Erro" to "Não foi possível atualizar o relatório"
        }
    }

    fun notifyIfUpdatedReport(infos: CapacitySectorInfos) {
        val (title, message) = updateReportUI(infos)

        notificationViewModel.showNotification(requireContext(), title, message)

        lastOccupancyPercentage = infos.occupancyPercentage
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
