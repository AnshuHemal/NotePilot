package com.white.notepilot.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.white.notepilot.R
import com.white.notepilot.data.repository.ImageRepository
import com.white.notepilot.ui.components.CustomTopBar
import com.white.notepilot.ui.theme.Blue
import kotlinx.coroutines.launch

@Composable
fun ImageSyncScreen(
    navController: NavHostController,
    imageRepository: ImageRepository = hiltViewModel()
) {
    var isSyncing by remember { mutableStateOf(false) }
    var syncMessage by remember { mutableStateOf("") }
    var unsyncedCount by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            CustomTopBar(
                title = "Sync Images",
                leftIconRes = R.drawable.back_arrow,
                onLeftIconClick = { navController.popBackStack() }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Cloudinary Image Sync",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Sync your local images to Cloudinary cloud storage",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                if (isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = Blue
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Syncing images...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                } else {
                    Button(
                        onClick = {
                            isSyncing = true
                            syncMessage = ""
                            scope.launch {
                                try {
                                    val images = imageRepository.getUnsyncedImages()
                                    unsyncedCount = images.size

                                    if (unsyncedCount == 0) {
                                        syncMessage = "All images are already synced!"
                                        isSyncing = false
                                        return@launch
                                    }

                                    val result = imageRepository.syncExistingImages()
                                    if (result.isSuccess) {
                                        syncMessage = result.getOrNull() ?: "Sync completed"
                                    } else {
                                        syncMessage = "Sync failed: ${result.exceptionOrNull()?.message}"
                                    }
                                } catch (e: Exception) {
                                    syncMessage = "Error: ${e.message}"
                                } finally {
                                    isSyncing = false
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Blue
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Sync Now",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (syncMessage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = syncMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (syncMessage.contains("failed") || syncMessage.contains("Error"))
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        scope.launch {
                            val images = imageRepository.getUnsyncedImages()
                            unsyncedCount = images.size
                            syncMessage = "Found $unsyncedCount unsynced image(s)"
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Check Unsynced Images",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
