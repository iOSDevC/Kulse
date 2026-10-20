package com.iosdevc.android.logger

import com.iosdevc.android.logger.db.entity.HttpTransactionEntity
import com.iosdevc.android.logger.db.entity.TransactionCompletionUpdate
import com.iosdevc.android.logger.db.entity.TransactionResponseUpdate
import com.iosdevc.android.logger.internal.CapturingSource
import com.iosdevc.android.logger.internal.HeaderSerializer
import com.iosdevc.android.logger.internal.Redactor
import com.iosdevc.android.logger.model.TransactionState
import kotlinx.coroutines.Deferred
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.asResponseBody
import okio.Buffer
import okio.buffer
import java.io.IOException

internal class KulseInterceptor(
    private val logger: Kulse,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        if (logger.config.shouldExclude(request.url)) {
            return chain.proceed(request)
        }

        val requestDate = System.currentTimeMillis()
        val redactedHeaders = Redactor.redactHeaders(request.headers, logger.config.sensitiveHeaders)
        val requestBodyResult = readRequestBody(request)

        val url = request.url
        val requestEntity = HttpTransactionEntity(
            sessionId = logger.currentSessionId,
            requestDate = requestDate,
            method = request.method,
            url = if (logger.config.sensitiveQueryParams.isNotEmpty()) {
                Redactor.redactUrl(url, logger.config.sensitiveQueryParams)
            } else {
                url.toString()
            },
            host = url.host,
            path = url.encodedPath,
            scheme = url.scheme,
            requestContentType = request.body?.contentType()?.toString(),
            requestHeaders = HeaderSerializer.serialize(redactedHeaders),
            requestBody = redactBody(requestBodyResult?.first, request.body?.contentType()?.toString()),
            requestBodySize = requestBodyResult?.second ?: 0,
            isRequestBodyEncoded = isEncoded(request),
            state = 0,
        )

        // C1: Fire-and-forget async insert. Later writes for this transaction wait for its
        // id on Kulse's write queue, so nothing here blocks on the database.
        val transactionId = logger.insertTransaction(chain.call(), requestEntity)

        val startNanos = System.nanoTime()
        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: IOException) {
            complete(transactionId, startNanos, error = e)
            throw e
        }

        val responseRedactedHeaders = Redactor.redactHeaders(response.headers, logger.config.sensitiveHeaders)
        val responseUpdate = TransactionResponseUpdate(
            id = 0,
            responseDate = System.currentTimeMillis(),
            responseCode = response.code,
            responseMessage = response.message,
            responseContentType = response.body?.contentType()?.toString(),
            responseHeaders = HeaderSerializer.serialize(responseRedactedHeaders),
            isResponseBodyEncoded = isEncoded(response),
            protocol = response.protocol.toString(),
            // chain.connection() is always null in application interceptors.
            tlsVersion = response.handshake?.tlsVersion?.javaName,
            cipherSuite = response.handshake?.cipherSuite?.javaName,
            isFromCache = response.cacheResponse != null,
        )
        logger.updateTransaction(transactionId) { dao, id ->
            dao.updateResponse(responseUpdate.copy(id = id))
        }

        val body = response.body
        if (body == null || !hasBody(response)) {
            complete(transactionId, startNanos, error = null)
            return response
        }

        // C2: Capture the body while the app reads it instead of reading it up front, so
        // streaming responses are not held back. At most maxResponseBodySize bytes are kept.
        val maxSize = logger.config.maxResponseBodySize
        val contentType = body.contentType()
        val capturingSource = CapturingSource(body.source(), maxSize) { captured, totalBytes, error ->
            complete(transactionId, startNanos, error, totalBytes) {
                val text = if (totalBytes > maxSize) {
                    "[Body too large: $totalBytes bytes]"
                } else {
                    captured.readString(contentType?.charset(Charsets.UTF_8) ?: Charsets.UTF_8)
                }
                redactBody(text, contentType?.toString())
            }
        }
        return response.newBuilder()
            .body(capturingSource.buffer().asResponseBody(contentType, body.contentLength()))
            .build()
    }

    /**
     * Records the final state of the transaction. [readBody] runs on Kulse's write queue,
     * so decoding and redacting a large body never delays the thread reading the response.
     */
    private fun complete(
        transactionId: Deferred<Long>,
        startNanos: Long,
        error: IOException?,
        bodySize: Long = 0,
        readBody: () -> String? = { null },
    ) {
        val duration = (System.nanoTime() - startNanos) / 1_000_000
        logger.updateTransaction(transactionId) { dao, id ->
            dao.updateCompletion(
                TransactionCompletionUpdate(
                    id = id,
                    responseBody = readBody(),
                    responseBodySize = bodySize,
                    duration = duration,
                    error = error?.toString(),
                    state = if (error == null) TransactionState.COMPLETE.value else TransactionState.FAILED.value,
                )
            )
        }
    }

    // Same rules as OkHttp's internal promisesBody(): these responses never carry a body.
    private fun hasBody(response: Response): Boolean {
        if (response.request.method == "HEAD") return false
        val code = response.code
        if (code in 100..199 || code == 204 || code == 304) return false
        return response.body?.contentLength() != 0L
    }

    private fun readRequestBody(request: Request): Pair<String, Long>? {
        val body = request.body ?: return null
        val contentLength = body.contentLength()
        if (body.isOneShot() || body.isDuplex()) {
            // Reading these would consume the bytes the request still has to send.
            return "[Body not captured: one-shot or duplex request body]" to contentLength.coerceAtLeast(0)
        }
        if (contentLength > logger.config.maxRequestBodySize) {
            return "[Body too large: $contentLength bytes]" to contentLength
        }
        return try {
            val buffer = Buffer()
            body.writeTo(buffer)
            val size = buffer.size
            if (size > logger.config.maxRequestBodySize) {
                return "[Body too large: $size bytes]" to size
            }
            val charset = body.contentType()?.charset(Charsets.UTF_8) ?: Charsets.UTF_8
            buffer.readString(charset) to size
        } catch (_: Exception) {
            null
        }
    }

    private fun redactBody(body: String?, contentType: String?): String? {
        if (body == null) return null
        val fields = logger.config.sensitiveJsonFields
        if (fields.isEmpty()) return body
        if (contentType == null || !contentType.contains("json", ignoreCase = true)) return body
        return Redactor.redactJsonFields(body, fields)
    }

    private fun isEncoded(request: Request): Boolean {
        val encoding = request.header("Content-Encoding")
        return encoding != null && !encoding.equals("identity", ignoreCase = true)
    }

    private fun isEncoded(response: Response): Boolean {
        val encoding = response.header("Content-Encoding")
        return encoding != null && !encoding.equals("identity", ignoreCase = true)
    }
}
