package com.white.notepilot.ui.screens

import android.content.Context
import android.content.Intent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.white.notepilot.R
import com.white.notepilot.ui.theme.Dimens
import com.white.notepilot.ui.theme.NotesTheme

@Composable
fun OpenSourceLicensesScreen(
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
                            .size(16.dp)
                            .clickable { navController.popBackStack() },
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                    
                    Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                    
                    Text(
                        text = "Open Source Licenses",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = Dimens.PaddingLarge)
                ) {
                    OpenSourceLicensesContent()
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun OpenSourceLicensesContent() {
    val context = LocalContext.current
    
    Column {
        Text(
            text = "This app uses the following open source libraries and frameworks:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            lineHeight = 22.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // Android & Kotlin Libraries
        LicenseCategory(
            title = "Android & Kotlin",
            libraries = listOf(
                LibraryInfo(
                    name = "Android Jetpack Compose",
                    description = "Modern toolkit for building native Android UI",
                    license = "Apache License 2.0",
                    url = "https://developer.android.com/jetpack/compose"
                ),
                LibraryInfo(
                    name = "Kotlin",
                    description = "Programming language for Android development",
                    license = "Apache License 2.0",
                    url = "https://kotlinlang.org/"
                ),
                LibraryInfo(
                    name = "Android Architecture Components",
                    description = "ViewModel, LiveData, Room, Navigation",
                    license = "Apache License 2.0",
                    url = "https://developer.android.com/topic/libraries/architecture"
                ),
                LibraryInfo(
                    name = "Material Design 3",
                    description = "Material Design components for Android",
                    license = "Apache License 2.0",
                    url = "https://m3.material.io/"
                )
            ),
            context = context
        )
        
        // Firebase Libraries
        LicenseCategory(
            title = "Firebase & Google Services",
            libraries = listOf(
                LibraryInfo(
                    name = "Firebase Authentication",
                    description = "User authentication and management",
                    license = "Apache License 2.0",
                    url = "https://firebase.google.com/products/auth"
                ),
                LibraryInfo(
                    name = "Firebase Firestore",
                    description = "NoSQL cloud database",
                    license = "Apache License 2.0",
                    url = "https://firebase.google.com/products/firestore"
                ),
                LibraryInfo(
                    name = "Firebase Cloud Messaging",
                    description = "Push notification service",
                    license = "Apache License 2.0",
                    url = "https://firebase.google.com/products/cloud-messaging"
                ),
                LibraryInfo(
                    name = "Google AdMob",
                    description = "Mobile advertising platform",
                    license = "Google APIs Terms of Service",
                    url = "https://developers.google.com/admob"
                )
            ),
            context = context
        )
        
        // Dependency Injection & Networking
        LicenseCategory(
            title = "Dependency Injection & Networking",
            libraries = listOf(
                LibraryInfo(
                    name = "Dagger Hilt",
                    description = "Dependency injection framework",
                    license = "Apache License 2.0",
                    url = "https://dagger.dev/hilt/"
                ),
                LibraryInfo(
                    name = "Retrofit",
                    description = "HTTP client for Android",
                    license = "Apache License 2.0",
                    url = "https://square.github.io/retrofit/"
                ),
                LibraryInfo(
                    name = "OkHttp",
                    description = "HTTP & HTTP/2 client",
                    license = "Apache License 2.0",
                    url = "https://square.github.io/okhttp/"
                )
            ),
            context = context
        )
        
        // Database & Storage
        LicenseCategory(
            title = "Database & Storage",
            libraries = listOf(
                LibraryInfo(
                    name = "Room Database",
                    description = "SQLite object mapping library",
                    license = "Apache License 2.0",
                    url = "https://developer.android.com/training/data-storage/room"
                ),
                LibraryInfo(
                    name = "DataStore",
                    description = "Data storage solution for preferences",
                    license = "Apache License 2.0",
                    url = "https://developer.android.com/topic/libraries/architecture/datastore"
                )
            ),
            context = context
        )
        
        // Image Processing & UI
        LicenseCategory(
            title = "Image Processing & UI Libraries",
            libraries = listOf(
                LibraryInfo(
                    name = "Coil",
                    description = "Image loading library for Android",
                    license = "Apache License 2.0",
                    url = "https://coil-kt.github.io/coil/"
                ),
                LibraryInfo(
                    name = "Cloudinary Android SDK",
                    description = "Image and video management service",
                    license = "MIT License",
                    url = "https://cloudinary.com/documentation/android_integration"
                ),
                LibraryInfo(
                    name = "Accompanist",
                    description = "Compose utilities and extensions",
                    license = "Apache License 2.0",
                    url = "https://google.github.io/accompanist/"
                ),
                LibraryInfo(
                    name = "Lottie",
                    description = "Animation library for Android",
                    license = "Apache License 2.0",
                    url = "https://airbnb.io/lottie/"
                )
            ),
            context = context
        )
        
        // License Information
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "License Information",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        LicenseDetails(
            title = "Apache License 2.0",
            description = "A permissive license that allows commercial use, modification, distribution, and private use. It provides an express grant of patent rights from contributors."
        )
        
        LicenseDetails(
            title = "MIT License",
            description = "A simple permissive license that allows commercial use, modification, distribution, and private use with minimal restrictions."
        )
        
        LicenseDetails(
            title = "Google APIs Terms of Service",
            description = "Terms governing the use of Google's APIs and services, including AdMob and Firebase services."
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "For complete license texts and more information, please visit the respective project websites.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun LicenseCategory(
    title: String,
    libraries: List<LibraryInfo>,
    context: Context
) {
    Column(
        modifier = Modifier.padding(bottom = 32.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        libraries.forEach { library ->
            LibraryItem(
                library = library,
                context = context
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun LibraryItem(
    library: LibraryInfo,
    context: Context
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { 
                openUrl(context, library.url)
            }
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = library.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = library.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            lineHeight = 20.sp
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = "License: ${library.license}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(2.dp))
        
        Text(
            text = "Tap to visit website",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            fontSize = 11.sp
        )
    }
}

@Composable
private fun LicenseDetails(
    title: String,
    description: String
) {
    Column(
        modifier = Modifier.padding(bottom = 20.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            lineHeight = 22.sp
        )
    }
}

private data class LibraryInfo(
    val name: String,
    val description: String,
    val license: String,
    val url: String
)

private fun openUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        context.startActivity(intent)
    } catch (_: Exception) {
        // Handle error silently
    }
}

@Preview(showBackground = true)
@Composable
fun OpenSourceLicensesScreenPreview() {
    NotesTheme {
        OpenSourceLicensesScreen(navController = rememberNavController())
    }
}