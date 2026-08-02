package com.securebeam.app.protocol

import android.util.Base64
import com.securebeam.app.crypto.CryptoEngine
import java.io.ByteArrayOutputStream
import java.util.zip.Inflater

class PacketReassembler {

    private var sessionId: String? = null
    private var totalPackets: Int = 0
    private var fileName: String = ""
    private var fileSize: Long = 0
    private var expectedFileHash: String = ""
    private var ivBase64: String = ""
    private var keyBase64: String = ""

    private val receivedChunks = mutableMapOf<Int, ByteArray>()
    val missingIndices = mutableSetOf<Int>()

    fun processFrame(frame: FramePacket): ReassemblyState {
        // Initialize transfer session context if first frame received
        if (sessionId == null) {
            sessionId = frame.sessionId
            totalPackets = frame.totalPackets
            fileName = frame.fileName
            fileSize = frame.fileSize
            expectedFileHash = frame.fileHash
            ivBase64 = frame.ivBase64
            keyBase64 = frame.keyBase64

            for (i in 1..totalPackets) {
                missingIndices.add(i)
            }
        }

        // Validate session match
        if (frame.sessionId != sessionId) {
            return ReassemblyState.InvalidSession
        }

        // Add chunk to received buffer
        if (!receivedChunks.containsKey(frame.packetIndex)) {
            val chunkBytes = Base64.decode(frame.payloadBase64, Base64.NO_WRAP)
            receivedChunks[frame.packetIndex] = chunkBytes
            missingIndices.remove(frame.packetIndex)
        }

        val receivedCount = receivedChunks.size
        val progress = if (totalPackets > 0) (receivedCount.toFloat() / totalPackets) else 0f

        if (receivedCount < totalPackets) {
            return ReassemblyState.InProgress(
                receivedCount = receivedCount,
                totalCount = totalPackets,
                progressPercent = (progress * 100).toInt(),
                missingCount = missingIndices.size
            )
        }

        // 100% of frames received: Reassemble & Decrypt
        return try {
            val assembledEncryptedBytes = ByteArrayOutputStream()
            for (i in 1..totalPackets) {
                val chunk = receivedChunks[i] ?: return ReassemblyState.CorruptedData("Missing chunk $i")
                assembledEncryptedBytes.write(chunk)
            }

            val encryptedBytes = assembledEncryptedBytes.toByteArray()
            val iv = Base64.decode(ivBase64, Base64.NO_WRAP)
            val keyBytes = Base64.decode(keyBase64, Base64.NO_WRAP)
            val secretKey = CryptoEngine.secretKeyFromBytes(keyBytes)

            // AES-256-GCM Decryption
            val compressedBytes = CryptoEngine.decryptAesGcm(encryptedBytes, secretKey, iv)

            // Inflate Decompression
            val originalBytes = decompress(compressedBytes)

            // Integrity Hash Verification
            val actualHash = CryptoEngine.calculateSha256(originalBytes)
            if (actualHash != expectedFileHash) {
                return ReassemblyState.HashMismatch(
                    expected = expectedFileHash,
                    actual = actualHash
                )
            }

            ReassemblyState.Completed(
                fileName = fileName,
                fileSize = fileSize,
                fileBytes = originalBytes,
                fileHash = actualHash
            )
        } catch (e: Exception) {
            ReassemblyState.CorruptedData(e.message ?: "Decryption or decompression failed")
        }
    }

    private fun decompress(data: ByteArray): ByteArray {
        val inflater = Inflater()
        inflater.setInput(data)

        val outputStream = ByteArrayOutputStream(data.size)
        val buffer = ByteArray(1024)
        while (!inflater.finished()) {
            val count = inflater.inflate(buffer)
            outputStream.write(buffer, 0, count)
        }
        inflater.end()
        return outputStream.toByteArray()
    }

    fun reset() {
        sessionId = null
        totalPackets = 0
        receivedChunks.clear()
        missingIndices.clear()
    }
}

sealed class ReassemblyState {
    object InvalidSession : ReassemblyState()
    data class InProgress(
        val receivedCount: Int,
        val totalCount: Int,
        val progressPercent: Int,
        val missingCount: Int
    ) : ReassemblyState()
    data class Completed(
        val fileName: String,
        val fileSize: Long,
        val fileBytes: ByteArray,
        val fileHash: String
    ) : ReassemblyState()
    data class CorruptedData(val reason: String) : ReassemblyState()
    data class HashMismatch(val expected: String, val actual: String) : ReassemblyState()
}
