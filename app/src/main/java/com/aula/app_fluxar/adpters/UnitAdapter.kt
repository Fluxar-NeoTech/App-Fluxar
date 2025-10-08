package com.aula.app_fluxar.adpters

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.aula.app_fluxar.API.model.UnitInfos
import com.aula.app_fluxar.R

class UnitAdapter(private val unidades: List<Triple<UnitInfos, Float, Double>>) : RecyclerView.Adapter<UnitAdapter.UnitViewHolder>() {

    private var filteredList: List<Triple<UnitInfos, Float, Double>> = unidades.toList()

    inner class UnitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titulo: TextView = itemView.findViewById(R.id.txtTitulo)
        val distancia: TextView = itemView.findViewById(R.id.txtDistancia)
        val disponivel: TextView = itemView.findViewById(R.id.txtDisponivel)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val (unit, distance, disponibility) = filteredList[position]
                    navigateToUnitDetails(unit, distance, disponibility)
                }
            }
        }

        private fun navigateToUnitDetails(unit: UnitInfos, distance: Float, disponibility: Double) {
            val bundle = Bundle().apply {
                putParcelable("unit", unit)
                putFloat("distance", distance)
                putDouble("disponibility", disponibility)
            }

            try {
                Navigation.findNavController(itemView).navigate(
                    R.id.action_navigationUnidades_to_unitDetails,
                    bundle
                )
            } catch (e: Exception) {
                Log.e("UnitAdapter", "Erro na navegação: ${e.message}")
            }
        }
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
    }

    override fun getItemCount(): Int = filteredList.size

    fun sortByDistance() {
        filteredList = unidades.sortedBy { it.second }
        notifyDataSetChanged()
    }

    fun sortByDisponibilidade() {
        filteredList = unidades.sortedByDescending { it.third }
        notifyDataSetChanged()
    }

    fun reset() {
        filteredList = unidades.toList()
        notifyDataSetChanged()
    }
}