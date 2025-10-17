package com.aula.app_fluxar.ui.fragment

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
import com.aula.app_fluxar.databinding.FragmentNavRelatorioBinding
import com.aula.app_fluxar.sessionManager.SessionManager

class NavigationReport : Fragment() {

    private var _binding: FragmentNavRelatorioBinding? = null
    private val binding get() = _binding!!
    private val capacitySectorInfosViewModel: CapacitySectorInfosViewModel by viewModels()

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
                Log.d("NavigationRelatorio", "✅ Informações de capacidade carregadas: ${it.occupancyPercentage}% ocupado, ${it.remainingVolume}m³ restante")
            }
        }

        capacitySectorInfosViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (error.isNotEmpty()) {
                Log.e("NavigationRelatorio", "❌ Erro ao carregar informações de capacidade: $error")
                showErrorState(error)
            }
        }

        capacitySectorInfosViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                Log.d("NavigationRelatorio", "🔄 Carregando informações de capacidade...")
                showLoadingState()
            } else {
                hideLoadingState()
            }
        }
    }

    private fun loadCapacitySectorInfos() {
        val employee = SessionManager.getCurrentProfile()
        employee?.let {
            val sectorId = it.sector.id
            val employeeId = SessionManager.getEmployeeId()
            capacitySectorInfosViewModel.getSectorCapacityInfos(sectorId, employeeId)
        } ?: run {
            Log.e("NavigationRelatorio", "❌ Não foi possível carregar informações: employee não encontrado")
            showErrorState("Usuário não logado")
        }
    }

    private fun updateReportUI(infos: CapacitySectorInfos) {
        try {
            binding.tvPorcentagem.text = "${infos.occupancyPercentage.toInt()}%"

            binding.progressBarRelatorio.progress = infos.occupancyPercentage.toInt()

            val totalCapacity = if (infos.occupancyPercentage > 0) {
                infos.remainingVolume / (1 - infos.occupancyPercentage / 100)
            } else {
                infos.remainingVolume
            }
            val usedVolume = totalCapacity - infos.remainingVolume

            binding.metrosCubicosOcupados.text = "${String.format("%.1f", usedVolume)}m³"

            binding.metrosCubicosTotais.text = "${String.format("%.1f", totalCapacity)}m³"

            binding.textoSetorPodeReceber.text =
                Html.fromHtml("O setor pode receber <b>${String.format("%.1f", infos.remainingVolume)}m³</b> de insumos no seu estoque", Html.FROM_HTML_MODE_LEGACY)

            updateStatusMessage(infos.occupancyPercentage)

            Log.d("NavigationRelatorio", "✅ UI do relatório atualizada - Ocupado: $usedVolume m³, Total: $totalCapacity m³")

        } catch (e: Exception) {
            Log.e("NavigationRelatorio", "❌ Erro ao atualizar UI do relatório: ${e.message}")
        }
    }

    private fun updateStatusMessage(occupancyPercentage: Double) {
        val titleMessage = when {
            occupancyPercentage >= 100 -> "Estoque Cheio"
            occupancyPercentage >= 90 -> "Estoque Quase Cheio"
            occupancyPercentage >= 50 -> "Estoque Moderado"
            occupancyPercentage >= 25 -> "Estoque Baixo"
            else -> "Estoque Muito Baixo"
        }

        val statusMessage = when {
            occupancyPercentage >= 100 -> "Seu estoque está lotado!"
            occupancyPercentage >= 90 -> "É recomendado tomar medidas contra a situação."
            occupancyPercentage >= 50 -> "Espaço suficiente disponível no estoque."
            occupancyPercentage >= 25 -> "É recomendado ter atenção com o nível de estoque."
            else -> "Tome medidas urgentemente para não ficar sem produtos!"
        }

        binding.textoOcupacaoEstoqueSetor3.text = statusMessage
        binding.tituloSituacaoExtoque.text = titleMessage
    }

    private fun showLoadingState() {
        binding.homeLoadingLayout.visibility = View.VISIBLE
        binding.homeContentLayout.visibility = View.GONE
        binding.homeErrorLayout.visibility = View.GONE
    }

    private fun hideLoadingState() {
        binding.homeLoadingLayout.visibility = View.GONE
        binding.homeContentLayout.visibility = View.VISIBLE
        binding.homeErrorLayout.visibility = View.GONE
    }

    private fun showErrorState(errorMessage: String) {
        binding.homeLoadingLayout.visibility = View.GONE
        binding.homeContentLayout.visibility = View.GONE
        binding.homeErrorLayout.visibility = View.VISIBLE
        binding.homeErrorText.text = errorMessage

        android.widget.Toast.makeText(requireContext(), "Erro: $errorMessage", android.widget.Toast.LENGTH_LONG).show()

        Log.e("NavigationRelatorio", "Erro na UI: $errorMessage")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        loadCapacitySectorInfos()
    }
}