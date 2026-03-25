package com.iosdevc.android.logger.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "http_transactions",
    indices = [
        Index("session_id"),
        Index("host"),
        Index("response_code"),
        Index("request_date"),
    ]
)
data class HttpTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") val id: Long = 0,

    @ColumnInfo(name = "session_id") val sessionId: String,

    // Request
    @ColumnInfo(name = "request_date") val requestDate: Long? = null,
    @ColumnInfo(name = "method") val method: String? = null,
    @ColumnInfo(name = "url") val url: String? = null,
    @ColumnInfo(name = "host") val host: String? = null,
    @ColumnInfo(name = "path") val path: String? = null,
    @ColumnInfo(name = "scheme") val scheme: String? = null,
    @ColumnInfo(name = "request_content_type") val requestContentType: String? = null,
    @ColumnInfo(name = "request_headers") val requestHeaders: String? = null,
    @ColumnInfo(name = "request_body") val requestBody: String? = null,
    @ColumnInfo(name = "request_body_size") val requestBodySize: Long = 0,
    @ColumnInfo(name = "is_request_body_encoded") val isRequestBodyEncoded: Boolean = false,

    // Response
    @ColumnInfo(name = "response_date") val responseDate: Long? = null,
    @ColumnInfo(name = "response_code") val responseCode: Int? = null,
    @ColumnInfo(name = "response_message") val responseMessage: String? = null,
    @ColumnInfo(name = "response_content_type") val responseContentType: String? = null,
    @ColumnInfo(name = "response_headers") val responseHeaders: String? = null,
    @ColumnInfo(name = "response_body") val responseBody: String? = null,
    @ColumnInfo(name = "response_body_size") val responseBodySize: Long = 0,
    @ColumnInfo(name = "is_response_body_encoded") val isResponseBodyEncoded: Boolean = false,

    // Protocol & Security
    @ColumnInfo(name = "protocol") val protocol: String? = null,
    @ColumnInfo(name = "tls_version") val tlsVersion: String? = null,
    @ColumnInfo(name = "cipher_suite") val cipherSuite: String? = null,

    // Timing (milliseconds)
    @ColumnInfo(name = "duration") val duration: Long? = null,
    @ColumnInfo(name = "dns_start") val dnsStart: Long? = null,
    @ColumnInfo(name = "dns_end") val dnsEnd: Long? = null,
    @ColumnInfo(name = "connect_start") val connectStart: Long? = null,
    @ColumnInfo(name = "connect_end") val connectEnd: Long? = null,
    @ColumnInfo(name = "tls_start") val tlsStart: Long? = null,
    @ColumnInfo(name = "tls_end") val tlsEnd: Long? = null,
    @ColumnInfo(name = "request_start") val requestStart: Long? = null,
    @ColumnInfo(name = "request_end") val requestEnd: Long? = null,
    @ColumnInfo(name = "response_start") val responseStart: Long? = null,
    @ColumnInfo(name = "response_end") val responseEnd: Long? = null,

    // Error
    @ColumnInfo(name = "error") val error: String? = null,

    // Flags
    @ColumnInfo(name = "is_from_cache") val isFromCache: Boolean = false,

    // State: 0 = pending, 1 = complete, 2 = failed
    @ColumnInfo(name = "state") val state: Int = 0,
)
