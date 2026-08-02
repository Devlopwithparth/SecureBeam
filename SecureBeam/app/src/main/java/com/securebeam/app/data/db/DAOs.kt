package com.securebeam.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TransferDao {
    @Query("SELECT * FROM transfer_records ORDER BY timestamp DESC")
    fun getAllTransfers(): Flow<List<TransferRecord>>

    @Query("SELECT * FROM transfer_records ORDER BY timestamp DESC LIMIT 5")
    fun getRecentTransfers(): Flow<List<TransferRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransfer(record: TransferRecord): Long

    @Query("SELECT COUNT(*) FROM transfer_records")
    fun getTotalTransfersCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM transfer_records WHERE status = 'COMPLETED'")
    fun getSuccessfulTransfersCount(): Flow<Int>

    @Query("SELECT SUM(fileSize) FROM transfer_records WHERE status = 'COMPLETED'")
    fun getTotalStorageUsedBytes(): Flow<Long?>

    @Query("SELECT AVG(speedKbps) FROM transfer_records WHERE status = 'COMPLETED'")
    fun getAverageSpeedKbps(): Flow<Float?>
}

@Dao
interface AuditDao {
    @Query("SELECT * FROM audit_events ORDER BY timestamp DESC")
    fun getAllAuditEvents(): Flow<List<AuditEvent>>

    @Query("SELECT * FROM audit_events ORDER BY timestamp DESC LIMIT 20")
    fun getRecentAuditEvents(): Flow<List<AuditEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditEvent(event: AuditEvent)
}

@Dao
interface DeviceDao {
    @Query("SELECT * FROM trusted_devices ORDER BY lastSeen DESC")
    fun getAllDevices(): Flow<List<TrustedDevice>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDevice(device: TrustedDevice)

    @Query("SELECT * FROM trusted_devices WHERE deviceId = :id")
    suspend fun getDeviceById(id: String): TrustedDevice?
}
