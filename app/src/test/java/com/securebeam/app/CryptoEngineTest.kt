package com.securebeam.app

import com.securebeam.app.crypto.CryptoEngine
import org.junit.Assert.*
import org.junit.Test

class CryptoEngineTest {

    @Test
    fun testAesGcmEncryptionDecryption() {
        val originalText = "SecureBeam Air-Gapped High-Speed Encryption Test Data"
        val originalBytes = originalText.toByteArray(Charsets.UTF_8)

        val key = CryptoEngine.generateSessionKey()
        val iv = CryptoEngine.generateIv()

        val encryptedBytes = CryptoEngine.encryptAesGcm(originalBytes, key, iv)
        assertNotNull(encryptedBytes)
        assertFalse(originalBytes.contentEquals(encryptedBytes))

        val decryptedBytes = CryptoEngine.decryptAesGcm(encryptedBytes, key, iv)
        val decryptedText = String(decryptedBytes, Charsets.UTF_8)

        assertEquals(originalText, decryptedText)
    }

    @Test
    fun testRsaSignatureVerification() {
        val payload = "VERIFY_SENDER_DIGITAL_SIGNATURE_PAYLOAD".toByteArray()

        val keyPair = CryptoEngine.generateRsaKeyPair()
        val signature = CryptoEngine.signData(payload, keyPair.private)

        val isValid = CryptoEngine.verifySignature(payload, signature, keyPair.public)
        assertTrue(isValid)

        val tamperedPayload = "VERIFY_SENDER_DIGITAL_SIGNATURE_TAMPERED".toByteArray()
        val isTamperedValid = CryptoEngine.verifySignature(tamperedPayload, signature, keyPair.public)
        assertFalse(isTamperedValid)
    }

    @Test
    fun testSha256Checksum() {
        val text = "SecureBeam SHA256 Test"
        val hash1 = CryptoEngine.calculateSha256(text.toByteArray())
        val hash2 = CryptoEngine.calculateSha256(text.toByteArray())

        assertEquals(hash1, hash2)
        assertEquals(64, hash1.length)
    }
}
