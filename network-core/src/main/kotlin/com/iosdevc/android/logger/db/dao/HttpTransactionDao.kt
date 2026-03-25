package com.iosdevc.android.logger.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.iosdevc.android.logger.db.entity.HttpTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HttpTransactionDao {

    @Insert
    suspend fun insert(transaction: HttpTransactionEntity): Long

    @Update
    suspend fun update(transaction: HttpTransactionEntity)

    @Query("SELECT * FROM http_transactions ORDER BY request_date DESC")
    fun getAllFlow(): Flow<List<HttpTransactionEntity>>

    @Query("SELECT * FROM http_transactions ORDER BY request_date DESC")
    suspend fun getAll(): List<HttpTransactionEntity>

    @Query("SELECT * FROM http_transactions WHERE id = :id")
    fun getByIdFlow(id: Long): Flow<HttpTransactionEntity?>

    @Query("SELECT * FROM http_transactions WHERE id = :id")
    suspend fun getById(id: Long): HttpTransactionEntity?

    @Query(
        """
        SELECT * FROM http_transactions
        WHERE (:sessionId IS NULL OR session_id = :sessionId)
          AND (:method IS NULL OR method = :method)
          AND (:host IS NULL OR host LIKE '%' || :host || '%')
          AND (:minStatusCode IS NULL OR response_code >= :minStatusCode)
          AND (:maxStatusCode IS NULL OR response_code <= :maxStatusCode)
          AND (:searchQuery IS NULL
               OR url LIKE '%' || :searchQuery || '%'
               OR request_body LIKE '%' || :searchQuery || '%'
               OR response_body LIKE '%' || :searchQuery || '%'
               OR request_headers LIKE '%' || :searchQuery || '%'
               OR response_headers LIKE '%' || :searchQuery || '%')
          AND (:afterDate IS NULL OR request_date >= :afterDate)
          AND (:beforeDate IS NULL OR request_date <= :beforeDate)
        ORDER BY request_date DESC
        LIMIT :limit OFFSET :offset
        """
    )
    fun getFiltered(
        sessionId: String? = null,
        method: String? = null,
        host: String? = null,
        minStatusCode: Int? = null,
        maxStatusCode: Int? = null,
        searchQuery: String? = null,
        afterDate: Long? = null,
        beforeDate: Long? = null,
        limit: Int = 500,
        offset: Int = 0,
    ): Flow<List<HttpTransactionEntity>>

    @Query("SELECT DISTINCT host FROM http_transactions WHERE host IS NOT NULL ORDER BY host")
    fun getAllHosts(): Flow<List<String>>

    @Query("DELETE FROM http_transactions")
    suspend fun deleteAll()

    @Query("DELETE FROM http_transactions WHERE request_date < :before")
    suspend fun deleteOlderThan(before: Long)

    @Query("SELECT COUNT(*) FROM http_transactions")
    suspend fun count(): Int

    @Query("SELECT COALESCE(SUM(request_body_size + response_body_size), 0) FROM http_transactions")
    suspend fun totalBodySize(): Long

    @Query("SELECT DISTINCT session_id FROM http_transactions")
    suspend fun getSessionIdsWithData(): List<String>
}
