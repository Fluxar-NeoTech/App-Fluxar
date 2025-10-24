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

            val profile = SessionManager.getCurrentProfile()
            val maxCapacityValue = profile?.maxCapacity

            Log.d("RelatorioDiagnostico", "Ocupação (%): ${infos.occupancyPercentage.toInt()}%")
            Log.d("RelatorioDiagnostico", "MaxCapacity (Profile): $maxCapacityValue")
            Log.d("RelatorioDiagnostico", "RemainingVolume (API): ${infos.remainingVolume}")


            if (maxCapacityValue == null || maxCapacityValue <= 0) {
                val unavailableText = "Indisponível"
                binding.metrosCubicosOcupados.text = "${unavailableText}m³"
                binding.metrosCubicosTotais.text = "${unavailableText}m³"

                Log.w("RelatorioDiagnostico", "Capacidade Máxima (maxCapacity) é nula ou zero. Exibindo 'Indisponível'.")
            } else {
                val totalCapacityNum = maxCapacityValue.toDouble()
                var usedVolumeNum = totalCapacityNum - infos.remainingVolume

                if (infos.occupancyPercentage < 0.1) {
                    usedVolumeNum = 0.0
                    Log.d("RelatorioDiagnostico", "Ajuste de 0%: UsedVolume forçado para 0.0m³")
                }

                val totalCapacity = String.format("%.1f", totalCapacityNum)
                val usedVolume = String.format("%.1f", usedVolumeNum)

                binding.metrosCubicosOcupados.text = "${usedVolume}m³"
                binding.metrosCubicosTotais.text = "${totalCapacity}m³"

                Log.d("RelatorioDiagnostico", "Cálculo: Total=$totalCapacity, Ocupado=$usedVolume")

                // Restante do código
                binding.textoSetorPodeReceber.text =
                    Html.fromHtml("O setor pode receber <b>${String.format("%.1f", infos.remainingVolume)}m³</b> de insumos no seu estoque", Html.FROM_HTML_MODE_LEGACY)
            }

            updateStatusMessage(infos.occupancyPercentage)

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