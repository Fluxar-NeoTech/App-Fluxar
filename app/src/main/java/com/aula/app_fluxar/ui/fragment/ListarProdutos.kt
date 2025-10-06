package com.aula.app_fluxar.ui.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aula.app_fluxar.R
import com.aula.app_fluxar.API.viewModel.GetBatchesViewModel
import com.aula.app_fluxar.adpters.BatchAdapter
import com.aula.app_fluxar.sessionManager.SessionManager

class ListarProdutos : Fragment() {

    private lateinit var productListRV: RecyclerView
    private val getBatchesViewModel: GetBatchesViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_layout_listar_produtos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView(view)
        loadBatches()
    }

    private fun setupRecyclerView(view: View) {
        productListRV = view.findViewById(R.id.product_list_RV)
        productListRV.layoutManager = LinearLayoutManager(requireContext())
        productListRV.adapter = BatchAdapter(emptyList())
    }

    private fun loadBatches() {
        val employee = SessionManager.getCurrentProfile()

        if (employee != null) {
            val unitID = employee.unit.id
            val sectorID = employee.sector.id

            getBatchesViewModel.getBatches(unitID, sectorID)
        } else {
            Log.e("ListarProdutos", "Employee não encontrado na sessão")
            return
        }

        getBatchesViewModel.getBatchesResult.observe(viewLifecycleOwner) { batches ->
            if (batches != null && batches.isNotEmpty()) {
                productListRV.adapter = BatchAdapter(batches)
                Log.d("ListarProdutos", "${batches.size} lotes carregados com sucesso")
            } else {
                Log.d("ListarProdutos", "Nenhum lote encontrado para exibir")
            }
        }

        getBatchesViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrEmpty()) {
                Log.e("ListarProdutos", "Erro ao carregar lotes: $error")
            }
        }
    }
}