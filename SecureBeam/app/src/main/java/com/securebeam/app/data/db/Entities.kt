package com.securebeam.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transfer_records")
data class TransferRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fileName: String,
    val fileSize: Long,
    val fileType: String,
    val direction: String, // "SENDER" or "RECEIVER"
    val status: String,    // "COMPLETED", "FAILED", "INTERRUPTED"
    val speedKbps: Float,
    val durationSeconds: Float,
    val timestamp: Long = System.currentTimeMillis(),
    val fileHash: String,
    val certificateId: String
)

@Entity(tableName = "audit_events")
data class AuditEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val eventType: String, // "AUTH_SUCCESS", "FILE_ENCRYPTED", "PACKET_INTEGRITY_PASS", "SHRED_COMPLETED"
    val severity: String,  // "INFO", "WARNING", "SECURITY_ALERT"
    val details: String,
    val deviceId: String
)

@Entity(tableName = "trusted_devices")
data class TrustedDevice(
    @PrimaryKey val deviceId: String,
    val deviceName: String,
    val publicKeyPem: String,
    val trustScore: Int, // 0 to 100
    val totalTransfers: Int,
    val lastSeen: Long = System.currentTimeMillis()
)

@Entity(tableName = "security_certificates")
data class SecurityCertificate(
    @PrimaryKey val certificateId: String,
    val transferRecordId: Long,
    val fileName: String,
    val fileSize: Long,
    val sha256Hash: String,
    val digitalSignature: String,
    val issuedAt: Long = System.currentTimeMillis()
)
