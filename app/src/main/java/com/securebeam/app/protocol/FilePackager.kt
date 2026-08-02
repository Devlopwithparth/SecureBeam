package com.securebeam.app.protocol

import android.util.Base64
import com.securebeam.app.crypto.CryptoEngine
import java.io.ByteArrayOutputStream
import java.util.UUID
import java.util.zip.Deflater

object FilePackager {

    const val DEFAULT_CHUNK_SIZE = 384 // Bytes per frame chunk for optimal QR readability

    fun packageFile(
        fileName: String,
        fileBytes: ByteArray,
        chunkSize: Int = DEFAULT_CHUNK_SIZE
    ): List<FramePacket> {
        val sessionId = UUID.randomUUID().toString().take(8)
        val originalHash = CryptoEngine.calculateSha256(fileBytes)

        // 1. Deflate Compress
        val compressedBytes = compress(fileBytes)

        // 2. Encrypt with AES-256-GCM
        val sessionKey = CryptoEngine.generateSessionKey()
        val iv = CryptoEngine.generateIv()
        val encryptedBytes = CryptoEngine.encryptAesGcm(compressedBytes, sessionKey, iv)

        val keyBase64 = Base64.encodeToString(sessionKey.encoded, Base64.NO_WRAP)
        val ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP)

        // 3. Chunk encrypted payload into packets
        val totalPackets = (encryptedBytes.size + chunkSize - 1) / chunkSize
        val packets = mutableListOf<FramePacket>()

        for (i in 0 until totalPackets) {
            val start = i * chunkSize
            val end = minOf(start + chunkSize, encryptedBytes.size)
            val chunk = encryptedBytes.copyOfRange(start, end)
            val chunkBase64 = Base64.encodeToString(chunk, Base64.NO_WRAP)

            val packet = FramePacket(
                sessionId = sessionId,
                packetIndex = i + 1,
                totalPackets = totalPackets,
                fileName = fileName,
                fileSize = fileBytes.size.toLong(),
                fileHash = originalHash,
                ivBase64 = ivBase64,
                keyBase64 = keyBase64,
                payloadBase64 = chunkBase64,
                crc32 = 0L // Populated inside serialize()
            )
            packets.add(packet)
        }

        return packets
    }

    private fun compress(data: ByteArray): ByteArray {
        val deflater = Deflater(Deflater.BEST_COMPRESSION)
        deflater.setInput(data)
        deflater.finish()

        val outputStream = ByteArrayOutputStream(data.size)
        val buffer = ByteArray(1024)
        while (!deflater.finished()) {
            val count = deflater.deflate(buffer)
            outputStream.write(buffer, 0, count)
        }
        deflater.end()
        return outputStream.toByteArray()
    }
}
