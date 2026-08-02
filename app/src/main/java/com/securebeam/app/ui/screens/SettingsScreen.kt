package com.securebeam.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.securebeam.app.ui.components.SecureBeamTopBar
import com.securebeam.app.ui.theme.*
import com.securebeam.app.ui.viewmodel.SecureBeamViewModel

@Composable
fun SettingsScreen(
    viewModel: SecureBeamViewModel,
    onBack: () -> Unit
) {
    val targetFps by viewModel.targetFps.collectAsState()
    val qrDensity by viewModel.qrDensity.collectAsState()
    val encryptionMode by viewModel.encryptionMode.collectAsState()

    Scaffold(
        topBar = { SecureBeamTopBar(title = "Engine & Protocol Settings", onBackClick = onBack) },
        containerColor = ObsidianDark
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Optical Stream Settings
            item {
                Text(text = "OPTICAL STREAM TUNING", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardSurfaceBorder),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = NeonCyan)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Animation Speed: $targetFps FPS", fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Slider(
                            value = targetFps.toFloat(),
                            onValueChange = { viewModel.targetFps.value = it.toInt() },
                            valueRange = 5f..30f,
                            steps = 5,
                            colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan)
                        )
                    }
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardSurfaceBorder),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.QrCode, contentDescription = null, tint = CyberPurple)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "QR Frame Chunk Density", fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("LOW", "MEDIUM", "HIGH").forEach { density ->
                                FilterChip(
                                    selected = qrDensity == density,
                                    onClick = { viewModel.qrDensity.value = density },
                                    label = { Text(density) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CyberPurple,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Security Encryption Mode
            item {
                Text(text = "CRYPTOGRAPHY & SECURITY", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardSurfaceBorder),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = StatusSuccess)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Encryption Algorithm Mode", fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("AES-256-GCM", "AES-256-CBC").forEach { mode ->
                                FilterChip(
                                    selected = encryptionMode == mode,
                                    onClick = { viewModel.encryptionMode.value = mode },
                                    label = { Text(mode) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = StatusSuccess,
                                        selectedLabelColor = ObsidianDark
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
