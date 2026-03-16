package com.white.notepilot.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.white.notepilot.R
import com.white.notepilot.data.auth.PhoneAuthState
import com.white.notepilot.ui.navigation.Routes
import com.white.notepilot.viewmodel.PhoneAuthViewModel
import kotlinx.coroutines.delay

private val OtpAccentBlue = Color(0xFF3D5AFE)

@Composable
fun OtpVerificationScreen(
    navController: NavHostController,
    phoneNumber: String,
    phoneAuthViewModel: PhoneAuthViewModel = hiltViewModel()
) {
    val state by phoneAuthViewModel.state.collectAsState()
    val context = LocalContext.current
    val activity = context as Activity

    var otp by remember { mutableStateOf("") }
    var countdown by remember { mutableIntStateOf(60) }
    var canResend by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    // Persist verificationId — OtpSent state is replaced by Verifying, so we cache it
    val rememberedVerificationId = remember { mutableStateOf<String?>(null) }
    LaunchedEffect(state) {
        if (state is PhoneAuthState.OtpSent) {
            rememberedVerificationId.value = (state as PhoneAuthState.OtpSent).verificationId
        }
    }

    // Auto-verify the moment all 6 digits are entered
    LaunchedEffect(otp) {
        if (otp.length == 6) {
            val vid = rememberedVerificationId.value
            if (vid != null) {
                phoneAuthViewModel.verifyOtp(vid, otp)
            }
        }
    }

    // Countdown timer
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        while (countdown > 0) {
            delay(1000L)
            countdown--
        }
        canResend = true
    }

    // Navigate on success (covers both manual OTP and test-number instant verification)
    LaunchedEffect(state) {
        if (state is PhoneAuthState.Success) {
            navController.navigate(Routes.Home.route) {
                popUpTo(Routes.Login.route) { inclusive = true }
            }
        }
    }

    val isVerifying = state is PhoneAuthState.Verifying
    val isError = state is PhoneAuthState.Error

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Back button — back_arrow points left
            IconButton(
                onClick = {
                    phoneAuthViewModel.reset()
                    navController.popBackStack()
                },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            ) {
                Icon(
                    painter = painterResource(R.drawable.back_arrow),
                    contentDescription = "Back",
                    tint = Color(0xFF111827),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Verify your number",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Phone number — no edit icon
            Text(
                text = "Code sent to $phoneNumber",
                fontSize = 14.sp,
                color = Color(0xFF6B7280)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Hidden input capturing all 6 digits
            BasicTextField(
                value = otp,
                onValueChange = { new ->
                    if (new.length <= 6 && new.all { it.isDigit() }) otp = new
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                cursorBrush = SolidColor(Color.Transparent),
                textStyle = TextStyle(color = Color.Transparent, fontSize = 1.sp),
                modifier = Modifier
                    .size(1.dp)
                    .focusRequester(focusRequester)
            ) { _ -> }

            // 6 OTP digit boxes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
            ) {
                repeat(6) { index ->
                    val digit = otp.getOrNull(index)?.toString() ?: ""
                    val isFocused = otp.length == index && !isVerifying

                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .border(
                                width = if (isFocused) 2.dp else 1.dp,
                                color = when {
                                    isError -> MaterialTheme.colorScheme.error
                                    isFocused -> OtpAccentBlue
                                    digit.isNotEmpty() -> OtpAccentBlue.copy(alpha = 0.4f)
                                    else -> Color(0xFFE5E7EB)
                                },
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = digit,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Error message
            AnimatedVisibility(visible = isError, enter = fadeIn(), exit = fadeOut()) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = (state as? PhoneAuthState.Error)?.message ?: "",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Verify button — manual fallback if auto-verify didn't fire
            Button(
                onClick = {
                    val vid = rememberedVerificationId.value
                    if (otp.length == 6 && vid != null) {
                        phoneAuthViewModel.verifyOtp(vid, otp)
                    }
                },
                enabled = otp.length == 6 && !isVerifying,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OtpAccentBlue)
            ) {
                if (isVerifying) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Verify OTP",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Resend row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Didn't receive the code? ",
                    fontSize = 13.sp,
                    color = Color(0xFF6B7280)
                )
                if (canResend) {
                    TextButton(onClick = {
                        phoneAuthViewModel.reset()
                        navController.popBackStack()
                    }) {
                        Text(
                            text = "Resend",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OtpAccentBlue
                        )
                    }
                } else {
                    Text(
                        text = "Resend in ${countdown}s",
                        fontSize = 13.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
