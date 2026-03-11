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
fun PrivacyPolicyScreen(
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
                        text = "Privacy Policy",
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
                    PrivacyPolicyContent()
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun PrivacyPolicyContent() {
    Column {
        Text(
            text = "Last updated: ${getCurrentDate()}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        PolicySection(
            title = "1. Information We Collect",
            content = """
                We collect information you provide directly to us, such as:
                
                • Account Information: When you create an account, we collect your email address and profile information from Google Sign-In.
                
                • Notes and Content: We store the notes, categories, and images you create within the app.
                
                • Feedback: When you submit feedback through our app, we collect the information you provide including your email, feedback type, and description.
                
                • Device Information: We automatically collect certain device information including device model, operating system version, and app version for debugging and improvement purposes.
            """.trimIndent()
        )
        
        PolicySection(
            title = "2. How We Use Your Information",
            content = """
                We use the information we collect to:
                
                • Provide, maintain, and improve our services
                • Sync your notes across devices using Firebase Firestore
                • Store your images securely using Cloudinary
                • Respond to your feedback and support requests
                • Send you notifications about app updates and features
                • Analyze app usage to improve user experience
                • Ensure the security and integrity of our services
            """.trimIndent()
        )
        
        PolicySection(
            title = "3. Information Sharing and Disclosure",
            content = """
                We do not sell, trade, or otherwise transfer your personal information to third parties except:
                
                • Service Providers: We use Firebase (Google) for authentication and data storage, and Cloudinary for image storage. These services have their own privacy policies.
                
                • Legal Requirements: We may disclose your information if required by law or to protect our rights and safety.
                
                • Business Transfers: In the event of a merger or acquisition, your information may be transferred as part of the business assets.
            """.trimIndent()
        )
        
        PolicySection(
            title = "4. Data Storage and Security",
            content = """
                • Your data is stored securely using industry-standard encryption
                • Notes are stored in Firebase Firestore with secure access controls
                • Images are stored in Cloudinary with secure URLs
                • Local data on your device is protected by Android's security features
                • We implement appropriate technical and organizational measures to protect your data
            """.trimIndent()
        )
        
        PolicySection(
            title = "5. Your Rights and Choices",
            content = """
                You have the right to:
                
                • Access your personal information
                • Update or correct your information
                • Delete your account and associated data
                • Export your data
                • Opt-out of notifications
                • Request information about how we use your data
                
                To exercise these rights, please contact us at hemalkatariya4488@gmail.com
            """.trimIndent()
        )
        
        PolicySection(
            title = "6. Third-Party Services",
            content = """
                Our app integrates with the following third-party services:
                
                • Google Firebase: For authentication and data storage
                • Cloudinary: For image storage and management
                • Google AdMob: For displaying advertisements
                
                These services have their own privacy policies and terms of service.
            """.trimIndent()
        )
        
        PolicySection(
            title = "7. Children's Privacy",
            content = """
                Our app is not intended for children under 13 years of age. We do not knowingly collect personal information from children under 13. If you are a parent or guardian and believe your child has provided us with personal information, please contact us.
            """.trimIndent()
        )
        
        PolicySection(
            title = "8. Changes to This Privacy Policy",
            content = """
                We may update this Privacy Policy from time to time. We will notify you of any changes by posting the new Privacy Policy on this page and updating the "Last updated" date.
            """.trimIndent()
        )
        
        PolicySection(
            title = "9. Contact Us",
            content = """
                If you have any questions about this Privacy Policy, please contact us:
                
                • Email: hemalkatariya4488@gmail.com
                • Developer: Hemal Katariya
                • App: NotePilot
                
                We will respond to your inquiries within 30 days.
            """.trimIndent()
        )
    }
}

@Composable
private fun PolicySection(
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
fun PrivacyPolicyScreenPreview() {
    NotesTheme {
        PrivacyPolicyScreen(navController = rememberNavController())
    }
}