package com.securebeam.app.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.securebeam.app.ui.components.SecureBeamTopBar
import com.securebeam.app.ui.theme.*
import com.securebeam.app.ui.viewmodel.SecureBeamViewModel

@Composable
fun SenderScreen(
    viewModel: SecureBeamViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val isSending by viewModel.isSending.collectAsState()
    val packets by viewModel.senderPackets.collectAsState()
    val frameIndex by viewModel.currentFrameIndex.collectAsState()
    val qrBitmap by viewModel.senderQrBitmap.collectAsState()
    val targetFps by viewModel.targetFps.collectAsState()

    var selectedFileName by remember { mutableStateOf("sample_document.pdf") }

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(it)
            val bytes = inputStream?.readBytes()
            if (bytes != null) {
                val name = it.lastPathSegment?.substringAfterLast('/') ?: "selected_file.bin"
                selectedFileName = name
                viewModel.prepareFileForTransfer(name, bytes)
            }
        }
    }

    Scaffold(
        topBar = { SecureBeamTopBar(title = "Sender - Optical QR Streamer", onBackClick = onBack) },
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
            // File Selection Card
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardSurfaceBorder),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Selected File",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = selectedFileName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }

                    Button(
                        onClick = { filePickerLauncher.launch("*/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                    ) {
                        Icon(imageVector = Icons.Default.Folder, contentDescription = null, tint = ObsidianDark)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Browse", color = ObsidianDark, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Demo Sample Quick Action
            if (packets.isEmpty()) {
                OutlinedButton(
                    onClick = {
                        val sampleData = "SECUREBEAM_DEMO_PAYLOAD_AIR_GAPPED_TRANSMISSION_TEST_DATA_1234567890".toByteArray()
                        viewModel.prepareFileForTransfer("demo_payload.txt", sampleData)
                    },
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberPurple),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.FlashOn, contentDescription = null, tint = CyberPurple)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Load Sample Payload Test", color = CyberPurple, fontWeight = FontWeight.Bold)
                }
            }

            // Animated QR Stream Viewer
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Black),
                border = androidx.compose.foundation.BorderStroke(2.dp, if (isSending) NeonCyan else CardSurfaceBorder),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .size(280.dp)
                    .padding(8.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (qrBitmap != null) {
                        Image(
                            bitmap = qrBitmap!!.asImageBitmap(),
                            contentDescription = "Animated QR Frame",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            text = "Select a file to generate\nhigh-speed QR stream",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Frame Counter & Stream Progress
            if (packets.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "FRAME $frameIndex OF ${packets.size}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )

                    LinearProgressIndicator(
                        progress = if (packets.isNotEmpty()) frameIndex.toFloat() / packets.size else 0f,
                        color = NeonCyan,
                        trackColor = CardSurfaceBorder,
                        modifier = Modifier.fillMaxWidth().height(8.dp)
                    )

                    // FPS Control Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Speed: ${targetFps} FPS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Slider(
                            value = targetFps.toFloat(),
                            onValueChange = { viewModel.targetFps.value = it.toInt() },
                            valueRange = 5f..30f,
                            steps = 5,
                            colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan),
                            modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
                        )
                    }

                    // Controls: Play / Stop
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { if (isSending) viewModel.stopQrAnimation() else viewModel.prepareFileForTransfer(selectedFileName, "SAMPLE_DATA".toByteArray()) },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isSending) StatusError else StatusSuccess)
                        ) {
                            Icon(
                                imageVector = if (isSending) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isSending) "Pause Stream" else "Resume Stream")
                        }
                    }
                }
            }
        }
    }
}
