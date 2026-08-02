package com.securebeam.app.security

import java.io.File
import java.io.RandomAccessFile
import java.security.SecureRandom

object SecureShredder {

    /**
     * DoD 5220.22-M 3-Pass Secure File Wipe Algorithm.
     * Pass 1: Write Zeros (0x00)
     * Pass 2: Write Ones (0xFF)
     * Pass 3: Write Random Bytes
     * Finally, truncate file size to 0 bytes and delete from filesystem.
     */
    fun shredFile(file: File): Boolean {
        if (!file.exists()) return false
        return try {
            val length = file.length()
            if (length > 0) {
                RandomAccessFile(file, "rws").use { raf ->
                    // Pass 1: Zeroize
                    overwriteWithByte(raf, length, 0x00.toByte())
                    // Pass 2: Ones
                    overwriteWithByte(raf, length, 0xFF.toByte())
                    // Pass 3: Secure Random
                    overwriteWithRandom(raf, length)
                    raf.setLength(0)
                }
            }
            file.delete()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun overwriteWithByte(raf: RandomAccessFile, length: Long, fillByte: Byte) {
        raf.seek(0)
        val bufferSize = 8192
        val buffer = ByteArray(bufferSize) { fillByte }
        var written: Long = 0
        while (written < length) {
            val bytesToWrite = minOf(bufferSize.toLong(), length - written).toInt()
            raf.write(buffer, 0, bytesToWrite)
            written += bytesToWrite
        }
    }

    private fun overwriteWithRandom(raf: RandomAccessFile, length: Long) {
        raf.seek(0)
        val bufferSize = 8192
        val buffer = ByteArray(bufferSize)
        val random = SecureRandom()
        var written: Long = 0
        while (written < length) {
            val bytesToWrite = minOf(bufferSize.toLong(), length - written).toInt()
            random.nextBytes(buffer)
            raf.write(buffer, 0, bytesToWrite)
            written += bytesToWrite
        }
    }
}
