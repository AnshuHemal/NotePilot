package com.white.notepilot.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.PhoneAuthProvider
import com.white.notepilot.data.auth.PhoneAuthRepository
import com.white.notepilot.data.auth.PhoneAuthState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PhoneAuthViewModel @Inject constructor(
    private val repository: PhoneAuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow<PhoneAuthState>(PhoneAuthState.Idle)
    val state: StateFlow<PhoneAuthState> = _state.asStateFlow()

    private var sendJob: Job? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    fun sendOtp(phoneNumber: String, activity: Activity) {
        sendJob?.cancel()
        sendJob = viewModelScope.launch {
            repository.sendOtp(phoneNumber, activity, resendToken).collect { state ->
                _state.value = state
                if (state is PhoneAuthState.OtpSent) {
                    resendToken = state.resendToken
                }
            }
        }
    }

    fun resendOtp(phoneNumber: String, activity: Activity) {
        sendOtp(phoneNumber, activity)
    }

    fun verifyOtp(verificationId: String, otp: String) {
        viewModelScope.launch {
            _state.value = PhoneAuthState.Verifying
            val result = repository.verifyOtp(verificationId, otp)
            _state.value = if (result.isSuccess) {
                PhoneAuthState.Success(result.getOrThrow())
            } else {
                PhoneAuthState.Error(result.exceptionOrNull()?.message ?: "Invalid OTP")
            }
        }
    }

    fun reset() {
        sendJob?.cancel()
        _state.value = PhoneAuthState.Idle
        resendToken = null
    }
}
