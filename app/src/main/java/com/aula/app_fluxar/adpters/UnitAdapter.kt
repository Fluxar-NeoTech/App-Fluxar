package com.aula.app_fluxar.adpters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aula.app_fluxar.R
import com.aula.app_fluxar.API.model.Unit as UnitModel

class UnitAdapter(private val unidades: List<Triple<UnitModel, Float, Int>>) : RecyclerView.Adapter<UnitAdapter.UnitViewHolder>() {

    // Lista visível no RecyclerView (começa com todos)
    private var filteredList: List<Triple<UnitModel, Float, Int>> = unidades.toList()

    inner class UnitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titulo: TextView = itemView.findViewById(R.id.txtTitulo)
        val distancia: TextView = itemView.findViewById(R.id.txtDistancia)
        val disponivel: TextView = itemView.findViewById(R.id.txtDisponivel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UnitViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.unidade_item, parent, false)
        return UnitViewHolder(view)
    }

    override fun onBindViewHolder(holder: UnitViewHolder, position: Int) {
        val (unidade, distancia, disponibilidade) = filteredList[position]
        holder.titulo.text = unidade.name
        holder.distancia.text = "Distância: %.2f km".format(distancia)
        holder.disponivel.text = "$disponibilidade m³\nDisponíveis"

        Log.d("RV_BIND", "Exibindo: ${holder.titulo.text}")
    }

    override fun getItemCount(): Int = filteredList.size

    // Ordena por distância (menor primeiro)
    fun sortByDistance() {
        filteredList = unidades.sortedBy { it.second }
        notifyDataSetChanged()
    }

    // Ordena por disponibilidade (maior primeiro)
    fun sortByDisponibilidade() {
        filteredList = unidades.sortedByDescending { it.third }
        notifyDataSetChanged()
    }

    // Restaura lista original
    fun reset() {
        filteredList = unidades.toList()
        notifyDataSetChanged()
    }
}
