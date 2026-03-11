package com.white.notepilot.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.white.notepilot.R
import com.white.notepilot.ui.theme.Dimens
import com.white.notepilot.ui.theme.NotesTheme

@Composable
fun TermsOfUseScreen(
    navController: NavHostController
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Sticky Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = Dimens.PaddingLarge)
                        .padding(
                            top = innerPadding.calculateTopPadding() + Dimens.PaddingMedium,
                            bottom = Dimens.PaddingMedium
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.back_arrow),
                        contentDescription = "Back",
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { navController.popBackStack() },
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                    
                    Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                    
                    Text(
                        text = "Terms of Use",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                
                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = Dimens.PaddingLarge)
                ) {
                    TermsOfUseContent()
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun TermsOfUseContent() {
    Column {
        Text(
            text = "Last updated: ${getCurrentDate()}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        Text(
            text = "Welcome to NotePilot. These Terms of Use (\"Terms\") govern your use of the NotePilot mobile application (\"App\") developed by Hemal Katariya (\"we,\" \"us,\" or \"our\").",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            lineHeight = 22.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        TermsSection(
            title = "1. Acceptance of Terms",
            content = """
                By downloading, installing, or using the NotePilot app, you agree to be bound by these Terms. If you do not agree to these Terms, please do not use the App.
                
                These Terms constitute a legally binding agreement between you and Hemal Katariya regarding your use of the App.
            """.trimIndent()
        )
        
        TermsSection(
            title = "2. Description of Service",
            content = """
                NotePilot is a note-taking application that allows you to:
                
                • Create, edit, and organize notes
                • Add categories and images to your notes
                • Sync your data across devices using Firebase
                • Store images using Cloudinary
                • Access your notes offline
                • Receive notifications about app features
                
                The App is provided free of charge with optional advertisements.
            """.trimIndent()
        )
        
        TermsSection(
            title = "3. User Accounts",
            content = """
                • You must create an account using Google Sign-In to use certain features
                • You are responsible for maintaining the confidentiality of your account
                • You agree to provide accurate and complete information
                • You are responsible for all activities that occur under your account
                • You must notify us immediately of any unauthorized use of your account
            """.trimIndent()
        )
        
        TermsSection(
            title = "4. Acceptable Use",
            content = """
                You agree to use the App only for lawful purposes and in accordance with these Terms. You agree NOT to:
                
                • Use the App for any illegal or unauthorized purpose
                • Store or share content that is harmful, offensive, or violates others' rights
                • Attempt to gain unauthorized access to the App or its systems
                • Interfere with or disrupt the App's functionality
                • Reverse engineer, decompile, or disassemble the App
                • Use the App to spam or send unsolicited communications
            """.trimIndent()
        )
        
        TermsSection(
            title = "5. Content and Data",
            content = """
                • You retain ownership of the content you create and store in the App
                • You grant us a license to store, process, and sync your content to provide the service
                • You are responsible for backing up your important data
                • We may remove content that violates these Terms or applicable laws
                • You agree not to store sensitive personal information like passwords or financial data
            """.trimIndent()
        )
        
        TermsSection(
            title = "6. Privacy",
            content = """
                Your privacy is important to us. Our Privacy Policy explains how we collect, use, and protect your information. By using the App, you agree to our Privacy Policy.
                
                Key privacy points:
                • We use Firebase for data storage and authentication
                • Images are stored securely using Cloudinary
                • We do not sell your personal information
                • You can delete your account and data at any time
            """.trimIndent()
        )
        
        TermsSection(
            title = "7. Third-Party Services",
            content = """
                The App integrates with third-party services including:
                
                • Google Firebase (authentication and data storage)
                • Cloudinary (image storage)
                • Google AdMob (advertisements)
                
                Your use of these services is subject to their respective terms and privacy policies.
            """.trimIndent()
        )
        
        TermsSection(
            title = "8. Advertisements",
            content = """
                The App may display advertisements provided by Google AdMob. By using the App, you agree to the display of such advertisements.
                
                • Ads help us provide the App free of charge
                • We do not control the content of third-party advertisements
                • Clicking on ads may redirect you to external websites
            """.trimIndent()
        )
        
        TermsSection(
            title = "9. Disclaimers",
            content = """
                THE APP IS PROVIDED "AS IS" WITHOUT WARRANTIES OF ANY KIND. WE DISCLAIM ALL WARRANTIES, EXPRESS OR IMPLIED, INCLUDING:
                
                • Warranties of merchantability and fitness for a particular purpose
                • Warranties that the App will be error-free or uninterrupted
                • Warranties regarding the accuracy or reliability of content
                
                Your use of the App is at your own risk.
            """.trimIndent()
        )
        
        TermsSection(
            title = "10. Limitation of Liability",
            content = """
                TO THE MAXIMUM EXTENT PERMITTED BY LAW, WE SHALL NOT BE LIABLE FOR:
                
                • Any indirect, incidental, or consequential damages
                • Loss of data, profits, or business opportunities
                • Damages resulting from your use or inability to use the App
                • Damages exceeding the amount you paid for the App (which is zero)
            """.trimIndent()
        )
        
        TermsSection(
            title = "11. Termination",
            content = """
                • You may stop using the App at any time
                • We may terminate your access if you violate these Terms
                • Upon termination, your right to use the App ceases immediately
                • You may delete your account and data through the App settings
                • Provisions regarding liability and disputes survive termination
            """.trimIndent()
        )
        
        TermsSection(
            title = "12. Changes to Terms",
            content = """
                We may update these Terms from time to time. We will notify you of material changes by:
                
                • Posting the updated Terms in the App
                • Updating the "Last updated" date
                • Sending a notification through the App (if applicable)
                
                Your continued use of the App after changes constitutes acceptance of the new Terms.
            """.trimIndent()
        )
        
        TermsSection(
            title = "13. Contact Information",
            content = """
                If you have questions about these Terms, please contact us:
                
                • Email: hemalkatariya4488@gmail.com
                • Developer: Hemal Katariya
                • App: NotePilot
                
                We will respond to your inquiries within 30 days.
            """.trimIndent()
        )
        
        TermsSection(
            title = "14. Governing Law",
            content = """
                These Terms are governed by and construed in accordance with applicable laws. Any disputes arising from these Terms or your use of the App will be resolved through appropriate legal channels.
            """.trimIndent()
        )
    }
}

@Composable
private fun TermsSection(
    title: String,
    content: String
) {
    Column(
        modifier = Modifier.padding(bottom = 24.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            lineHeight = 22.sp
        )
    }
}

private fun getCurrentDate(): String {
    return "March 10, 2026"
}

@Preview(showBackground = true)
@Composable
fun TermsOfUseScreenPreview() {
    NotesTheme {
        TermsOfUseScreen(navController = rememberNavController())
    }
}