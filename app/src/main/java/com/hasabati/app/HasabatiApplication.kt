package com.hasabati.app

import android.app.Application
import com.hasabati.app.data.db.AppDatabase
import com.hasabati.app.data.repository.BackupManager
import com.hasabati.app.data.repository.HasabatiRepository

/**
 * حقن تبعيات بسيط (بدون مكتبات خارجية) — يوفّر نسخة واحدة من قاعدة البيانات والـ Repository
 * لكل التطبيق طوال دورة حياته.
 */
class HasabatiApplication : Application() {
    lateinit var repository: HasabatiRepository
        private set
    lateinit var backupManager: BackupManager
        private set

    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.getInstance(this)
        repository = HasabatiRepository(db)
        backupManager = BackupManager(this, db)
    }
}
