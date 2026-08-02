package com.securebeam.app.protocol

import android.util.Base64
import java.util.zip.CRC32

data class FramePacket(
    val sessionId: String,
    val packetIndex: Int,
    val totalPackets: Int,
    val fileName: String,
    val fileSize: Long,
    val fileHash: String,
    val ivBase64: String,
    val keyBase64: String,
    val payloadBase64: String,
    val crc32: Long
) {
    /**
     * Compact string representation for QR encoding:
     * SECBEAM|SessionID|Index|Total|FileName|Size|Hash|IV|Key|Payload|CRC32
     */
    fun serialize(): String {
        val rawContent = "$sessionId|$packetIndex|$totalPackets|$fileName|$fileSize|$fileHash|$ivBase64|$keyBase64|$payloadBase64"
        val calculatedCrc = computeCrc32(rawContent.toByteArray(Charsets.UTF_8))
        return "SECBEAM|$rawContent|$calculatedCrc"
    }

    companion object {
        fun deserialize(qrString: String): FramePacket? {
            return try {
                if (!qrString.startsWith("SECBEAM|")) return null
                val parts = qrString.split("|")
                if (parts.size < 11) return null

                val sessionId = parts[1]
                val packetIndex = parts[2].toInt()
                val totalPackets = parts[3].toInt()
                val fileName = parts[4]
                val fileSize = parts[5].toLong()
                val fileHash = parts[6]
                val ivBase64 = parts[7]
                val keyBase64 = parts[8]
                val payloadBase64 = parts[9]
                val expectedCrc = parts[10].toLong()

                val rawContent = "$sessionId|$packetIndex|$totalPackets|$fileName|$fileSize|$fileHash|$ivBase64|$keyBase64|$payloadBase64"
                val actualCrc = computeCrc32(rawContent.toByteArray(Charsets.UTF_8))

                if (actualCrc != expectedCrc) return null

                FramePacket(
                    sessionId = sessionId,
                    packetIndex = packetIndex,
                    totalPackets = totalPackets,
                    fileName = fileName,
                    fileSize = fileSize,
                    fileHash = fileHash,
                    ivBase64 = ivBase64,
                    keyBase64 = keyBase64,
                    payloadBase64 = payloadBase64,
                    crc32 = actualCrc
                )
            } catch (e: Exception) {
                null
            }
        }

        fun computeCrc32(bytes: ByteArray): Long {
            val crc = CRC32()
            crc.update(bytes)
            return crc.value
        }
    }
}
