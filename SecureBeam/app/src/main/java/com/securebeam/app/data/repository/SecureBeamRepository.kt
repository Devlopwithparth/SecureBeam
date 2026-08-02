package com.securebeam.app.data.repository

import com.securebeam.app.data.db.AuditDao
import com.securebeam.app.data.db.AuditEvent
import com.securebeam.app.data.db.DeviceDao
import com.securebeam.app.data.db.TransferDao
import com.securebeam.app.data.db.TransferRecord
import com.securebeam.app.data.db.TrustedDevice
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class SecureBeamRepository(
    private val transferDao: TransferDao,
    private val auditDao: AuditDao,
    private val deviceDao: DeviceDao
) {
    val allTransfers: Flow<List<TransferRecord>> = transferDao.getAllTransfers()
    val recentTransfers: Flow<List<TransferRecord>> = transferDao.getRecentTransfers()
    val auditLogs: Flow<List<AuditEvent>> = auditDao.getRecentAuditEvents()
    val trustedDevices: Flow<List<TrustedDevice>> = deviceDao.getAllDevices()

    val dashboardStats: Flow<DashboardStats> = combine(
        transferDao.getTotalTransfersCount(),
        transferDao.getSuccessfulTransfersCount(),
        transferDao.getTotalStorageUsedBytes(),
        transferDao.getAverageSpeedKbps()
    ) { total, success, storageBytes, avgSpeed ->
        val rate = if (total > 0) (success.toFloat() / total * 100).toInt() else 100
        DashboardStats(
            totalTransfers = total,
            successRatePercent = rate,
            storageUsedMb = (storageBytes ?: 0L) / (1024f * 1024f),
            averageSpeedKbps = avgSpeed ?: 0f
        )
    }

    suspend fun recordTransfer(
        fileName: String,
        fileSize: Long,
        fileType: String,
        direction: String,
        status: String,
        speedKbps: Float,
        durationSeconds: Float,
        fileHash: String,
        certificateId: String
    ): Long {
        val record = TransferRecord(
            fileName = fileName,
            fileSize = fileSize,
            fileType = fileType,
            direction = direction,
            status = status,
            speedKbps = speedKbps,
            durationSeconds = durationSeconds,
            fileHash = fileHash,
            certificateId = certificateId
        )
        val id = transferDao.insertTransfer(record)
        logAudit("FILE_TRANSFER_$direction", "INFO", "File $fileName transferred ($status)")
        return id
    }

    suspend fun logAudit(eventType: String, severity: String, details: String, deviceId: String = "LOCAL_DEV") {
        auditDao.insertAuditEvent(
            AuditEvent(
                eventType = eventType,
                severity = severity,
                details = details,
                deviceId = deviceId
            )
        )
    }

    suspend fun registerDevice(deviceId: String, deviceName: String, publicKeyPem: String) {
        val device = TrustedDevice(
            deviceId = deviceId,
            deviceName = deviceName,
            publicKeyPem = publicKeyPem,
            trustScore = 98,
            totalTransfers = 0
        )
        deviceDao.insertOrUpdateDevice(device)
        logAudit("DEVICE_REGISTERED", "INFO", "Registered trusted device: $deviceName ($deviceId)")
    }
}

data class DashboardStats(
    val totalTransfers: Int = 0,
    val successRatePercent: Int = 100,
    val storageUsedMb: Float = 0f,
    val averageSpeedKbps: Float = 0f
)
