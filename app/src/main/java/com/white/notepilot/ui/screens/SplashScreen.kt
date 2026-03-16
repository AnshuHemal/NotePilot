package com.white.notepilot.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.white.notepilot.R
import com.white.notepilot.data.auth.AuthState
import com.white.notepilot.ui.navigation.Routes
import com.white.notepilot.ui.theme.NotesTheme
import com.white.notepilot.viewmodel.AuthViewModel
import com.white.notepilot.viewmodel.OnboardingViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull

@Composable
fun SplashScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel = hiltViewModel(),
    onboardingViewModel: OnboardingViewModel = hiltViewModel()
) {
    val alpha = remember { Animatable(0f) }
    val authState by authViewModel.authState.collectAsState()
    val onboardingCompleted by onboardingViewModel.isCompleted.collectAsState()

    LaunchedEffect(authState, onboardingCompleted) {
        // Wait until onboarding flag is loaded (non-null)
        if (onboardingCompleted == null) return@LaunchedEffect

        alpha.animateTo(targetValue = 1f, animationSpec = tween(durationMillis = 1000))
        delay(1000)

        // Safety timeout — if still Idle/Loading after 5s, go to Login
        val resolvedState = withTimeoutOrNull(5_000) {
            var state = authState
            while (state == AuthState.Idle || state == AuthState.Loading) {
                delay(100)
                state = authState
            }
            state
        } ?: AuthState.Error("Timeout")

        when (resolvedState) {
            is AuthState.Success -> {
                // Logged in — check if they've seen onboarding
                val destination = if (onboardingCompleted == true) Routes.Home.route
                                  else Routes.Onboarding.route
                navController.navigate(destination) {
                    popUpTo(Routes.Splash.route) { inclusive = true }
                }
            }
            else -> {
                // Not logged in — show onboarding on first launch, Login on return visits
                val destination = if (onboardingCompleted == true) Routes.Login.route
                                  else Routes.Onboarding.route
                navController.navigate(destination) {
                    popUpTo(Routes.Splash.route) { inclusive = true }
                }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = stringResource(R.string.app_logo),
                    modifier = Modifier
                        .size(200.dp)
                        .alpha(alpha.value)
                )

                Text(
                    text = stringResource(R.string.capture_your_thoughts),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.W600
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .alpha(alpha.value),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Developed by",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Hemal Katariya",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    NotesTheme {
        SplashScreen(navController = rememberNavController())
    }
}
