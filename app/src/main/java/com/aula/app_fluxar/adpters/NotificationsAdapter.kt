package com.aula.app_fluxar.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aula.app_fluxar.API.model.NotificationItem
import com.aula.app_fluxar.R

class NotificationsAdapter(
    val notifications: MutableList<NotificationItem>,
    val onRemove: (Int) -> Unit
) : RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder>() {

    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleText: TextView = itemView.findViewById(R.id.textView2)
        private val messageText: TextView = itemView.findViewById(R.id.msg_notificacao)
        private val markRead: TextView = itemView.findViewById(R.id.marcar_visualizada)
        private val timeText: TextView = itemView.findViewById(R.id.tempo_notificacao)

        fun bind(notification: NotificationItem, position: Int) {
            titleText.text = notification.titulo
            messageText.text = notification.mensagem
            timeText.text = notification.data

            markRead.setOnClickListener {
                onRemove(position)
                notifyItemRemoved(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.notificacao_item, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(notifications[position], position)
    }

    override fun getItemCount(): Int = notifications.size
}
