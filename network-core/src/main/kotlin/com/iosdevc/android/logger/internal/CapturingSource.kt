package com.iosdevc.android.logger.internal

import okio.Buffer
import okio.ForwardingSource
import okio.Source
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Hands a response body to the app untouched while keeping a copy of its first [maxBytes]
 * bytes. Nothing is read ahead, so streaming responses reach the app as they arrive.
 *
 * [onComplete] runs once, when the body is exhausted, closed or fails, with the captured
 * bytes, the total number of bytes the app read, and the read error if there was one.
 */
internal class CapturingSource(
    delegate: Source,
    private val maxBytes: Long,
    private val onComplete: (captured: Buffer, totalBytes: Long, error: IOException?) -> Unit,
) : ForwardingSource(delegate) {

    private val captured = Buffer()
    private var totalBytes = 0L
    private val completed = AtomicBoolean(false)

    override fun read(sink: Buffer, byteCount: Long): Long {
        val read = try {
            super.read(sink, byteCount)
        } catch (e: IOException) {
            complete(e)
            throw e
        }
        if (read == -1L) {
            complete(null)
            return -1L
        }
        val room = maxBytes - captured.size
        if (room > 0 && !completed.get()) {
            sink.copyTo(captured, sink.size - read, minOf(read, room))
        }
        totalBytes += read
        return read
    }

    override fun close() {
        try {
            super.close()
        } finally {
            complete(null)
        }
    }

    private fun complete(error: IOException?) {
        if (completed.compareAndSet(false, true)) {
            onComplete(captured, totalBytes, error)
        }
    }
}
