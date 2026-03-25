package com.iosdevc.android.logger.repository

import com.iosdevc.android.logger.db.KulseDatabase
import com.iosdevc.android.logger.db.entity.HttpTransactionEntity
import com.iosdevc.android.logger.db.entity.SessionEntity
import com.iosdevc.android.logger.internal.HeaderSerializer
import com.iosdevc.android.logger.model.HttpTransaction
import com.iosdevc.android.logger.model.Session
import com.iosdevc.android.logger.model.TransactionState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class KulseRepository internal constructor(private val database: KulseDatabase) {

    fun getTransactionsFiltered(
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
    ): Flow<List<HttpTransaction>> {
        return database.transactionDao().getFiltered(
            sessionId = sessionId,
            method = method,
            host = host,
            minStatusCode = minStatusCode,
            maxStatusCode = maxStatusCode,
            searchQuery = searchQuery,
            afterDate = afterDate,
            beforeDate = beforeDate,
            limit = limit,
            offset = offset,
        ).map { entities -> entities.map { it.toDomain() } }
    }

    fun getTransactionById(id: Long): Flow<HttpTransaction?> {
        return database.transactionDao().getByIdFlow(id).map { it?.toDomain() }
    }

    fun getAllHosts(): Flow<List<String>> {
        return database.transactionDao().getAllHosts()
    }

    fun getSessions(): Flow<List<Session>> {
        return database.sessionDao().getAllFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun getSessionsWithData(): List<Session> {
        val idsWithData = database.transactionDao().getSessionIdsWithData().toSet()
        val allSessions = database.sessionDao().getAll()
        return allSessions.filter { it.id in idsWithData }.map { it.toDomain() }
    }

    suspend fun getAllTransactions(): List<HttpTransaction> {
        return database.transactionDao().getAll().map { it.toDomain() }
    }

    suspend fun deleteAllTransactions() {
        database.transactionDao().deleteAll()
        database.logMessageDao().deleteAll()
    }

    suspend fun deleteAllSessions() {
        database.sessionDao().deleteAll()
    }

    suspend fun getStoreStats(): StoreStats {
        val transactionCount = database.transactionDao().count()
        val totalBodySize = database.transactionDao().totalBodySize()
        return StoreStats(
            requestCount = transactionCount,
            totalBodySize = totalBodySize,
        )
    }
}

data class StoreStats(
    val requestCount: Int,
    val totalBodySize: Long,
)

internal fun HttpTransactionEntity.toDomain(): HttpTransaction = HttpTransaction(
    id = id,
    sessionId = sessionId,
    requestDate = requestDate,
    method = method,
    url = url,
    host = host,
    path = path,
    scheme = scheme,
    requestContentType = requestContentType,
    requestHeaders = HeaderSerializer.deserialize(requestHeaders),
    requestBody = requestBody,
    requestBodySize = requestBodySize,
    responseDate = responseDate,
    responseCode = responseCode,
    responseMessage = responseMessage,
    responseContentType = responseContentType,
    responseHeaders = HeaderSerializer.deserialize(responseHeaders),
    responseBody = responseBody,
    responseBodySize = responseBodySize,
    protocol = protocol,
    tlsVersion = tlsVersion,
    cipherSuite = cipherSuite,
    duration = duration,
    dnsStart = dnsStart,
    dnsEnd = dnsEnd,
    connectStart = connectStart,
    connectEnd = connectEnd,
    tlsStart = tlsStart,
    tlsEnd = tlsEnd,
    requestStart = requestStart,
    requestEnd = requestEnd,
    responseStart = responseStart,
    responseEnd = responseEnd,
    error = error,
    isFromCache = isFromCache,
    state = TransactionState.fromInt(state),
)

internal fun SessionEntity.toDomain(): Session = Session(
    id = id,
    startedAt = startedAt,
    appVersion = appVersion,
    buildNumber = buildNumber,
    deviceInfo = deviceInfo,
)
