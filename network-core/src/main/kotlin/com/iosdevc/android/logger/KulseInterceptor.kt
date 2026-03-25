package com.iosdevc.android.logger

import com.iosdevc.android.logger.db.entity.HttpTransactionEntity
import com.iosdevc.android.logger.internal.HeaderSerializer
import com.iosdevc.android.logger.internal.Redactor
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okio.Buffer
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

        // C1: Fire-and-forget async insert. The DB id is stored in callTransactionMap
        // when the insert completes, so EventListener can use it later.
        val call = chain.call()
        logger.insertTransactionAsync(requestEntity) { dbId ->
            logger.callTransactionMap[call] = dbId
        }

        val startNanos = System.nanoTime()
        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: IOException) {
            val duration = (System.nanoTime() - startNanos) / 1_000_000
            // Wait briefly for the async insert to provide the ID
            val transactionId = waitForTransactionId(call)
            if (transactionId != null) {
                logger.updateTransaction(
                    requestEntity.copy(
                        id = transactionId,
                        error = e.toString(),
                        duration = duration,
                        state = 2,
                    )
                )
            }
            // C5: Always clean up the map
            logger.callTransactionMap.remove(call)
            throw e
        }

        val duration = (System.nanoTime() - startNanos) / 1_000_000
        val responseRedactedHeaders = Redactor.redactHeaders(response.headers, logger.config.sensitiveHeaders)
        val responseBodyResult = readResponseBody(response)

        val transactionId = waitForTransactionId(call)
        if (transactionId != null) {
            logger.updateTransaction(
                requestEntity.copy(
                    id = transactionId,
                    responseDate = System.currentTimeMillis(),
                    responseCode = response.code,
                    responseMessage = response.message,
                    responseContentType = response.body?.contentType()?.toString(),
                    responseHeaders = HeaderSerializer.serialize(responseRedactedHeaders),
                    responseBody = redactBody(responseBodyResult?.first, response.body?.contentType()?.toString()),
                    responseBodySize = responseBodyResult?.second ?: 0,
                    isResponseBodyEncoded = isEncoded(response),
                    protocol = response.protocol.toString(),
                    tlsVersion = chain.connection()?.handshake()?.tlsVersion?.javaName,
                    cipherSuite = chain.connection()?.handshake()?.cipherSuite?.javaName,
                    duration = duration,
                    isFromCache = response.cacheResponse != null,
                    state = 1,
                )
            )
        }

        // C5: Clean up on success path -- EventListener can still read it
        // before this runs since flushTimings happens on callEnd which is after intercept returns.
        // We leave the entry for the EventListener to consume; it will remove it.
        // If no EventListener is installed, clean up here.
        if (!logger.hasEventListenerFactory) {
            logger.callTransactionMap.remove(call)
        }

        return response
    }

    /**
     * Wait for the async insert to complete and provide the transaction ID.
     * Spins briefly since the insert is on Dispatchers.IO and is very fast.
     */
    private fun waitForTransactionId(call: Any): Long? {
        // The insert is async on IO dispatcher. In practice, the network call takes
        // much longer than a Room insert, so by the time we get here the ID is ready.
        var attempts = 0
        while (attempts < 50) { // max ~50ms wait
            val id = logger.callTransactionMap[call]
            if (id != null) return id
            Thread.sleep(1)
            attempts++
        }
        return null
    }

    private fun readRequestBody(request: Request): Pair<String, Long>? {
        val body = request.body ?: return null
        val contentLength = body.contentLength()
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

    // C2: Only buffer up to maxResponseBodySize + 1 to prevent OOM
    private fun readResponseBody(response: Response): Pair<String, Long>? {
        val body = response.body ?: return null
        val maxSize = logger.config.maxResponseBodySize
        return try {
            val source = body.source()
            source.request(maxSize + 1)
            val buffer = source.buffer.clone()
            val size = buffer.size
            if (size > maxSize) {
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
