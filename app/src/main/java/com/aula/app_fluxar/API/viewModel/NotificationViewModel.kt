package com.aula.app_fluxar.API.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.aula.app_fluxar.API.model.NotificationItem

class NotificationsViewModel : ViewModel() {

    private val _notifications = MutableLiveData<MutableList<NotificationItem>>(mutableListOf())
    val notifications: LiveData<MutableList<NotificationItem>> get() = _notifications

    fun addNotification(notification: NotificationItem) {
        val list = _notifications.value ?: mutableListOf()
        list.add(0, notification)
        _notifications.value = list
    }

    fun removeNotification(index: Int) {
        val list = _notifications.value ?: mutableListOf()
        if (index in list.indices) {
            list.removeAt(index)
            _notifications.value = list
        }
    }

    fun clearAll() {
        _notifications.value = mutableListOf()
    }
}
