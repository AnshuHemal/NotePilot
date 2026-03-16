package com.white.notepilot.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.white.notepilot.ui.components.qr.ScannedNotePreviewDialog
import com.white.notepilot.ui.components.qr.ScannedNoteImageOnlyDialog
import com.white.notepilot.ui.navigation.Routes
import com.white.notepilot.ui.theme.Blue
import com.white.notepilot.utils.QRCodeHelper
import com.white.notepilot.utils.QRCodeImageGenerator
import com.white.notepilot.viewmodel.NotesViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRScannerScreen(
    navController: NavController,
    viewModel: NotesViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    var flashEnabled by remember { mutableStateOf(false) }
    var scannedData by remember { mutableStateOf<QRCodeHelper.QRNoteData?>(null) }
    var showPreviewDialog by remember { mutableStateOf(false) }
    var showImageOnlyDialog by remember { mutableStateOf(false) }
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }
    
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Scan QR Code",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (hasCameraPermission) {
                CameraPreview(
                    flashEnabled = flashEnabled,
                    onQRCodeScanned = { qrContent ->
                        if (!isProcessing) {
                            isProcessing = true
                            scope.launch {
                                val noteData = QRCodeHelper.decodeNoteFromQR(qrContent)
                                if (noteData != null) {
                                    scannedData = noteData
                                    
                                    if (qrContent.startsWith("notepilot://")) {
                                        showPreviewDialog = true
                                    } else {
                                        previewBitmap = withContext(Dispatchers.Default) {
                                            QRCodeImageGenerator.generateNotePreviewImage(
                                                context = context,
                                                title = noteData.title,
                                                htmlContent = noteData.htmlContent,
                                                categories = noteData.categories,
                                                width = 1080,
                                                isDarkMode = false
                                            )
                                        }
                                        showImageOnlyDialog = true
                                    }
                                }
                                isProcessing = false
                            }
                        }
                    }
                )
                
                // Scanning frame overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .size(280.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Transparent
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            4.dp,
                            Blue
                        )
                    ) {}
                }
                
                // Instructions
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 28.dp)
                        .background(
                            Color.Black.copy(alpha = 0.7f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Position QR code within the frame",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "The note will be automatically detected",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Camera Permission Required",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Please grant camera permission to scan QR codes",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    ) {
                        Text("Grant Permission")
                    }
                }
            }
        }
    }
    
    if (showPreviewDialog && scannedData != null) {
        val currentScannedData = scannedData!!
        ScannedNotePreviewDialog(
            title = currentScannedData.title,
            htmlContent = currentScannedData.htmlContent,
            categories = currentScannedData.categories,
            isLocked = currentScannedData.isLocked,
            onDismiss = {
                showPreviewDialog = false
                scannedData = null
            },
            onSaveNote = {
                scope.launch {
                    try {
                        viewModel.addScannedNote(
                            title = currentScannedData.title,
                            htmlContent = currentScannedData.htmlContent,
                            categories = currentScannedData.categories,
                            passwordHash = currentScannedData.passwordHash,
                            isLocked = currentScannedData.isLocked
                        )

                        android.widget.Toast.makeText(
                            context,
                            "Note added to workspace successfully!",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                        
                        navController.navigate(Routes.Home.route) {
                            popUpTo(Routes.Home.route) { inclusive = true }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        android.widget.Toast.makeText(
                            context,
                            "Failed to add note: ${e.message}",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        )
    }
    
    if (showImageOnlyDialog && previewBitmap != null) {
        ScannedNoteImageOnlyDialog(
            previewBitmap = previewBitmap,
            onDismiss = {
                showImageOnlyDialog = false
                previewBitmap = null
                scannedData = null
            }
        )
    }
}

@Composable
fun CameraPreview(
    flashEnabled: Boolean,
    onQRCodeScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val executor = remember { Executors.newSingleThreadExecutor() }
    val barcodeScanner = remember { BarcodeScanning.getClient() }
    
    var lastScannedTime by remember { mutableLongStateOf(0L) }
    val scanCooldown = 2000L
    
    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProvider = cameraProviderFuture.get()
            
            val preview = Preview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
            }
            
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { analysis ->
                    analysis.setAnalyzer(executor) { imageProxy ->
                        processImageProxy(
                            imageProxy,
                            barcodeScanner,
                            onQRCodeScanned,
                            lastScannedTime,
                            scanCooldown
                        ) { newTime ->
                            lastScannedTime = newTime
                        }
                    }
                }
            
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            
            try {
                cameraProvider.unbindAll()
                val camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )
                
                camera.cameraControl.enableTorch(flashEnabled)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            previewView
        },
        modifier = Modifier.fillMaxSize(),
        update = { previewView ->
            val cameraProvider = cameraProviderFuture.get()
            try {
                val camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }
                )
                camera.cameraControl.enableTorch(flashEnabled)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    )
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
private fun processImageProxy(
    imageProxy: ImageProxy,
    barcodeScanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    onQRCodeScanned: (String) -> Unit,
    lastScannedTime: Long,
    scanCooldown: Long,
    updateLastScannedTime: (Long) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )
        
        barcodeScanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    if (barcode.format == Barcode.FORMAT_QR_CODE) {
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastScannedTime > scanCooldown) {
                            barcode.rawValue?.let { value ->
                                if (value.startsWith("notepilot://") || value.startsWith("NOTEPILOT:")) {
                                    updateLastScannedTime(currentTime)
                                    onQRCodeScanned(value)
                                }
                            }
                        }
                    }
                }
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    } else {
        imageProxy.close()
    }
}
