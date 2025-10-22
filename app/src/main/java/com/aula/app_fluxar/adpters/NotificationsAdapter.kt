package com.aula.app_fluxar.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aula.app_fluxar.R
import com.aula.app_fluxar.API.model.NotificationResponse

class NotificationsAdapter(
    private val notifications: List<NotificationResponse>
) : RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder>() {

    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleText: TextView = itemView.findViewById(R.id.textView2)
        private val messageText: TextView = itemView.findViewById(R.id.msg_notificacao)
        private val markRead: TextView = itemView.findViewById(R.id.marcar_visualizada)
        private val timeText: TextView = itemView.findViewById(R.id.tempo_notificacao)

        fun bind(notification: NotificationResponse) {
            titleText.text = "ATENÇÃO!"
            messageText.text =
                "Seu estoque está ficando cheio! " +
                        "Restam apenas ${notification.days_to_stockout_pred} dias para o estoque encher!"

            markRead.setOnClickListener {
                itemView.visibility = View.GONE
            }

            timeText.text = notification.data
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.notificacao_item, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(notifications[position])
    }

    override fun getItemCount(): Int = notifications.size
}
