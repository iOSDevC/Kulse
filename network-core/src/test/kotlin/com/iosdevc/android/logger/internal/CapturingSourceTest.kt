package com.iosdevc.android.logger.internal

import okio.Buffer
import okio.Source
import okio.Timeout
import okio.buffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.IOException

class CapturingSourceTest {

    private class Capture(val body: String, val totalBytes: Long, val error: IOException?)

    private val captures = mutableListOf<Capture>()

    private fun capturing(source: Source, maxBytes: Long) =
        CapturingSource(source, maxBytes) { captured, totalBytes, error ->
            captures += Capture(captured.readUtf8(), totalBytes, error)
        }

    @Test
    fun passesTheBodyThroughAndCapturesItAtTheEnd() {
        val source = capturing(Buffer().writeUtf8("hello world"), maxBytes = 100).buffer()

        assertEquals("hello world", source.readUtf8())

        val capture = captures.single()
        assertEquals("hello world", capture.body)
        assertEquals(11, capture.totalBytes)
        assertNull(capture.error)
    }

    @Test
    fun keepsOnlyMaxBytesButCountsTheWholeBody() {
        val body = "x".repeat(10_000)
        val source = capturing(Buffer().writeUtf8(body), maxBytes = 10).buffer()

        assertEquals(body, source.readUtf8())

        val capture = captures.single()
        assertEquals("x".repeat(10), capture.body)
        assertEquals(10_000, capture.totalBytes)
    }

    @Test
    fun reportsReadFailures() {
        val failing = object : Source {
            override fun read(sink: Buffer, byteCount: Long): Long = throw IOException("connection reset")
            override fun timeout(): Timeout = Timeout.NONE
            override fun close() = Unit
        }
        val source = capturing(failing, maxBytes = 100).buffer()

        assertThrows(IOException::class.java) { source.readUtf8() }

        assertEquals("connection reset", captures.single().error?.message)
    }

    @Test
    fun completesOnceWhenClosedBeforeTheEnd() {
        val source = capturing(Buffer().writeUtf8("0123456789abcdef"), maxBytes = 100)

        source.read(Buffer(), 10)
        source.close()
        source.close()

        val capture = captures.single()
        assertEquals("0123456789", capture.body)
        assertEquals(10, capture.totalBytes)
        assertNull(capture.error)
    }
}
