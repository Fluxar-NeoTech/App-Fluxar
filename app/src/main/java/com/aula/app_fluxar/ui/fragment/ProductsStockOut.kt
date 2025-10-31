package com.aula.app_fluxar.ui.fragment

import StockOutViewModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aula.app_fluxar.API.model.StockOutRequest
import com.aula.app_fluxar.R
import com.aula.app_fluxar.adpters.StockOutAdapter

class ProductsStockOut : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: StockOutAdapter
    private val viewModel: StockOutViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_nav_stockout, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.stockout_rv)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = StockOutAdapter(emptyList())
        recyclerView.adapter = adapter

        val loadingLayout = view.findViewById<LinearLayout>(R.id.homeLoadingLayout)
        val errorLayout = view.findViewById<LinearLayout>(R.id.homeErrorLayout)
        val errorText = view.findViewById<TextView>(R.id.homeErrorText)
        val retryButton = view.findViewById<Button>(R.id.homeRetryButton)

        retryButton.setOnClickListener {
            val req = StockOutRequest(industria_id = 1, setor_id = 1)
            viewModel.fetchStockOut(req)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            loadingLayout.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { msg ->
            if (msg.isNotEmpty()) {
                errorLayout.visibility = View.VISIBLE
                errorText.text = msg
            } else {
                errorLayout.visibility = View.GONE
            }
        }

        viewModel.productStockOut.observe(viewLifecycleOwner) { list ->
            if (list != null) {
                val filteredList = list.filter { it.days_to_stockout_pred < 7 }
                adapter.updateList(filteredList)
                recyclerView.visibility = if (filteredList.isNotEmpty()) View.VISIBLE else View.GONE
            }
        }

        val req = StockOutRequest(industria_id = 1, setor_id = 1)
        viewModel.fetchStockOut(req)
    }
}


