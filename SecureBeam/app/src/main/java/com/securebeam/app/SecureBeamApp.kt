package com.securebeam.app

import android.app.Application
import com.securebeam.app.data.db.AppDatabase
import com.securebeam.app.data.repository.SecureBeamRepository

class SecureBeamApp : Application() {

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val repository: SecureBeamRepository by lazy {
        SecureBeamRepository(
            transferDao = database.transferDao(),
            auditDao = database.auditDao(),
            deviceDao = database.deviceDao()
        )
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: SecureBeamApp
            private set
    }
}
