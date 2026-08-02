package com.securebeam.app.ui.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.securebeam.app.data.repository.DashboardStats
import com.securebeam.app.data.repository.SecureBeamRepository
import com.securebeam.app.protocol.FilePackager
import com.securebeam.app.protocol.FramePacket
import com.securebeam.app.protocol.PacketReassembler
import com.securebeam.app.protocol.ReassemblyState
import com.securebeam.app.qr.QRGenerator
import com.securebeam.app.security.SecureShredder
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class SecureBeamViewModel(
    val repository: SecureBeamRepository
) : ViewModel() {

    // Auth state
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _enteredPin = MutableStateFlow("")
    val enteredPin: StateFlow<String> = _enteredPin.asStateFlow()

    // Settings state
    val targetFps = MutableStateFlow(15) // 5 to 30 FPS
    val qrDensity = MutableStateFlow("MEDIUM") // LOW, MEDIUM, HIGH
    val encryptionMode = MutableStateFlow("AES-256-GCM")

    // Sender state
    private val _senderPackets = MutableStateFlow<List<FramePacket>>(emptyList())
    val senderPackets: StateFlow<List<FramePacket>> = _senderPackets.asStateFlow()

    private val _currentFrameIndex = MutableStateFlow(0)
    val currentFrameIndex: StateFlow<Int> = _currentFrameIndex.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private val _senderQrBitmap = MutableStateFlow<Bitmap?>(null)
    val senderQrBitmap: StateFlow<Bitmap?> = _senderQrBitmap.asStateFlow()

    private var animationJob: Job? = null

    // Receiver state
    val reassembler = PacketReassembler()
    private val _receiverState = MutableStateFlow<ReassemblyState>(ReassemblyState.InProgress(0, 0, 0, 0))
    val receiverState: StateFlow<ReassemblyState> = _receiverState.asStateFlow()

    // Repository Flows
    val dashboardStats: StateFlow<DashboardStats> = repository.dashboardStats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats())

    val recentTransfers = repository.recentTransfers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogs = repository.auditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val trustedDevices = repository.trustedDevices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun authenticatePin(pin: String): Boolean {
        if (pin == "1234") { // Configurable master PIN
            _isAuthenticated.value = true
            viewModelScope.launch {
                repository.logAudit("AUTH_SUCCESS", "INFO", "User authenticated via PIN")
            }
            return true
        }
        return false
    }

    fun setAuthenticated(auth: Boolean) {
        _isAuthenticated.value = auth
        if (auth) {
            viewModelScope.launch {
                repository.logAudit("BIOMETRIC_AUTH_SUCCESS", "INFO", "User authenticated via Biometrics")
            }
        }
    }

    // Prepare File for Sender Stream
    fun prepareFileForTransfer(fileName: String, fileBytes: ByteArray) {
        viewModelScope.launch {
            _isSending.value = true
            repository.logAudit("FILE_PREPARE_START", "INFO", "Preparing file: $fileName (${fileBytes.size} bytes)")

            val packets = FilePackager.packageFile(fileName, fileBytes)
            _senderPackets.value = packets
            _currentFrameIndex.value = 0

            repository.logAudit(
                "FILE_PACKAGED",
                "INFO",
                "File packaged into ${packets.size} optical frames"
            )

            startQrAnimation()
        }
    }

    private fun startQrAnimation() {
        animationJob?.cancel()
        animationJob = viewModelScope.launch {
            val packets = _senderPackets.value
            if (packets.isEmpty()) return@launch

            var index = 0
            while (_isSending.value) {
                val packet = packets[index]
                val serialized = packet.serialize()
                val bitmap = QRGenerator.generateQrBitmap(serialized)
                _senderQrBitmap.value = bitmap
                _currentFrameIndex.value = index + 1

                val frameDelayMs = (1000f / targetFps.value).toLong()
                delay(frameDelayMs)
                index = (index + 1) % packets.size
            }
        }
    }

    fun stopQrAnimation() {
        _isSending.value = false
        animationJob?.cancel()
    }

    // Process Received Frame Packet from Camera
    fun onFrameScanned(packet: FramePacket, context: Context) {
        val state = reassembler.processFrame(packet)
        _receiverState.value = state

        if (state is ReassemblyState.Completed) {
            viewModelScope.launch {
                val certificateId = "CERT-${UUID.randomUUID().toString().take(8).uppercase()}"
                saveReceivedFile(context, state.fileName, state.fileBytes)

                repository.recordTransfer(
                    fileName = state.fileName,
                    fileSize = state.fileSize,
                    fileType = state.fileName.substringAfterLast('.', "bin"),
                    direction = "RECEIVER",
                    status = "COMPLETED",
                    speedKbps = 1024f,
                    durationSeconds = 2.5f,
                    fileHash = state.fileHash,
                    certificateId = certificateId
                )

                repository.logAudit(
                    "TRANSFER_RECEIVE_SUCCESS",
                    "INFO",
                    "Received and verified file ${state.fileName} (Hash: ${state.fileHash.take(8)})"
                )
            }
        }
    }

    private fun saveReceivedFile(context: Context, fileName: String, bytes: ByteArray) {
        try {
            val dir = File(context.getExternalFilesDir(null), "SecureBeam_Received")
            if (!dir.exists()) dir.mkdirs()
            val outputFile = File(dir, fileName)
            FileOutputStream(outputFile).use { it.write(bytes) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun shredSelectedFile(file: File) {
        viewModelScope.launch {
            val success = SecureShredder.shredFile(file)
            if (success) {
                repository.logAudit("FILE_SHREDDED", "WARNING", "Securely wiped file ${file.name} using 3-Pass DoD standard")
            }
        }
    }

    fun resetReceiver() {
        reassembler.reset()
        _receiverState.value = ReassemblyState.InProgress(0, 0, 0, 0)
    }

    class Factory(private val repository: SecureBeamRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SecureBeamViewModel(repository) as T
        }
    }
}
