package com.white.notepilot.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.white.notepilot.data.model.Notification
import com.white.notepilot.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val repository: NotificationRepository
) : ViewModel() {

    val notifications: StateFlow<List<Notification>> = repository.getAllNotifications()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val unreadCount: StateFlow<Int> = repository.getUnreadCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    fun markAsRead(notificationId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.markAsRead(notificationId)
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.markAllAsRead()
        }
    }

    fun deleteNotification(notificationId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteNotification(notificationId)
        }
    }

    fun deleteAllNotifications() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllNotifications()
        }
    }

    fun cleanupOldNotifications() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.cleanupOldNotifications(30)
        }
    }
}
