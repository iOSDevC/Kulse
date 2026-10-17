package com.iosdevc.android.logger.internal

import androidx.sqlite.db.SimpleSQLiteQuery
import com.iosdevc.android.logger.KulseConfig
import com.iosdevc.android.logger.db.KulseDatabase

internal object RetentionManager {

    suspend fun sweep(database: KulseDatabase, config: KulseConfig) {
        val cutoff = System.currentTimeMillis() - config.maxAge.inWholeMilliseconds
        database.transactionDao().deleteOlderThan(cutoff)
        database.logMessageDao().deleteOlderThan(cutoff)
        trimToSize(database, config.maxStorageSize)
    }

    // Deletes the oldest transactions, a tenth at a time, until the database fits in maxBytes.
    private suspend fun trimToSize(database: KulseDatabase, maxBytes: Long) {
        val dao = database.transactionDao()
        while (usedBytes(database) > maxBytes) {
            val count = dao.count()
            if (count == 0) return
            dao.deleteOldest(maxOf(1, count / 10))
        }
    }

    /** Bytes the database actually occupies: its pages minus the free ones left by deletes. */
    fun usedBytes(database: KulseDatabase): Long {
        val usedPages = pragma(database, "page_count") - pragma(database, "freelist_count")
        return usedPages * pragma(database, "page_size")
    }

    private fun pragma(database: KulseDatabase, name: String): Long =
        database.query(SimpleSQLiteQuery("PRAGMA $name")).use { cursor ->
            if (cursor.moveToFirst()) cursor.getLong(0) else 0L
        }
}
