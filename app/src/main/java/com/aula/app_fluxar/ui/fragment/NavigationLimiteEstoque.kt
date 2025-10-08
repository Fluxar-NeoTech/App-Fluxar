package com.aula.app_fluxar.ui.fragment

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.aula.app_fluxar.API.viewModel.CapacityStockViewModel
import com.aula.app_fluxar.R
import com.aula.app_fluxar.ui.activity.MainActivity
import com.google.android.material.textfield.TextInputEditText

class NavigationLimiteEstoque : Fragment() {
    private val viewModel: CapacityStockViewModel by viewModels()
    private lateinit var alturaEstoque: TextInputEditText
    private lateinit var larguraEstoque: TextInputEditText
    private lateinit var comprimentoEstoque: TextInputEditText
    private lateinit var concluirBt: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_layout_nav_limite_estoque, container, false)

        alturaEstoque = view.findViewById(R.id.inputAlturaCapacidade)
        larguraEstoque = view.findViewById(R.id.inputlarguraCapacidade)
        comprimentoEstoque = view.findViewById(R.id.inputComprimentoCapacidade)
        concluirBt = view.findViewById(R.id.concluirBt)

        setupListeners()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.capacityStockResult.observe(viewLifecycleOwner) { result ->
            if (result != null) {
                Toast.makeText(
                    requireContext(),
                    "Capacidade salva com sucesso!",
                    Toast.LENGTH_SHORT
                ).show()

                alturaEstoque.text?.clear()
                larguraEstoque.text?.clear()
                comprimentoEstoque.text?.clear()

                viewModel.clearResult()
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (error.isNotEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                viewModel.clearResult()
            }
        }
    }

    private fun setupListeners() {
        concluirBt.setOnClickListener {
            val profile = com.aula.app_fluxar.sessionManager.SessionManager.getCurrentProfile()

            if (profile == null) {
                Toast.makeText(context, "Erro: Perfil não carregado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val setorId = profile.sector.id
            val unidadeId = profile.unit.id

            val alturaStr = alturaEstoque.text.toString().trim()
            val larguraStr = larguraEstoque.text.toString().trim()
            val comprimentoStr = comprimentoEstoque.text.toString().trim()

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

            openUpdateCapacityPopUp(altura, largura, comprimento, setorId, unidadeId)
        }
    }

    private fun openUpdateCapacityPopUp(altura: Double, largura: Double, comprimento: Double, setorID: Long, unidadeID: Long) {
        val dialogAddProduct = layoutInflater.inflate(R.layout.pop_up_atualizar_capacidade, null)
        val positiveButton = dialogAddProduct.findViewById<Button>(R.id.cadastrarCapacidadeS)
        val negativeButton = dialogAddProduct.findViewById<Button>(R.id.cadastrarCapacidadeN)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogAddProduct)
            .create()

        positiveButton.setOnClickListener {
            viewModel.updateCapacityStock(largura, altura, comprimento, setorID, unidadeID)

            viewModel.updateSuccess.observe(viewLifecycleOwner) { success ->
                if (success) {
                    dialog.dismiss()
                }
            }
        }

        negativeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }
}
