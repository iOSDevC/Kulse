package com.iosdevc.android.logger.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.iosdevc.android.logger.db.entity.LogMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LogMessageDao {

    @Insert
    suspend fun insert(message: LogMessageEntity): Long

    @Query("SELECT * FROM log_messages ORDER BY timestamp DESC")
    fun getAllFlow(): Flow<List<LogMessageEntity>>

    @Query("SELECT * FROM log_messages WHERE level >= :minLevel ORDER BY timestamp DESC LIMIT :limit")
    fun getByMinLevel(minLevel: Int, limit: Int = 500): Flow<List<LogMessageEntity>>

    @Query("DELETE FROM log_messages")
    suspend fun deleteAll()

    @Query("DELETE FROM log_messages WHERE timestamp < :before")
    suspend fun deleteOlderThan(before: Long)
}
