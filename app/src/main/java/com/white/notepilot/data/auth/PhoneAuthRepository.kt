package com.white.notepilot.data.auth

import android.app.Activity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhoneAuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {
    /** Sends OTP and emits [PhoneAuthState] updates via a Flow. */
    fun sendOtp(
        phoneNumber: String,
        activity: Activity,
        resendToken: PhoneAuthProvider.ForceResendingToken? = null
    ): Flow<PhoneAuthState> = callbackFlow {

        trySend(PhoneAuthState.SendingOtp)

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // Auto-retrieval or instant verification
                trySend(PhoneAuthState.Verifying)
                firebaseAuth.signInWithCredential(credential)
                    .addOnSuccessListener { result ->
                        trySend(PhoneAuthState.Success(result.user?.uid ?: ""))
                        close()
                    }
                    .addOnFailureListener { e ->
                        trySend(PhoneAuthState.Error(e.message ?: "Verification failed"))
                        close()
                    }
            }

            override fun onVerificationFailed(e: com.google.firebase.FirebaseException) {
                trySend(PhoneAuthState.Error(e.message ?: "Failed to send OTP"))
                close()
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                trySend(PhoneAuthState.OtpSent(verificationId, token, phoneNumber))
                // Don't close — we keep the flow open until the caller cancels
            }
        }

        val optionsBuilder = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)

        if (resendToken != null) optionsBuilder.setForceResendingToken(resendToken)

        PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build())

        awaitClose()
    }

    /** Verifies the OTP code entered by the user. */
    suspend fun verifyOtp(verificationId: String, otp: String): Result<String> {
        return try {
            val credential = PhoneAuthProvider.getCredential(verificationId, otp)
            val result = firebaseAuth.signInWithCredential(credential).await()
            Result.success(result.user?.uid ?: "")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
