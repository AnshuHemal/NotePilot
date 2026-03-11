package com.white.notepilot.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.white.notepilot.data.model.AppUpdate
import com.white.notepilot.data.repository.UpdateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val updateRepository: UpdateRepository
) : ViewModel() {
    
    private val _updateInfo = MutableStateFlow<AppUpdate?>(null)
    val updateInfo: StateFlow<AppUpdate?> = _updateInfo.asStateFlow()
    
    private val _showUpdateDialog = MutableStateFlow(false)
    val showUpdateDialog: StateFlow<Boolean> = _showUpdateDialog.asStateFlow()
    
    private val _isForceUpdate = MutableStateFlow(false)
    val isForceUpdate: StateFlow<Boolean> = _isForceUpdate.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    fun checkForUpdates(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                val updateInfo = updateRepository.getUpdateInfo()
                
                if (updateInfo != null && updateInfo.isEnabled) {
                    val currentVersion = updateRepository.getCurrentAppVersion(context)
                    
                    val isUpdateRequired = updateRepository.isUpdateRequired(
                        currentVersion, 
                        updateInfo.minimumVersion
                    )
                    
                    val isUpdateAvailable = updateRepository.isUpdateAvailable(
                        currentVersion, 
                        updateInfo.latestVersion
                    )
                    
                    if (isUpdateRequired || (isUpdateAvailable && updateInfo.forceUpdate)) {
                        _updateInfo.value = updateInfo
                        _isForceUpdate.value = isUpdateRequired || updateInfo.forceUpdate
                        _showUpdateDialog.value = true
                    }
                }
            } catch (e: Exception) {
                // Handle error silently
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun dismissUpdateDialog() {
        if (!_isForceUpdate.value) {
            _showUpdateDialog.value = false
        }
    }
    
    fun resetUpdateState() {
        _showUpdateDialog.value = false
        _isForceUpdate.value = false
        _updateInfo.value = null
    }
}