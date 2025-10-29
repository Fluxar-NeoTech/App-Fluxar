package com.aula.app_fluxar.API.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.aula.app_fluxar.API.model.NotificationItem

class NotificationsViewModel : ViewModel() {
    private val _notifications = MutableLiveData<MutableList<NotificationItem>>(mutableListOf())
    val notifications: LiveData<MutableList<NotificationItem>> = _notifications

    fun addNotification(notification: NotificationItem) {
        val currentList = _notifications.value ?: mutableListOf()
        val updatedList = currentList.toMutableList().apply { add(notification) }
        _notifications.value = updatedList
    }

    fun removeNotification(index: Int) {
        val currentList = _notifications.value ?: mutableListOf()
        if (index in currentList.indices) {
            val updatedList = currentList.toMutableList().apply { removeAt(index) }
            _notifications.value = updatedList
        }
    }

    fun clearAll() {
        _notifications.value = mutableListOf()
    }
}
