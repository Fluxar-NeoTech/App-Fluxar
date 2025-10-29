package com.aula.app_fluxar.ui.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.aula.app_fluxar.API.model.UserLogRequest
import com.aula.app_fluxar.API.viewModel.AddUserLogsViewModel
import com.aula.app_fluxar.API.viewModel.CapacityStockViewModel
import com.aula.app_fluxar.API.viewModel.CapacitySectorInfosViewModel
import com.aula.app_fluxar.R
import com.aula.app_fluxar.sessionManager.SessionManager
import com.google.android.material.textfield.TextInputEditText

class NavigationStockLimit : Fragment() {
    private val viewModel: CapacityStockViewModel by viewModels()
    private val capacitySectorInfosViewModel: CapacitySectorInfosViewModel by viewModels()

    private var alturaEstoque: TextInputEditText? = null
    private var larguraEstoque: TextInputEditText? = null
    private var comprimentoEstoque: TextInputEditText? = null
    private var concluirBt: Button? = null

    private var stockLimitLoadingLayout: LinearLayout? = null
    private var stockLimitErrorLayout: LinearLayout? = null
    private var stockLimitContentLayout: androidx.constraintlayout.widget.ConstraintLayout? = null
    private var stockLimitLoadingProgress: ProgressBar? = null
    private var stockLimitLoadingText: TextView? = null
    private var stockLimitErrorText: TextView? = null
    private var stockLimitRetryButton: Button? = null

