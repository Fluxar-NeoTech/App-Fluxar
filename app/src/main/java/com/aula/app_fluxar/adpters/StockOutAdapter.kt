package com.aula.app_fluxar.adpters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aula.app_fluxar.API.model.StockOutResponse
import com.aula.app_fluxar.R

class StockOutAdapter(
    private var stockOutList: List<StockOutResponse>
) : RecyclerView.Adapter<StockOutAdapter.StockOutViewHolder>() {

    inner class StockOutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val msgTextView: TextView = itemView.findViewById(R.id.msg_stockout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockOutViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.stockout_item, parent, false)
        return StockOutViewHolder(view)
    }

    override fun onBindViewHolder(holder: StockOutViewHolder, position: Int) {
        val item = stockOutList[position]
        val days = item.days_to_stockout_pred
        val textRemaingDays = if (days > 1) listOf("Restam", "dias") else listOf("Resta", "dia")
        holder.msgTextView.text =
            "Ruptura do produto ${item.produto_nome} iminente! ${textRemaingDays[0]} apenas ${"%.0f".format(days)} ${textRemaingDays[1]} de suprimento."
    }

    override fun getItemCount() = stockOutList.size

    fun updateList(newItems: List<StockOutResponse>) {
        val newList = stockOutList.toMutableList()
        newList.addAll(newItems)
        stockOutList = newList
        notifyDataSetChanged()
    }
}
