package com.iosdevc.android.logger.internal

import com.iosdevc.android.logger.KulseConfig
import com.iosdevc.android.logger.db.KulseDatabase

internal object RetentionManager {

    suspend fun sweep(database: KulseDatabase, config: KulseConfig) {
        val cutoff = System.currentTimeMillis() - config.maxAge.inWholeMilliseconds
        database.transactionDao().deleteOlderThan(cutoff)
        database.logMessageDao().deleteOlderThan(cutoff)

        val totalSize = database.transactionDao().totalBodySize()
        if (totalSize > config.maxStorageSize) {
            val count = database.transactionDao().count()
            if (count > 0) {
                val deleteBeforeDate = System.currentTimeMillis() - (config.maxAge.inWholeMilliseconds / 3)
                database.transactionDao().deleteOlderThan(deleteBeforeDate)
            }
        }
    }
}
