package com.iosdevc.android.logger.model

/**
 * Immutable representation of an HTTP transaction captured by Kulse.
 *
 * Contains all request data, response data, protocol information,
 * timing metrics, and the current state of the transaction.
 *
 * @property id Unique identifier of the transaction in the database.
 * @property sessionId Identifier of the session this transaction belongs to.
 * @property requestDate Timestamp in milliseconds of when the request was sent.
 * @property method HTTP method (GET, POST, PUT, etc.).
 * @property url Full URL of the request.
 * @property host Host of the URL.
 * @property path URL path without query params.
 * @property scheme URL scheme (http or https).
 * @property requestContentType Content-Type of the request body.
 * @property requestHeaders List of name-value pairs of the request headers.
 * @property requestBody Request body as text, or `null` if there is no body.
 * @property requestBodySize Size in bytes of the request body.
 * @property responseDate Timestamp in milliseconds of when the response was received.
 * @property responseCode HTTP status code of the response.
 * @property responseMessage HTTP status message (e.g., "OK", "Not Found").
 * @property responseContentType Content-Type of the response body.
 * @property responseHeaders List of name-value pairs of the response headers.
 * @property responseBody Response body as text, or `null` if there is no body.
 * @property responseBodySize Size in bytes of the response body.
 * @property protocol Protocol used (e.g., "h2", "http/1.1").
 * @property tlsVersion Negotiated TLS version.
 * @property cipherSuite TLS cipher suite used.
 * @property duration Total duration of the transaction in milliseconds.
 * @property dnsStart Timestamp of the start of DNS resolution.
 * @property dnsEnd Timestamp of the end of DNS resolution.
 * @property connectStart Timestamp of the start of the TCP connection.
 * @property connectEnd Timestamp of the end of the TCP connection.
 * @property tlsStart Timestamp of the start of the TLS handshake.
 * @property tlsEnd Timestamp of the end of the TLS handshake.
 * @property requestStart Timestamp of the start of sending the request.
 * @property requestEnd Timestamp of the end of sending the request.
 * @property responseStart Timestamp of the start of receiving the response.
 * @property responseEnd Timestamp of the end of receiving the response.
 * @property error Error message if the transaction failed.
 * @property isFromCache Indicates whether the response came from the OkHttp cache.
 * @property state Current state of the transaction.
 *
 * @see TransactionState
 */
data class HttpTransaction(
    val id: Long,
    val sessionId: String,

    // Request
    val requestDate: Long?,
    val method: String?,
    val url: String?,
    val host: String?,
    val path: String?,
    val scheme: String?,
    val requestContentType: String?,
    val requestHeaders: List<Pair<String, String>>,
    val requestBody: String?,
    val requestBodySize: Long,

    // Response
    val responseDate: Long?,
    val responseCode: Int?,
    val responseMessage: String?,
    val responseContentType: String?,
    val responseHeaders: List<Pair<String, String>>,
    val responseBody: String?,
    val responseBodySize: Long,

    // Protocol
    val protocol: String?,
    val tlsVersion: String?,
    val cipherSuite: String?,

    // Timing
    val duration: Long?,
    val dnsStart: Long?,
    val dnsEnd: Long?,
    val connectStart: Long?,
    val connectEnd: Long?,
    val tlsStart: Long?,
    val tlsEnd: Long?,
    val requestStart: Long?,
    val requestEnd: Long?,
    val responseStart: Long?,
    val responseEnd: Long?,

    // Status
    val error: String?,
    val isFromCache: Boolean,
    val state: TransactionState,
)
