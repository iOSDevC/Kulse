package com.iosdevc.android.logger.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "log_messages",
    indices = [
        Index("session_id"),
        Index("level"),
        Index("timestamp"),
    ]
)
data class LogMessageEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") val id: Long = 0,
    @ColumnInfo(name = "session_id") val sessionId: String,
    @ColumnInfo(name = "timestamp") val timestamp: Long,
    @ColumnInfo(name = "level") val level: Int,
    @ColumnInfo(name = "message") val message: String,
    @ColumnInfo(name = "tag") val tag: String? = null,
    @ColumnInfo(name = "file") val file: String? = null,
    @ColumnInfo(name = "function") val function: String? = null,
    @ColumnInfo(name = "line") val line: Int? = null,
    @ColumnInfo(name = "transaction_id") val transactionId: Long? = null,
)
