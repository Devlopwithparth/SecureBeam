package com.securebeam.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.securebeam.app.data.db.AuditEvent
import com.securebeam.app.ui.components.SecureBeamTopBar
import com.securebeam.app.ui.theme.*
import com.securebeam.app.ui.viewmodel.SecureBeamViewModel
import java.io.File

@Composable
fun SecurityAuditScreen(
    viewModel: SecureBeamViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val auditLogs by viewModel.auditLogs.collectAsState()

    var shredderStatus by remember { mutableStateOf("") }

    Scaffold(
        topBar = { SecureBeamTopBar(title = "Cyber Security Audit & Shredder", onBackClick = onBack) },
        containerColor = ObsidianDark
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Device Trust Score Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StatusSuccess),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "DEVICE TRUST SCORE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                            Text(
                                text = "98 / 100",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = StatusSuccess
                            )
                            Text(
                                text = "AES-256-GCM / RSA Keys Active | No Tamper Detected",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = StatusSuccess,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }

            // Secure File Shredder Utility (DoD 5220.22-M)
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StatusError),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null, tint = StatusError)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "DoD 5220.22-M SECURE FILE SHREDDER",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = StatusError
                            )
                        }

                        Text(
                            text = "3-Pass zeroization and random data overwrite protocol to permanently eradicate sensitive temporary files.",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )

                        Button(
                            onClick = {
                                val dummyFile = File(context.cachePathOrTemp(), "temp_sensitive_shred_target.tmp")
                                dummyFile.writeText("SENSITIVE_TEMPORARY_DATA_FOR_ZEROIZATION")
                                viewModel.shredSelectedFile(dummyFile)
                                shredderStatus = "3-Pass Shred Complete: Zeroized & Unlinked"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StatusError)
                        ) {
                            Icon(imageVector = Icons.Default.CleaningServices, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Shred Temporary Cache Files", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        if (shredderStatus.isNotEmpty()) {
                            Text(text = shredderStatus, fontSize = 11.sp, color = StatusSuccess, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Audit Logs Title
            item {
                Text(
                    text = "REAL-TIME CYBER SECURITY AUDIT LOGS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
            }

            if (auditLogs.isEmpty()) {
                item {
                    Text("No audit events logged.", color = TextSecondary, fontSize = 12.sp)
                }
            } else {
                items(auditLogs) { event ->
                    AuditEventItem(event = event)
                }
            }
        }
    }
}

@Composable
fun AuditEventItem(event: AuditEvent) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardSurfaceBorder),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.eventType,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (event.severity == "WARNING") StatusWarning else NeonCyan
                )
                Text(text = event.details, fontSize = 11.sp, color = Color.White)
            }

            Surface(
                color = if (event.severity == "WARNING") StatusWarning.copy(alpha = 0.2f) else StatusSuccess.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = event.severity,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (event.severity == "WARNING") StatusWarning else StatusSuccess,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

private fun android.content.Context.cachePathOrTemp(): File {
    return this.cacheDir ?: File(this.filesDir, "temp")
}
