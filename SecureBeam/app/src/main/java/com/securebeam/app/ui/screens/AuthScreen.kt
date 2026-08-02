package com.securebeam.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.securebeam.app.ui.theme.*

@Composable
fun AuthScreen(
    onAuthenticated: () -> Unit,
    onBiometricRequested: () -> Unit,
    viewModel: com.securebeam.app.ui.viewmodel.SecureBeamViewModel
) {
    var pinText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    val isAuth by viewModel.isAuthenticated.collectAsState()

    LaunchedEffect(isAuth) {
        if (isAuth) {
            onAuthenticated()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianDark)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // App Branding Icon
            Surface(
                color = CardSurface,
                shape = CircleShape,
                border = androidx.compose.foundation.BorderStroke(2.dp, NeonCyan),
                modifier = Modifier.size(90.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "SecureBeam Shield",
                        tint = NeonCyan,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Text(
                text = "SecureBeam",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "Air-Gapped Encrypted Transfer Engine\nPlease authenticate to proceed",
                fontSize = 12.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            // PIN Indicator Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                repeat(4) { index ->
                    val isFilled = index < pinText.length
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(
                                color = if (isFilled) NeonCyan else CardSurfaceBorder,
                                shape = CircleShape
                            )
                    )
                }
            }

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = StatusError,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Keypad Grid
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val buttons = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("C", "0", "OK")
                )

                buttons.forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        row.forEach { digit ->
                            Button(
                                onClick = {
                                    when (digit) {
                                        "C" -> {
                                            if (pinText.isNotEmpty()) pinText = pinText.dropLast(1)
                                            errorMessage = ""
                                        }
                                        "OK" -> {
                                            val success = viewModel.authenticatePin(pinText)
                                            if (!success) {
                                                errorMessage = "Invalid PIN (Default: 1234)"
                                                pinText = ""
                                            }
                                        }
                                        else -> {
                                            if (pinText.length < 4) {
                                                pinText += digit
                                            }
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CardSurface),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.size(64.dp)
                            ) {
                                Text(
                                    text = digit,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (digit == "OK") NeonCyan else Color.White
                                )
                            }
                        }
                    }
                }
            }

            // Biometric Option Button
            OutlinedButton(
                onClick = onBiometricRequested,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberPurple),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberPurple),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = "Biometric Login",
                    tint = CyberPurple
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Biometric Authentication", fontWeight = FontWeight.Bold)
            }
        }
    }
}
