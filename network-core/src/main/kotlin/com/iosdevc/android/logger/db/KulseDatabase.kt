package com.iosdevc.android.logger.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.iosdevc.android.logger.db.dao.HttpTransactionDao
import com.iosdevc.android.logger.db.dao.LogMessageDao
import com.iosdevc.android.logger.db.dao.SessionDao
import com.iosdevc.android.logger.db.entity.HttpTransactionEntity
import com.iosdevc.android.logger.db.entity.LogMessageEntity
import com.iosdevc.android.logger.db.entity.SessionEntity

@Database(
    entities = [
        HttpTransactionEntity::class,
        LogMessageEntity::class,
        SessionEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class KulseDatabase : RoomDatabase() {

    abstract fun transactionDao(): HttpTransactionDao
    abstract fun logMessageDao(): LogMessageDao
    abstract fun sessionDao(): SessionDao

    companion object {
        private const val DB_NAME = "kulse.db"

        @Volatile
        private var instance: KulseDatabase? = null

        fun getInstance(context: Context): KulseDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    KulseDatabase::class.java,
                    DB_NAME,
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { instance = it }
            }
        }
    }
}
