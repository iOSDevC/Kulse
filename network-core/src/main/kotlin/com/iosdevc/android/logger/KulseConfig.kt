package com.iosdevc.android.logger

import okhttp3.HttpUrl
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

/**
 * Configuration for the Kulse library.
 *
 * Controls storage limits, sensitive data redaction,
 * host filtering, and log level. All parameters have reasonable
 * default values for most applications.
 *
 * @property maxStorageSize Maximum size in bytes the database can occupy before
 *   the retention mechanism deletes old records. Defaults to 256 MB.
 * @property maxAge Maximum duration that transactions are kept. Defaults to 14 days.
 * @property sweepInterval Interval between retention cleaner runs. Defaults to 1 hour.
 * @property maxRequestBodySize Maximum size in bytes of the request body that is stored.
 *   Larger bodies are truncated. Defaults to 1 MB.
 * @property maxResponseBodySize Maximum size in bytes of the response body that is stored.
 *   Larger bodies are truncated. Defaults to 8 MB.
 * @property sensitiveHeaders Names of headers whose values are redacted in the log.
 *   Defaults include Authorization, Cookie, Set-Cookie, X-Api-Key, and X-Auth-Token.
 * @property sensitiveQueryParams Names of query parameters that are redacted.
 * @property sensitiveJsonFields Names of JSON fields that are redacted in bodies.
 * @property excludedHosts Hosts that are never logged. Compared by suffix (case-insensitive).
 * @property includedHosts If not empty, only matching hosts are logged.
 *   Compared by suffix (case-insensitive).
 * @property isEnabled If `false`, the interceptor does not log any transactions.
 * @property logLevel Minimum log level for custom messages.
 *
 * @see Kulse.install
 */
data class KulseConfig(
    val maxStorageSize: Long = 256L * 1024 * 1024,
    val maxAge: Duration = 14.days,
    val sweepInterval: Duration = 1.hours,

    val maxRequestBodySize: Long = 1L * 1024 * 1024,
    val maxResponseBodySize: Long = 8L * 1024 * 1024,

    val sensitiveHeaders: Set<String> = setOf(
        "Authorization", "Cookie", "Set-Cookie",
        "X-Api-Key", "X-Auth-Token",
    ),
    val sensitiveQueryParams: Set<String> = emptySet(),
    val sensitiveJsonFields: Set<String> = emptySet(),

    val excludedHosts: Set<String> = emptySet(),
    val includedHosts: Set<String> = emptySet(),

    val isEnabled: Boolean = true,
    val logLevel: LogLevel = LogLevel.DEBUG,
) {
    /**
     * Determines whether a URL should be excluded from logging.
     *
     * The URL is excluded if:
     * - [isEnabled] is `false`.
     * - The host matches any entry in [excludedHosts].
     * - [includedHosts] is not empty and the host does not match any entry.
     *
     * @param url URL of the HTTP request to evaluate.
     * @return `true` if the transaction should be ignored by the interceptor.
     */
    fun shouldExclude(url: HttpUrl): Boolean {
        if (!isEnabled) return true
        val host = url.host
        if (excludedHosts.any { host.endsWith(it, ignoreCase = true) }) return true
        if (includedHosts.isNotEmpty() && includedHosts.none { host.endsWith(it, ignoreCase = true) }) return true
        return false
    }
}
