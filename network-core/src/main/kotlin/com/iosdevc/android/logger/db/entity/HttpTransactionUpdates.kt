package com.iosdevc.android.logger.db.entity

import androidx.room.ColumnInfo

// Partial updates of http_transactions. Each one only touches its own columns, so the
// writes for a transaction can never overwrite each other's data.

/** Response metadata, written as soon as the response headers arrive. */
data class TransactionResponseUpdate(
    @ColumnInfo(name = "id") val id: Long,
    @ColumnInfo(name = "response_date") val responseDate: Long?,
    @ColumnInfo(name = "response_code") val responseCode: Int?,
    @ColumnInfo(name = "response_message") val responseMessage: String?,
    @ColumnInfo(name = "response_content_type") val responseContentType: String?,
    @ColumnInfo(name = "response_headers") val responseHeaders: String?,
    @ColumnInfo(name = "is_response_body_encoded") val isResponseBodyEncoded: Boolean,
    @ColumnInfo(name = "protocol") val protocol: String?,
    @ColumnInfo(name = "tls_version") val tlsVersion: String?,
    @ColumnInfo(name = "cipher_suite") val cipherSuite: String?,
    @ColumnInfo(name = "is_from_cache") val isFromCache: Boolean,
)

/** Final outcome, written when the response body ends or the call fails. */
data class TransactionCompletionUpdate(
    @ColumnInfo(name = "id") val id: Long,
    @ColumnInfo(name = "response_body") val responseBody: String?,
    @ColumnInfo(name = "response_body_size") val responseBodySize: Long,
    @ColumnInfo(name = "duration") val duration: Long?,
    @ColumnInfo(name = "error") val error: String?,
    @ColumnInfo(name = "state") val state: Int,
)

/** Phase timings collected by the EventListener. */
data class TransactionTimingsUpdate(
    @ColumnInfo(name = "id") val id: Long,
    @ColumnInfo(name = "dns_start") val dnsStart: Long?,
    @ColumnInfo(name = "dns_end") val dnsEnd: Long?,
    @ColumnInfo(name = "connect_start") val connectStart: Long?,
    @ColumnInfo(name = "connect_end") val connectEnd: Long?,
    @ColumnInfo(name = "tls_start") val tlsStart: Long?,
    @ColumnInfo(name = "tls_end") val tlsEnd: Long?,
    @ColumnInfo(name = "request_start") val requestStart: Long?,
    @ColumnInfo(name = "request_end") val requestEnd: Long?,
    @ColumnInfo(name = "response_start") val responseStart: Long?,
    @ColumnInfo(name = "response_end") val responseEnd: Long?,
)
