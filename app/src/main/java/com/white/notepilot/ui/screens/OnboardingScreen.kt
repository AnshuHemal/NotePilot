package com.white.notepilot.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.white.notepilot.R
import com.white.notepilot.ui.navigation.Routes
import com.white.notepilot.viewmodel.AuthViewModel
import com.white.notepilot.viewmodel.OnboardingViewModel
import kotlinx.coroutines.launch

private val AccentBlue  = Color(0xFF3D5AFE)
private val MockDivider = Color(0xFFF1F5F9)
private val IcBg1 = Color(0xFFDCFCE7); private val Ic1 = Color(0xFF16A34A)
private val IcBg2 = Color(0xFFFFE4E6); private val Ic2 = Color(0xFFE11D48)
private val IcBg3 = Color(0xFFDBEAFE); private val Ic3 = Color(0xFF2563EB)
private val IcBg4 = Color(0xFFF3E8FF); private val Ic4 = Color(0xFF7C3AED)

@Composable
fun OnboardingScreen(
    navController: NavHostController,
    viewModel: OnboardingViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val scope = rememberCoroutineScope()
    val current = pagerState.currentPage

    fun finish() {
        viewModel.completeOnboarding()
        // Go to Home if already logged in, otherwise Login
        val destination = if (authViewModel.getCurrentUser() != null) Routes.Home.route
                          else Routes.Login.route
        navController.navigate(destination) {
            popUpTo(Routes.Onboarding.route) { inclusive = true }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF0F2F5))) {
        Column(modifier = Modifier.fillMaxSize()) {

            Box(
                modifier = Modifier.fillMaxWidth().statusBarsPadding()
                    .padding(end = 8.dp, top = 4.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                TextButton(onClick = { finish() }) {
                    Text("Skip", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = AccentBlue)
                }
            }

            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { index ->
                when (index) {
                    0 -> PageContent("Capture Every Thought",
                        "Create rich notes with text, images and voice. Your ideas deserve a beautiful home.",
                        isActive = current == 0) { MockNoteEditor() }
                    1 -> PageContent("Organise with Categories",
                        "Tag and group your notes into categories. Find anything in seconds.",
                        isActive = current == 1) { MockCategories() }
                    2 -> PageContent("Sync Across Devices",
                        "Your notes are safely backed up to the cloud and available everywhere.",
                        isActive = current == 2) { MockSync() }
                    else -> PageContent("Lock What Matters",
                        "Protect sensitive notes with a PIN or biometric lock. Stay private.",
                        isActive = current == 3) { MockLock() }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().navigationBarsPadding()
                    .padding(horizontal = 28.dp, vertical = 28.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PagerDots(pageCount = 4, currentPage = current)
                Button(
                    onClick = {
                        if (current == 3) finish()
                        else scope.launch { pagerState.animateScrollToPage(current + 1) }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    modifier = Modifier.height(50.dp).width(130.dp)
                ) {
                    Text(
                        text = if (current == 3) "Get Started" else "Next",
                        fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun PageContent(
    title: String,
    description: String,
    isActive: Boolean,
    mockContent: @Composable () -> Unit
) {
    val cardAlpha by animateFloatAsState(
        targetValue = if (isActive) 1f else 0.55f,
        animationSpec = tween(400), label = "alpha"
    )
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier.fillMaxWidth().weight(1f)
                .graphicsLayer { alpha = cardAlpha }
                .shadow(elevation = 6.dp, shape = RoundedCornerShape(24.dp), clip = false)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .padding(20.dp)
        ) { mockContent() }
        Spacer(modifier = Modifier.height(28.dp))
        Text(title, fontSize = 22.sp, fontWeight = FontWeight.Bold,
            color = Color(0xFF111827), textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(10.dp))
        Text(description, fontSize = 14.sp, color = Color(0xFF6B7280),
            textAlign = TextAlign.Center, lineHeight = 22.sp)
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun PagerDots(pageCount: Int, currentPage: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(pageCount) { index ->
            val isSelected = index == currentPage
            val dotWidth by animateDpAsState(
                targetValue = if (isSelected) 22.dp else 8.dp,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "dot$index"
            )
            val dotColor by animateColorAsState(
                targetValue = if (isSelected) AccentBlue else Color(0xFFCBD5E1),
                animationSpec = tween(300), label = "dotColor$index"
            )
            Box(modifier = Modifier.height(8.dp).width(dotWidth).clip(CircleShape).background(dotColor))
        }
    }
}

@Composable
private fun MockNoteEditor() {
    Column(modifier = Modifier.fillMaxSize()) {
        MockHeader(R.drawable.back_arrow, "My Notes")
        Divider(color = MockDivider)
        Spacer(modifier = Modifier.height(8.dp))
        MockRow(IcBg1, Ic1, R.drawable.edit, "Meeting Notes", "Discussed Q4 targets and roadmap", "Today")
        Divider(color = MockDivider)
        MockRow(IcBg2, Ic2, R.drawable.edit, "Shopping List", "Milk, eggs, bread, coffee beans", "Yesterday")
        Divider(color = MockDivider)
        MockRow(IcBg3, Ic3, R.drawable.edit, "Project Ideas", "New feature concepts for the app", "2 Jan")
        Divider(color = MockDivider)
        MockRow(IcBg4, Ic4, R.drawable.edit, "Travel Plans", "Flights booked, hotel confirmed", "1 Jan")
    }
}

@Composable
private fun MockCategories() {
    Column(modifier = Modifier.fillMaxSize()) {
        MockHeader(R.drawable.back_arrow, "Categories")
        Divider(color = MockDivider)
        Spacer(modifier = Modifier.height(8.dp))
        MockRow(IcBg1, Ic1, R.drawable.category_label, "Work", "12 notes", "")
        Divider(color = MockDivider)
        MockRow(IcBg2, Ic2, R.drawable.category_label, "Personal", "8 notes", "")
        Divider(color = MockDivider)
        MockRow(IcBg3, Ic3, R.drawable.category_label, "Ideas", "5 notes", "")
        Divider(color = MockDivider)
        MockRow(IcBg4, Ic4, R.drawable.category_label, "Travel", "3 notes", "")
    }
}

@Composable
private fun MockSync() {
    Column(modifier = Modifier.fillMaxSize()) {
        MockHeader(R.drawable.back_arrow, "Cloud Sync")
        Divider(color = MockDivider)
        Spacer(modifier = Modifier.height(8.dp))
        MockRow(IcBg1, Ic1, R.drawable.cloud, "Synced!", "All notes backed up successfully", "Now")
        Divider(color = MockDivider)
        MockRow(IcBg3, Ic3, R.drawable.cloud, "Auto Backup", "Syncing every 15 minutes", "Active")
        Divider(color = MockDivider)
        MockRow(IcBg2, Ic2, R.drawable.sync, "Unsynced Notes", "2 notes pending upload", "Soon")
        Divider(color = MockDivider)
        MockRow(IcBg4, Ic4, R.drawable.cloud, "Last Sync", "Everything is up to date", "5 min")
    }
}

@Composable
private fun MockLock() {
    Column(modifier = Modifier.fillMaxSize()) {
        MockHeader(R.drawable.back_arrow, "Secure Notes")
        Divider(color = MockDivider)
        Spacer(modifier = Modifier.height(8.dp))
        MockRow(IcBg2, Ic2, R.drawable.lock, "Private Diary", "Protected with PIN lock", "Locked")
        Divider(color = MockDivider)
        MockRow(IcBg4, Ic4, R.drawable.fingerprint, "Bank Details", "Protected with biometric", "Locked")
        Divider(color = MockDivider)
        MockRow(IcBg1, Ic1, R.drawable.lock_open, "Shopping List", "No lock applied", "Open")
        Divider(color = MockDivider)
        MockRow(IcBg3, Ic3, R.drawable.lock, "Work Secrets", "Protected with PIN lock", "Locked")
    }
}

@Composable
private fun MockHeader(iconRes: Int, label: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(painter = painterResource(iconRes), contentDescription = null,
            tint = Color(0xFF374151), modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(label, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF111827))
    }
}

@Composable
private fun MockRow(
    iconBg: Color, iconTint: Color, iconRes: Int,
    title: String, subtitle: String, time: String
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(painter = painterResource(iconRes), contentDescription = null,
                tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF111827))
            if (subtitle.isNotEmpty()) {
                Text(subtitle, fontSize = 11.sp, color = Color(0xFF9CA3AF), maxLines = 1)
            }
        }
        if (time.isNotEmpty()) {
            Text(time, fontSize = 10.sp, color = Color(0xFF9CA3AF))
        }
    }
}
