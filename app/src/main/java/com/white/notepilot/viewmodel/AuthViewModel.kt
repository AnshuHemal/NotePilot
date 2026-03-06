package com.white.notepilot.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.white.notepilot.data.auth.AuthRepository
import com.white.notepilot.data.auth.AuthState
import com.white.notepilot.data.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    init {
        checkAuthStatus()
    }
    
    private fun checkAuthStatus() {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser != null) {
            _authState.value = AuthState.Success(currentUser.uid)
        }
    }
    
    fun signInWithGoogle(account: GoogleSignInAccount) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            val result = authRepository.signInWithGoogle(account)
            
            _authState.value = if (result.isSuccess) {
                val user = result.getOrNull()
                AuthState.Success(user?.uid ?: "")
            } else {
                AuthState.Error(result.exceptionOrNull()?.message ?: "Sign in failed")
            }
        }
    }
    
    fun signOut() {
        authRepository.signOut()
        _authState.value = AuthState.Idle
    }
    
    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }
    
    fun getCurrentUser(): User? {
        return authRepository.getCurrentUser()
    }
    
    fun getGoogleSignInClient() = authRepository.getGoogleSignInClient()
}
