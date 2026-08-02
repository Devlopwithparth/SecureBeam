package com.securebeam.app.ui.screens

import android.Manifest
import android.content.contentValuesOf
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.securebeam.app.protocol.FilePackager
import com.securebeam.app.protocol.FramePacket
import com.securebeam.app.protocol.ReassemblyState
import com.securebeam.app.qr.CameraFrameAnalyzer
import com.securebeam.app.ui.components.SecureBeamTopBar
import com.securebeam.app.ui.theme.*
import com.securebeam.app.ui.viewmodel.SecureBeamViewModel
import java.util.concurrent.Executors

@Composable
fun ReceiverScreen(
    viewModel: SecureBeamViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val state by viewModel.receiverState.collectAsState()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = { SecureBeamTopBar(title = "Receiver - Optical Frame Scanner", onBackClick = onBack) },
        containerColor = ObsidianDark
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Camera Viewport / Simulation Box
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Black),
                border = androidx.compose.foundation.BorderStroke(2.dp, CyberPurple),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (hasCameraPermission) {
                        AndroidView(
                            factory = { ctx ->
                                val previewView = PreviewView(ctx)
                                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                                cameraProviderFuture.addListener({
                                    val cameraProvider = cameraProviderFuture.get()
                                    val preview = Preview.Builder().build().also {
                                        it.setSurfaceProvider(previewView.surfaceProvider)
                                    }

                                    val analyzer = ImageAnalysis.Builder()
                                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                        .build().also {
                                            it.setAnalyzer(
                                                Executors.newSingleThreadExecutor(),
                                                CameraFrameAnalyzer { framePacket ->
                                                    viewModel.onFrameScanned(framePacket, ctx)
                                                }
                                            )
                                        }

                                    try {
                                        cameraProvider.unbindAll()
                                        cameraProvider.bindToLifecycle(
                                            lifecycleOwner,
                                            CameraSelector.DEFAULT_BACK_CAMERA,
                                            preview,
                                            analyzer
                                        )
                                    } catch (e: Exception) {
                                        Log.e("ReceiverScreen", "Camera binding failed", e)
                                    }
                                }, ContextCompat.getMainExecutor(ctx))
                                previewView
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            text = "Camera Permission Required for Live Scan",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    // Scan Reticle Overlay
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .border(2.dp, NeonCyan, RoundedCornerShape(12.dp))
                    )
                }
            }

            // Quick Scan Simulation Button for Easy Testing
            OutlinedButton(
                onClick = {
                    val packets = FilePackager.packageFile("received_secure_doc.pdf", "SECURE_OFFLINE_TRANSFER_SIMULATED_PAYLOAD_12345".toByteArray())
                    packets.forEach { frame ->
                        viewModel.onFrameScanned(frame, context)
                    }
                },
                border = androidx.compose.foundation.BorderStroke(1.dp, StatusSuccess),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.FlashOn, contentDescription = null, tint = StatusSuccess)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Simulate Optical Frame Capture", color = StatusSuccess, fontWeight = FontWeight.Bold)
            }

            // Real-time Packet Matrix Status
            when (val currentState = state) {
                is ReassemblyState.InProgress -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardSurfaceBorder),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "PACKET REASSEMBLY PROGRESS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Frames Received: ${currentState.receivedCount} / ${currentState.totalCount}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "${currentState.progressPercent}%",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberPurple
                                )
                            }
                            LinearProgressIndicator(
                                progress = currentState.progressPercent / 100f,
                                color = CyberPurple,
                                trackColor = CardSurfaceBorder,
                                modifier = Modifier.fillMaxWidth().height(8.dp)
                            )
                        }
                    }
                }
                is ReassemblyState.Completed -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = StatusSuccess.copy(alpha = 0.1f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StatusSuccess),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = StatusSuccess)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "TRANSFER COMPLETED & VERIFIED",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusSuccess
                                )
                            }
                            Text(text = "File Name: ${currentState.fileName}", fontSize = 12.sp, color = Color.White)
                            Text(text = "SHA-256 Hash: ${currentState.fileHash.take(16)}...", fontSize = 10.sp, color = TextSecondary)

                            Button(
                                onClick = { viewModel.resetReceiver() },
                                colors = ButtonDefaults.buttonColors(containerColor = StatusSuccess),
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = ObsidianDark)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Scan Next File", color = ObsidianDark, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                is ReassemblyState.CorruptedData -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = StatusError.copy(alpha = 0.1f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StatusError),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = StatusError)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Data Corrupted: ${currentState.reason}", color = StatusError, fontSize = 12.sp)
                        }
                    }
                }
                else -> {}
            }
        }
    }
}
