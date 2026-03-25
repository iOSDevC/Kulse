package com.iosdevc.android.logger.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "started_at") val startedAt: Long,
    @ColumnInfo(name = "app_version") val appVersion: String? = null,
    @ColumnInfo(name = "build_number") val buildNumber: String? = null,
    @ColumnInfo(name = "device_info") val deviceInfo: String? = null,
)