    private val addUserLogsViewModel: AddUserLogsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_layout_nav_limite_estoque, container, false)

        alturaEstoque = view.findViewById(R.id.inputAlturaCapacidade)
        larguraEstoque = view.findViewById(R.id.inputlarguraCapacidade)
        comprimentoEstoque = view.findViewById(R.id.inputComprimentoCapacidade)
        concluirBt = view.findViewById(R.id.concluirBt)

        stockLimitLoadingLayout = view.findViewById(R.id.stockLimitLoadingLayout)
        stockLimitErrorLayout = view.findViewById(R.id.stockLimitErrorLayout)
        stockLimitContentLayout = view.findViewById(R.id.stockLimitContentLayout)
        stockLimitLoadingProgress = view.findViewById(R.id.stockLimitLoadingProgress)
        stockLimitLoadingText = view.findViewById(R.id.stockLimitLoadingText)
        stockLimitErrorText = view.findViewById(R.id.stockLimitErrorText)
        stockLimitRetryButton = view.findViewById(R.id.stockLimitRetryButton)

        setupListeners()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        stockLimitRetryButton?.setOnClickListener {
            showStockLimitContentState()
        }

        viewModel.capacityStockResult.observe(viewLifecycleOwner) { result ->
            if (result != null) {
                Toast.makeText(
                    requireContext(),
                    "Capacidade salva com sucesso!",
                    Toast.LENGTH_SHORT
                ).show()

                alturaEstoque?.text?.clear()
                larguraEstoque?.text?.clear()
                comprimentoEstoque?.text?.clear()

                viewModel.clearResult()
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (error.isNotEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                viewModel.clearResult()
            }
        }

        showStockLimitContentState()
    }

    private fun setupListeners() {
        concluirBt?.setOnClickListener {
            val profile = SessionManager.getCurrentProfile()

            if (profile == null) {
                Toast.makeText(context, "Erro: Perfil não carregado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val setorId = profile.sector.id
            val unidadeId = profile.unit.id

            val alturaStr = alturaEstoque?.text.toString().trim()
            val larguraStr = larguraEstoque?.text.toString().trim()
            val comprimentoStr = comprimentoEstoque?.text.toString().trim()

            if (alturaStr.isEmpty() || larguraStr.isEmpty() || comprimentoStr.isEmpty()) {
                Toast.makeText(context, "Por favor, preencha todos os campos!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val altura = alturaStr.toDoubleOrNull()
            val largura = larguraStr.toDoubleOrNull()
            val comprimento = comprimentoStr.toDoubleOrNull()

            if (altura == null || largura == null || comprimento == null) {
                Toast.makeText(context, "Digite apenas números válidos!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            checkCurrentCapacityAndUpdate(altura, largura, comprimento, setorId, unidadeId)
        }
    }

    private fun checkCurrentCapacityAndUpdate(altura: Double, largura: Double, comprimento: Double, setorID: Long, unidadeID: Long) {
        val employeeId = SessionManager.getEmployeeId()

        showStockLimitLoadingState("Verificando capacidade atual...")

        capacitySectorInfosViewModel.getSectorCapacityInfos(setorID, employeeId)

        capacitySectorInfosViewModel.capacitySectorInfosResult.observe(viewLifecycleOwner) { infos ->
            capacitySectorInfosViewModel.capacitySectorInfosResult.removeObservers(viewLifecycleOwner)

            if (infos != null) {
                val newCapacity = altura * largura * comprimento
                val totalCapacityNum = SessionManager.getCurrentProfile()!!.maxCapacity
                val usedVolumeNum = totalCapacityNum - infos.remainingVolume

                if (usedVolumeNum > newCapacity) {
                    Toast.makeText(
                        requireContext(),
                        "Volume ocupado atual (${String.format("%.2f", usedVolumeNum)} m³) é maior que a nova capacidade (${String.format("%.2f", newCapacity)} m³)",
                        Toast.LENGTH_LONG
                    ).show()
                    showStockLimitContentState()
                } else {
                    openUpdateCapacityPopUp(altura, largura, comprimento, setorID, unidadeID)
                    showStockLimitContentState()
                }
            } else {
                Toast.makeText(requireContext(), "Erro ao verificar capacidade atual", Toast.LENGTH_SHORT).show()
                showStockLimitContentState()
            }
        }

        capacitySectorInfosViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            capacitySectorInfosViewModel.errorMessage.removeObservers(viewLifecycleOwner)

            if (error.isNotEmpty()) {
                Toast.makeText(requireContext(), "Erro: $error", Toast.LENGTH_SHORT).show()
                showStockLimitErrorState("Erro ao verificar capacidade: $error")
            }
        }

        view?.postDelayed({
            if (stockLimitLoadingLayout?.visibility == View.VISIBLE) {
                showStockLimitErrorState("Tempo limite excedido ao verificar capacidade")
            }
        }, 10000)
    }

    private fun openUpdateCapacityPopUp(altura: Double, largura: Double, comprimento: Double, setorID: Long, unidadeID: Long) {
        val dialogAddProduct = layoutInflater.inflate(R.layout.pop_up_atualizar_capacidade, null)
        val positiveButton = dialogAddProduct.findViewById<Button>(R.id.cadastrarCapacidadeS)
        val negativeButton = dialogAddProduct.findViewById<Button>(R.id.cadastrarCapacidadeN)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogAddProduct)
            .create()

        positiveButton.setOnClickListener {
            dialog.dismiss()

            showStockLimitLoadingState("Atualizando capacidade...")

            viewModel.updateCapacityStock(largura, altura, comprimento, setorID, unidadeID)

            val action = "Usuário adicionou a capacidade do estoque do seu setor e unidade"
            addUserLogsViewModel.addUserLogs(UserLogRequest(SessionManager.getEmployeeId(), action))

            viewModel.updateSuccess.observe(viewLifecycleOwner) { success ->
                if (success) {
                    viewModel.updateSuccess.removeObservers(viewLifecycleOwner)
                    viewModel.errorMessage.removeObservers(viewLifecycleOwner)

                    Toast.makeText(
                        requireContext(),
                        "Capacidade atualizada com sucesso!",
                        Toast.LENGTH_SHORT
                    ).show()

                    alturaEstoque?.text?.clear()
                    larguraEstoque?.text?.clear()
                    comprimentoEstoque?.text?.clear()

                    showStockLimitContentState()
                }
            }

            viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
                if (error.isNotEmpty()) {
                    viewModel.updateSuccess.removeObservers(viewLifecycleOwner)
                    viewModel.errorMessage.removeObservers(viewLifecycleOwner)

                    showStockLimitErrorState("Erro ao atualizar capacidade: $error")

                    view?.postDelayed({
                        showStockLimitContentState()
                    }, 3000)
                }
            }

            view?.postDelayed({
                if (stockLimitLoadingLayout?.visibility == View.VISIBLE) {
                    viewModel.updateSuccess.removeObservers(viewLifecycleOwner)
                    viewModel.errorMessage.removeObservers(viewLifecycleOwner)
                    showStockLimitErrorState("Tempo limite excedido ao atualizar capacidade")

                    view?.postDelayed({
                        showStockLimitContentState()
                    }, 3000)
                }
            }, 15000)
        }

        negativeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.setOnDismissListener {}

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun showStockLimitLoadingState(message: String = "Carregando...") {
        stockLimitLoadingLayout?.visibility = View.VISIBLE
        stockLimitErrorLayout?.visibility = View.GONE
        stockLimitContentLayout?.visibility = View.GONE
        stockLimitLoadingText?.text = message

        Log.d("NavigationStockLimit", "📱 Mostrando estado de loading: $message")
    }

    private fun showStockLimitContentState() {
        stockLimitLoadingLayout?.visibility = View.GONE
        stockLimitErrorLayout?.visibility = View.GONE
        stockLimitContentLayout?.visibility = View.VISIBLE

        Log.d("NavigationStockLimit", "✅ Mostrando conteúdo")
    }

    private fun showStockLimitErrorState(errorMessage: String) {
        stockLimitLoadingLayout?.visibility = View.GONE
        stockLimitErrorLayout?.visibility = View.VISIBLE
        stockLimitContentLayout?.visibility = View.GONE
        stockLimitErrorText?.text = errorMessage

        Log.e("NavigationStockLimit", "❌ Mostrando estado de erro: $errorMessage")
    }
}