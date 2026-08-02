package com.securebeam.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.securebeam.app.ui.screens.*
import com.securebeam.app.ui.theme.ObsidianDark
import com.securebeam.app.ui.theme.SecureBeamTheme
import com.securebeam.app.ui.viewmodel.SecureBeamViewModel

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = (application as SecureBeamApp).repository

        setContent {
            SecureBeamTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = ObsidianDark
                ) {
                    val viewModel: SecureBeamViewModel = viewModel(
                        factory = SecureBeamViewModel.Factory(repository)
                    )
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "auth"
                    ) {
                        composable("auth") {
                            AuthScreen(
                                onAuthenticated = {
                                    navController.navigate("dashboard") {
                                        popUpTo("auth") { inclusive = true }
                                    }
                                },
                                onBiometricRequested = {
                                    showBiometricPrompt { success ->
                                        if (success) {
                                            viewModel.setAuthenticated(true)
                                            navController.navigate("dashboard") {
                                                popUpTo("auth") { inclusive = true }
                                            }
                                        }
                                    }
                                },
                                viewModel = viewModel
                            )
                        }
                        composable("dashboard") {
                            DashboardScreen(
                                viewModel = viewModel,
                                onNavigateToSend = { navController.navigate("sender") },
                                onNavigateToReceive = { navController.navigate("receiver") },
                                onNavigateToSecurity = { navController.navigate("security") },
                                onNavigateToSettings = { navController.navigate("settings") }
                            )
                        }
                        composable("sender") {
                            SenderScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("receiver") {
                            ReceiverScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("security") {
                            SecurityAuditScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("settings") {
                            SettingsScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun showBiometricPrompt(onResult: (Boolean) -> Unit) {
        val executor = ContextCompat.getMainExecutor(this)
        val prompt = BiometricPrompt(
            this,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Toast.makeText(this@MainActivity, "Biometric Auth Succeeded", Toast.LENGTH_SHORT).show()
                    onResult(true)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(this@MainActivity, "Biometric Auth Error: $errString", Toast.LENGTH_SHORT).show()
                    onResult(false)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(this@MainActivity, "Biometric Auth Failed", Toast.LENGTH_SHORT).show()
                    onResult(false)
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("SecureBeam Biometric Lock")
            .setSubtitle("Confirm your identity to access air-gapped file transfers")
            .setNegativeButtonText("Use PIN Lock")
            .build()

        prompt.authenticate(promptInfo)
    }
}
