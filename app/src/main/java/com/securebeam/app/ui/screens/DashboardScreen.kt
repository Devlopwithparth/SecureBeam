package com.securebeam.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.securebeam.app.data.db.TransferRecord
import com.securebeam.app.ui.components.MetricCard
import com.securebeam.app.ui.components.SecureBeamTopBar
import com.securebeam.app.ui.theme.*
import com.securebeam.app.ui.viewmodel.SecureBeamViewModel

@Composable
fun DashboardScreen(
    viewModel: SecureBeamViewModel,
    onNavigateToSend: () -> Unit,
    onNavigateToReceive: () -> Unit,
    onNavigateToSecurity: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val stats by viewModel.dashboardStats.collectAsState()
    val recentList by viewModel.recentTransfers.collectAsState()

    Scaffold(
        topBar = { SecureBeamTopBar(title = "SecureBeam Dashboard") },
        containerColor = ObsidianDark
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Metrics Overview Grid
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MetricCard(
                        title = "Transfers",
                        value = "${stats.totalTransfers}",
                        subtitle = "Total Operations",
                        icon = Icons.Default.SwapHoriz,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Success Rate",
                        value = "${stats.successRatePercent}%",
                        subtitle = "Integrity Passed",
                        icon = Icons.Default.CheckCircle,
                        iconTint = StatusSuccess,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MetricCard(
                        title = "Storage Used",
                        value = "%.1f MB".format(stats.storageUsedMb),
                        subtitle = "Secure Saved Files",
                        icon = Icons.Default.FolderZip,
                        iconTint = CyberPurple,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Avg Speed",
                        value = "%.0f Kbps".format(stats.averageSpeedKbps),
                        subtitle = "Optical Transfer Rate",
                        icon = Icons.Default.Speed,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Quick Actions Section
            item {
                Text(
                    text = "QUICK ACTIONS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = onNavigateToSend,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = ObsidianDark
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SEND FILE",
                            fontWeight = FontWeight.Bold,
                            color = ObsidianDark
                        )
                    }

                    Button(
                        onClick = onNavigateToReceive,
                        colors = ButtonDefaults.buttonColors(containerColor = CyberPurple),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "RECEIVE",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onNavigateToSecurity,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardSurfaceBorder),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = null, tint = StatusSuccess)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Security & Shred")
                    }

                    OutlinedButton(
                        onClick = onNavigateToSettings,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardSurfaceBorder),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = TextSecondary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Settings")
                    }
                }
            }

            // Recent Transfer Activity
            item {
                Text(
                    text = "RECENT TRANSFER HISTORY",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            if (recentList.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardSurfaceBorder),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier.padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No transfer history recorded yet.\nSelect 'Send File' or 'Receive' to start an optical transfer.",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            } else {
                items(recentList) { record ->
                    TransferRecordItem(record = record)
                }
            }

            // Developer Footer Credit
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "© 2026 SecureBeam. All Rights Reserved",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "Designed & Developed by @Devlopwithparth",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TransferRecordItem(record: TransferRecord) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardSurfaceBorder),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = if (record.direction == "RECEIVER") CyberPurple.copy(alpha = 0.2f) else NeonCyan.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (record.direction == "RECEIVER") Icons.Default.Download else Icons.Default.Upload,
                            contentDescription = null,
                            tint = if (record.direction == "RECEIVER") CyberPurple else NeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = record.fileName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                    Text(
                        text = "${record.direction} | Hash: ${record.fileHash.take(8)}... | ${record.fileSize / 1024} KB",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }
            }

            Surface(
                color = StatusSuccess.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = record.status,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = StatusSuccess,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
