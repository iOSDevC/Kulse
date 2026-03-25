package com.iosdevc.android.logger.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.iosdevc.android.logger.model.HttpTransaction
import com.iosdevc.android.logger.repository.KulseRepository
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.File

/**
 * Exporter for HTTP transactions recorded by Kulse.
 *
 * Allows obtaining logs as JSON, saving them to a temporary file,
 * or sharing them via an Android Intent.
 *
 * @param repository Repository from which transactions are obtained.
 *
 * @see KulseRepository
 */
class LogExporter(private val repository: KulseRepository) {

    /**
     * Exports all stored transactions as a formatted JSON string.
     *
     * @return String with the JSON array of transactions in pretty-print format.
     */
    suspend fun exportAsJson(): String {
        val transactions = repository.getAllTransactions()
        val jsonArray = JsonArray(transactions.map { it.toJsonObject() })
        return Json { prettyPrint = true }.encodeToString(JsonArray.serializer(), jsonArray)
    }

    /**
     * Exports transactions to a temporary JSON file and returns its URI via FileProvider.
     *
     * The file is created in the application's cache directory under `kulse/`.
     *
     * @param context Context needed to access the cache and the FileProvider.
     * @return [Uri] of the created file, ready to share.
     * @see shareIntent
     */
    suspend fun exportToFile(context: Context): Uri {
        val json = exportAsJson()
        val dir = File(context.cacheDir, "kulse")
        dir.mkdirs()
        val file = File(dir, "kulse_logs_${System.currentTimeMillis()}.json")
        file.writeText(json)
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.kulse.fileprovider",
            file,
        )
    }

    /**
     * Creates an [Intent] ACTION_SEND to share the exported log file.
     *
     * @param uri File URI obtained from [exportToFile].
     * @return Intent configured with type `application/json` and read permissions.
     */
    fun shareIntent(uri: Uri): Intent {
        return Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    companion object {

        /**
         * Generates a cURL command equivalent to the given HTTP transaction.
         */
        fun generateCurl(tx: HttpTransaction): String {
            val parts = mutableListOf("curl")
            parts.add("-X ${tx.method}")
            for ((name, value) in tx.requestHeaders) {
                parts.add("-H '${name}: ${value}'")
            }
            tx.requestBody?.let { body ->
                if (!body.startsWith("[Body too large")) {
                    parts.add("-d '${body.replace("'", "'\\''")}'")
                }
            }
            parts.add("'${tx.url}'")
            return parts.joinToString(" \\\n  ")
        }

        /**
         * Formats a single transaction as plain text.
         */
        fun formatAsPlainText(tx: HttpTransaction): String = buildString {
            appendLine("=== REQUEST ===")
            appendLine("${tx.method} ${tx.url}")
            appendLine("Date: ${formatTimestamp(tx.requestDate)}")
            if (tx.requestHeaders.isNotEmpty()) {
                appendLine()
                appendLine("Headers:")
                for ((name, value) in tx.requestHeaders) {
                    appendLine("  $name: $value")
                }
            }
            if (!tx.requestBody.isNullOrBlank() && !tx.requestBody.startsWith("[Body too large")) {
                appendLine()
                appendLine("Body:")
                appendLine(tx.requestBody)
            }

            appendLine()
            appendLine("=== RESPONSE ===")
            appendLine("Status: ${tx.responseCode ?: "—"} ${tx.responseMessage ?: ""}")
            appendLine("Duration: ${tx.duration ?: "—"} ms")
            appendLine("Protocol: ${tx.protocol ?: "—"}")
            if (tx.responseHeaders.isNotEmpty()) {
                appendLine()
                appendLine("Headers:")
                for ((name, value) in tx.responseHeaders) {
                    appendLine("  $name: $value")
                }
            }
            if (!tx.responseBody.isNullOrBlank() && !tx.responseBody.startsWith("[Body too large")) {
                appendLine()
                appendLine("Body:")
                appendLine(tx.responseBody)
            }

            if (tx.error != null) {
                appendLine()
                appendLine("=== ERROR ===")
                appendLine(tx.error)
            }
        }

        /**
         * Formats a single transaction as Markdown.
         */
        fun formatAsMarkdown(tx: HttpTransaction): String = buildString {
            appendLine("# ${tx.method} ${tx.path}")
            appendLine()
            appendLine("## Request")
            appendLine()
            appendLine("| Field | Value |")
            appendLine("|-------|-------|")
            appendLine("| URL | `${tx.url}` |")
            appendLine("| Method | ${tx.method} |")
            appendLine("| Host | ${tx.host} |")
            appendLine("| Date | ${formatTimestamp(tx.requestDate)} |")

            if (tx.requestHeaders.isNotEmpty()) {
                appendLine()
                appendLine("### Request Headers")
                appendLine()
                appendLine("| Header | Value |")
                appendLine("|--------|-------|")
                for ((name, value) in tx.requestHeaders) {
                    appendLine("| $name | `$value` |")
                }
            }

            if (!tx.requestBody.isNullOrBlank() && !tx.requestBody.startsWith("[Body too large")) {
                appendLine()
                appendLine("### Request Body")
                appendLine()
                val lang = if (tx.requestContentType?.contains("json") == true) "json" else ""
                appendLine("```$lang")
                appendLine(tx.requestBody)
                appendLine("```")
            }

            appendLine()
            appendLine("## Response")
            appendLine()
            appendLine("| Field | Value |")
            appendLine("|-------|-------|")
            appendLine("| Status | ${tx.responseCode ?: "—"} ${tx.responseMessage ?: ""} |")
            appendLine("| Duration | ${tx.duration ?: "—"} ms |")
            appendLine("| Size | ${formatBytes(tx.responseBodySize)} |")
            appendLine("| Protocol | ${tx.protocol ?: "—"} |")

            if (tx.responseHeaders.isNotEmpty()) {
                appendLine()
                appendLine("### Response Headers")
                appendLine()
                appendLine("| Header | Value |")
                appendLine("|--------|-------|")
                for ((name, value) in tx.responseHeaders) {
                    appendLine("| $name | `$value` |")
                }
            }

            if (!tx.responseBody.isNullOrBlank() && !tx.responseBody.startsWith("[Body too large")) {
                appendLine()
                appendLine("### Response Body")
                appendLine()
                val lang = if (tx.responseContentType?.contains("json") == true) "json" else ""
                appendLine("```$lang")
                appendLine(tx.responseBody)
                appendLine("```")
            }

            if (tx.error != null) {
                appendLine()
                appendLine("## Error")
                appendLine()
                appendLine("```")
                appendLine(tx.error)
                appendLine("```")
            }
        }

        /**
         * Formats a single transaction as an HTML document.
         */
        fun formatAsHtml(tx: HttpTransaction): String = buildString {
            appendLine("<!DOCTYPE html>")
            appendLine("<html><head><meta charset=\"utf-8\">")
            appendLine("<title>${tx.method} ${tx.path}</title>")
            appendLine("<style>")
            appendLine("body{font-family:monospace;background:#1a1a2e;color:#e0e0e0;padding:20px;}")
            appendLine("h1,h2,h3{color:#7eb8da;} table{border-collapse:collapse;width:100%;margin:10px 0;}")
            appendLine("td,th{border:1px solid #333;padding:6px 10px;text-align:left;}")
            appendLine("th{background:#252545;} pre{background:#0d0d1a;padding:12px;border-radius:6px;overflow-x:auto;}")
            appendLine(".badge{display:inline-block;padding:2px 8px;border-radius:4px;font-weight:bold;}")
            appendLine(".s2xx{background:#1b5e20;color:#a5d6a7;} .s3xx{background:#0d47a1;color:#90caf9;}")
            appendLine(".s4xx{background:#e65100;color:#ffcc80;} .s5xx{background:#b71c1c;color:#ef9a9a;}")
            appendLine(".error{color:#ef5350;}")
            appendLine("</style></head><body>")
            appendLine("<h1>${tx.method} ${escapeHtml(tx.path ?: "")}</h1>")

            // Request section
            appendLine("<h2>Request</h2>")
            appendLine("<table><tr><th>Field</th><th>Value</th></tr>")
            appendLine("<tr><td>URL</td><td>${escapeHtml(tx.url ?: "")}</td></tr>")
            appendLine("<tr><td>Method</td><td>${tx.method}</td></tr>")
            appendLine("<tr><td>Host</td><td>${tx.host}</td></tr>")
            appendLine("<tr><td>Date</td><td>${formatTimestamp(tx.requestDate)}</td></tr>")
            appendLine("</table>")

            if (tx.requestHeaders.isNotEmpty()) {
                appendLine("<h3>Request Headers</h3><table><tr><th>Header</th><th>Value</th></tr>")
                for ((name, value) in tx.requestHeaders) {
                    appendLine("<tr><td>${escapeHtml(name)}</td><td>${escapeHtml(value)}</td></tr>")
                }
                appendLine("</table>")
            }

            if (!tx.requestBody.isNullOrBlank() && !tx.requestBody.startsWith("[Body too large")) {
                appendLine("<h3>Request Body</h3><pre>${escapeHtml(tx.requestBody)}</pre>")
            }

            // Response section
            val statusClass = when {
                tx.responseCode == null -> ""
                tx.responseCode in 200..299 -> "s2xx"
                tx.responseCode in 300..399 -> "s3xx"
                tx.responseCode in 400..499 -> "s4xx"
                else -> "s5xx"
            }
            appendLine("<h2>Response</h2>")
            appendLine("<table><tr><th>Field</th><th>Value</th></tr>")
            appendLine("<tr><td>Status</td><td><span class=\"badge $statusClass\">${tx.responseCode ?: "—"}</span> ${tx.responseMessage ?: ""}</td></tr>")
            appendLine("<tr><td>Duration</td><td>${tx.duration ?: "—"} ms</td></tr>")
            appendLine("<tr><td>Size</td><td>${formatBytes(tx.responseBodySize)}</td></tr>")
            appendLine("<tr><td>Protocol</td><td>${tx.protocol ?: "—"}</td></tr>")
            appendLine("</table>")

            if (tx.responseHeaders.isNotEmpty()) {
                appendLine("<h3>Response Headers</h3><table><tr><th>Header</th><th>Value</th></tr>")
                for ((name, value) in tx.responseHeaders) {
                    appendLine("<tr><td>${escapeHtml(name)}</td><td>${escapeHtml(value)}</td></tr>")
                }
                appendLine("</table>")
            }

            if (!tx.responseBody.isNullOrBlank() && !tx.responseBody.startsWith("[Body too large")) {
                appendLine("<h3>Response Body</h3><pre>${escapeHtml(tx.responseBody)}</pre>")
            }

            if (tx.error != null) {
                appendLine("<h2 class=\"error\">Error</h2><pre class=\"error\">${escapeHtml(tx.error)}</pre>")
            }

            appendLine("</body></html>")
        }

        private fun escapeHtml(text: String): String = text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")

        private fun formatTimestamp(millis: Long?): String {
            if (millis == null) return "—"
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", java.util.Locale.US)
            return sdf.format(java.util.Date(millis))
        }

        private fun formatBytes(bytes: Long): String = when {
            bytes <= 0 -> "—"
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "%.1f KB".format(bytes / 1024.0)
            else -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
        }
    }

    private fun HttpTransaction.toJsonObject(): JsonObject = buildJsonObject {
        put("id", id)
        put("sessionId", sessionId)
        put("requestDate", requestDate)
        put("method", method)
        put("url", url)
        put("host", host)
        put("path", path)
        put("scheme", scheme)
        put("requestContentType", requestContentType)
        put("requestBody", requestBody)
        put("requestBodySize", requestBodySize)
        put("responseDate", responseDate)
        put("responseCode", responseCode)
        put("responseMessage", responseMessage)
        put("responseContentType", responseContentType)
        put("responseBody", responseBody)
        put("responseBodySize", responseBodySize)
        put("protocol", protocol)
        put("tlsVersion", tlsVersion)
        put("cipherSuite", cipherSuite)
        put("duration", duration)
        put("error", error)
        put("isFromCache", isFromCache)
        put("state", state.name)
    }
}
