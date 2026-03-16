package com.white.notepilot.data.auth

import com.google.firebase.auth.PhoneAuthProvider

sealed class PhoneAuthState {
    object Idle : PhoneAuthState()
    object SendingOtp : PhoneAuthState()
    data class OtpSent(
        val verificationId: String,
        val resendToken: PhoneAuthProvider.ForceResendingToken,
        val phoneNumber: String
    ) : PhoneAuthState()
    object Verifying : PhoneAuthState()
    data class Success(val userId: String) : PhoneAuthState()
    data class Error(val message: String) : PhoneAuthState()
}
