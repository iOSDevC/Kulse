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
         *
         * Useful for reproducing requests outside of the application.
         *
         * @param transaction HTTP transaction from which to generate the command.
         * @return String with the cURL command formatted with line breaks.
         */
        fun generateCurl(transaction: HttpTransaction): String {
            val parts = mutableListOf("curl")
            parts.add("-X ${transaction.method}")

            for ((name, value) in transaction.requestHeaders) {
                parts.add("-H '${name}: ${value}'")
            }

            transaction.requestBody?.let { body ->
                if (!body.startsWith("[Body too large")) {
                    parts.add("-d '${body.replace("'", "'\\''")}'")
                }
            }

            parts.add("'${transaction.url}'")
            return parts.joinToString(" \\\n  ")
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
