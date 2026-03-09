package com.white.notepilot.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.white.notepilot.R
import com.white.notepilot.data.model.NoteImage
import com.white.notepilot.ui.components.CustomTopBar
import com.white.notepilot.ui.theme.Dimens
import com.white.notepilot.viewmodel.NotesViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ImageSyncTestScreen(
    navController: NavHostController,
    viewModel: NotesViewModel = hiltViewModel()
) {
    val imageRepository = viewModel.imageRepository
    
    var unsyncedImages by remember { mutableStateOf<List<NoteImage>>(emptyList()) }
    var isSyncing by remember { mutableStateOf(false) }
    var syncResult by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        scope.launch {
            unsyncedImages = withContext(Dispatchers.IO) {
                imageRepository.getUnsyncedImages()
            }
            isLoading = false
        }
    }
    
    Scaffold(
        topBar = {
            CustomTopBar(
                leftIconRes = R.drawable.back_arrow,
                onLeftIconClick = { navController.popBackStack() },
                title = "Image Sync Test"
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(Dimens.PaddingLarge),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Cloudinary Sync Status",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (isLoading) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                            Text("Loading...")
                        }
                    } else {
                        Text(
                            text = "Unsynced Images: ${unsyncedImages.size}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    
                    syncResult?.let { result ->
                        Text(
                            text = result,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (result.contains("Success")) Color(0xFF4CAF50) else Color(0xFFF44336)
                        )
                    }
                }
            }
            
            Button(
                onClick = {
                    isSyncing = true
                    syncResult = null
                    scope.launch {
                        try {
                            val result = withContext(Dispatchers.IO) {
                                imageRepository.syncAllUnsyncedImages()
                            }
                            
                            if (result.isSuccess) {
                                val count = result.getOrNull() ?: 0
                                syncResult = "Successfully synced $count image(s)"
                            } else {
                                syncResult = "Sync failed: ${result.exceptionOrNull()?.message}"
                            }
                            
                            unsyncedImages = withContext(Dispatchers.IO) {
                                imageRepository.getUnsyncedImages()
                            }
                        } catch (e: Exception) {
                            syncResult = "Exception: ${e.message}"
                        } finally {
                            isSyncing = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSyncing && unsyncedImages.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White
                    )
                } else {
                    Text(
                        text = "Sync All Images to Cloudinary",
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
            
            Button(
                onClick = {
                    isLoading = true
                    scope.launch {
                        unsyncedImages = withContext(Dispatchers.IO) {
                            imageRepository.getUnsyncedImages()
                        }
                        isLoading = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSyncing && !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Refresh List",
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            if (unsyncedImages.isNotEmpty()) {
                Text(
                    text = "Unsynced Images:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(unsyncedImages) { image ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = image.fileName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Size: ${image.fileSize / 1024} KB",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Note ID: ${image.noteId}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Not synced",
                                    tint = Color(0xFFF44336),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            } else if (!isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "All synced",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "All images are synced!",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
            }
        }
    }
}
