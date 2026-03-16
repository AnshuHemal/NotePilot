package com.white.notepilot.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.white.notepilot.R
import com.white.notepilot.data.auth.AuthState
import com.white.notepilot.data.auth.PhoneAuthState
import com.white.notepilot.ui.navigation.Routes
import com.white.notepilot.ui.components.CountryPickerSheet
import com.white.notepilot.viewmodel.AuthViewModel
import com.white.notepilot.viewmodel.PhoneAuthViewModel

private val AccentBlue = Color(0xFF3D5AFE)
private val FieldBorder = Color(0xFFE5E7EB)
private val FieldBg    = Color(0xFFFFFFFF)

@Composable
fun LoginScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel = hiltViewModel(),
    phoneAuthViewModel: PhoneAuthViewModel = hiltViewModel()
) {
    val authState    by authViewModel.authState.collectAsState()
    val phoneState   by phoneAuthViewModel.state.collectAsState()
    val context      = LocalContext.current
    val activity     = context as Activity

    var phoneNumber  by remember { mutableStateOf("") }
    var selectedCode by remember { mutableStateOf(CountryCode("+91", "India", "🇮🇳")) }
    var showPicker   by remember { mutableStateOf(false) }

    // Google sign-in launcher
    val googleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account?.let { authViewModel.signInWithGoogle(it) }
            } catch (_: ApiException) { authViewModel.resetAuthState() }
        } else { authViewModel.resetAuthState() }
    }

    // Navigate on Google success
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            navController.navigate(Routes.Home.route) {
                popUpTo(Routes.Login.route) { inclusive = true }
            }
        }
    }

    // Navigate on Phone OTP sent
    LaunchedEffect(phoneState) {
        if (phoneState is PhoneAuthState.OtpSent) {
            val fullNumber = "${selectedCode.dialCode}${phoneNumber.trim()}"
            navController.navigate(Routes.OtpVerification.createRoute(fullNumber))
        }
        if (phoneState is PhoneAuthState.Success) {
            navController.navigate(Routes.Home.route) {
                popUpTo(Routes.Login.route) { inclusive = true }
            }
        }
    }

    val isLoading = authState is AuthState.Loading ||
                    phoneState is PhoneAuthState.SendingOtp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp),
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Logo + title
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = null,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Login to ${stringResource(R.string.app_name)}",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Please enter your mobile number to login",
                fontSize = 14.sp,
                color = Color(0xFF6B7280)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ── Phone input row ───────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Country code picker button
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, FieldBorder, RoundedCornerShape(12.dp))
                        .background(FieldBg)
                        .clickable { showPicker = true }
                        .padding(horizontal = 14.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = selectedCode.flag, fontSize = 18.sp)
                    Text(
                        text = selectedCode.dialCode,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF111827)
                    )
                    Icon(
                        painter = painterResource(R.drawable.arrow_right),
                        contentDescription = null,
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size(14.dp)
                    )
                }

                // Phone number field
                TextField(
                    value = phoneNumber,
                    onValueChange = { if (it.length <= 15) phoneNumber = it },
                    placeholder = {
                        Text("Your Mobile Number", color = Color(0xFF9CA3AF), fontSize = 15.sp)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = FieldBg,
                        unfocusedContainerColor = FieldBg,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color(0xFF111827),
                        unfocusedTextColor = Color(0xFF111827),
                        cursorColor = AccentBlue
                    ),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, FieldBorder, RoundedCornerShape(12.dp))
                )
            }

            // Phone error
            if (phoneState is PhoneAuthState.Error) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = (phoneState as PhoneAuthState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // ── Continue button ───────────────────────────────────────────────
            Button(
                onClick = {
                    if (phoneNumber.isNotBlank()) {
                        val full = "${selectedCode.dialCode}${phoneNumber.trim()}"
                        phoneAuthViewModel.sendOtp(full, activity)
                    }
                },
                enabled = phoneNumber.isNotBlank() && !isLoading,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) {
                if (phoneState is PhoneAuthState.SendingOtp) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Continue to Login", fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── OR divider ────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = FieldBorder)
                Text("or", fontSize = 13.sp, color = Color(0xFF9CA3AF))
                HorizontalDivider(modifier = Modifier.weight(1f), color = FieldBorder)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Google button ─────────────────────────────────────────────────
            Button(
                onClick = {
                    val intent = authViewModel.getGoogleSignInClient().signInIntent
                    googleLauncher.launch(intent)
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
            ) {
                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = AccentBlue,
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.google_icon),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = stringResource(R.string.sign_in_with_google),
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp,
                            color = Color(0xFF374151)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Terms footer ──────────────────────────────────────────────────
            Text(
                text = buildAnnotatedString {
                    append("By continuing you agree to our ")
                    withStyle(SpanStyle(fontWeight = FontWeight.SemiBold,
                        textDecoration = TextDecoration.Underline, color = Color(0xFF111827))) {
                        append("terms & conditions")
                    }
                },
                fontSize = 12.sp,
                color = Color(0xFF6B7280),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Country picker bottom sheet
        if (showPicker) {
            CountryPickerSheet(
                onDismiss = { showPicker = false },
                onSelect = { code ->
                    selectedCode = code
                    showPicker = false
                }
            )
        }
    }
}
