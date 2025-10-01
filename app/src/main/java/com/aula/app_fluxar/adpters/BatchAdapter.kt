package com.aula.app_fluxar.adpters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aula.app_fluxar.R
import com.aula.app_fluxar.API.model.Batch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class BatchAdapter(private val batches: List<Batch>) : RecyclerView.Adapter<BatchAdapter.BatchViewHolder>() {

    inner class BatchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val produtoNome: TextView = itemView.findViewById(R.id.produto_nome)
        val produtoLote: TextView = itemView.findViewById(R.id.produto_lote)
        val produtoDataVal: TextView = itemView.findViewById(R.id.produto_data_val)
        val produtoMedidas: TextView = itemView.findViewById(R.id.produto_medidas)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BatchViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.produto_item, parent, false)
        return BatchViewHolder(view)
    }

    override fun onBindViewHolder(holder: BatchViewHolder, position: Int) {
        val batch = batches[position]

        holder.produtoNome.text = batch.productName ?: "Produto não especificado"

        holder.produtoLote.text = batch.batchCode ?: "N/A"

        batch.expirationDate?.let { dateString ->
            holder.produtoDataVal.text = formatDate(dateString)
        } ?: run {
            holder.produtoDataVal.text = "N/A"
        }

        holder.produtoMedidas.text = formatMeasures(batch)
    }

    override fun getItemCount(): Int = batches.size

    private fun formatDate(dateString: String): String {
        return try {
            val localDate = LocalDate.parse(dateString)
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            localDate.format(formatter)
        } catch (e: DateTimeParseException) {
            try {
                val parts = dateString.split("-")
                if (parts.size == 3) {
                    "${parts[2]}/${parts[1]}/${parts[0]}"
                } else {
                    dateString
                }
            } catch (e2: Exception) {
                dateString
            }
        } catch (e: Exception) {
            dateString
        }
    }

    private fun formatMeasures(batch: Batch): String {
        val length = batch.length ?: 0.0
        val width = batch.width ?: 0.0
        val height = batch.height ?: 0.0
        return "${length.toInt()} × ${width.toInt()} × ${height.toInt()} m"
    }
}