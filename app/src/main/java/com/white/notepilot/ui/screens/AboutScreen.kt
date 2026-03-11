package com.white.notepilot.ui.screens

import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.white.notepilot.R
import com.white.notepilot.ui.navigation.Routes
import com.white.notepilot.ui.theme.Dimens
import com.white.notepilot.ui.theme.NotesTheme
import androidx.core.net.toUri
import com.white.notepilot.ui.theme.LightGray

@Composable
fun AboutScreen(
    navController: NavHostController
) {
    val context = LocalContext.current
    
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Dimens.PaddingLarge)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Dimens.PaddingMedium),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.back_arrow),
                        contentDescription = "Back",
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { navController.popBackStack() },
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                    
                    Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                    
                    Text(
                        text = "About",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                AppInfoSection()
                
                Spacer(modifier = Modifier.height(24.dp))
                
                AboutItemsSection(
                    context = context,
                    navController = navController
                )
                
                Spacer(modifier = Modifier.height(100.dp))
            }
            
            DeveloperCredits(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
            )
        }
    }
}

@Composable
private fun AppInfoSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(R.drawable.logo),
            contentDescription = "App Icon",
            modifier = Modifier.size(64.dp),
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "NotePilot",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Version 1.0.0",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Your intelligent note-taking companion",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            fontSize = 14.sp
        )
    }
}

@Composable
private fun AboutItemsSection(
    context: Context,
    navController: NavHostController
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Legal & Support",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        AboutItem(
            title = "Privacy Policy",
            description = "How we handle your data",
            onClick = {
                navController.navigate(Routes.PrivacyPolicy.route)
            }
        )

        Spacer(modifier = Modifier.height(8.dp))
        
        AboutItem(
            title = "Terms of Use",
            description = "Terms and conditions",
            onClick = {
                navController.navigate(Routes.TermsOfUse.route)
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        AboutItem(
            title = "Open Source Licenses",
            description = "Third-party libraries",
            onClick = {
                navController.navigate(Routes.OpenSourceLicenses.route)
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        AboutItem(
            title = "Rate the App",
            description = "Share your feedback on Play Store",
            onClick = {
                openPlayStore(context)
            }
        )
    }
}

@Composable
private fun DeveloperCredits(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Developed by",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            fontSize = 12.sp
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = "Hemal Katariya",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 16.sp
        )
    }
}

@Composable
private fun AboutItem(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 13.sp,
                    color = LightGray,
                    lineHeight = 18.sp
                )
            }
            
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

private fun openPlayStore(context: Context) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, "market://details?id=${context.packageName}".toUri())
        context.startActivity(intent)
    } catch (_: Exception) {
        val intent = Intent(Intent.ACTION_VIEW, "https://play.google.com/store/apps/details?id=${context.packageName}".toUri())
        context.startActivity(intent)
    }
}

@Preview(showBackground = true)
@Composable
fun AboutScreenPreview() {
    NotesTheme {
        AboutScreen(navController = rememberNavController())
    }
}