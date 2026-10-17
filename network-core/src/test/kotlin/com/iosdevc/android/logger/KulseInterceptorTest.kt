package com.iosdevc.android.logger

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.iosdevc.android.logger.db.KulseDatabase
import com.iosdevc.android.logger.db.entity.HttpTransactionEntity
import com.iosdevc.android.logger.model.TransactionState
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.ResponseBody.Companion.asResponseBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.BufferedSink
import okio.Pipe
import okio.buffer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
class KulseInterceptorTest {

    private lateinit var database: KulseDatabase
    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            KulseDatabase::class.java,
        ).allowMainThreadQueries().build()
        Kulse.installForTesting(database)
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        runBlocking { Kulse.awaitWrites() }
        server.shutdown()
        database.close()
    }

    /** Kulse first, then [interceptors] standing in for the network when needed. */
    private fun client(vararg interceptors: Interceptor): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(Kulse.interceptor())
            .apply { interceptors.forEach { addInterceptor(it) } }
            .eventListenerFactory(Kulse.eventListenerFactory())
            .build()

    private fun get(path: String) = Request.Builder().url(server.url(path)).build()

    private fun storedTransactions(): List<HttpTransactionEntity> = runBlocking {
        Kulse.awaitWrites()
        database.transactionDao().getAll()
    }

    @Test
    fun storesTheResponseAndTheTimingsOfEveryCall() {
        val client = client()
        repeat(30) { i ->
            server.enqueue(MockResponse().setBody("response $i"))
            client.newCall(get("/items/$i")).execute().use {
                assertEquals("response $i", it.body!!.string())
            }
        }

        val transactions = storedTransactions()
        assertEquals(30, transactions.size)
        for (tx in transactions) {
            assertEquals(TransactionState.COMPLETE.value, tx.state)
            assertEquals(200, tx.responseCode)
            assertEquals("response ${tx.path!!.substringAfterLast('/')}", tx.responseBody)
            assertNotNull(tx.duration)
            assertNotNull(tx.requestStart)
            assertNotNull(tx.responseEnd)
        }
    }

    @Test(timeout = 10_000)
    fun deliversStreamingResponsesBeforeTheBodyEnds() {
        val pipe = Pipe(maxBufferSize = 1024 * 1024)
        val upstream = pipe.sink.buffer()
        val eventStream = Interceptor { chain ->
            Response.Builder()
                .request(chain.request())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(pipe.source.buffer().asResponseBody("text/event-stream".toMediaType()))
                .build()
        }
        upstream.writeUtf8("data: first\n\n").flush()

        // The interceptor used to read the whole body before returning, so this never returned.
        val response = client(eventStream).newCall(get("/events")).execute()
        val body = response.body!!.source()
        assertEquals("data: first", body.readUtf8Line())

        upstream.writeUtf8("data: second\n\n").close()
        assertEquals("\ndata: second\n\n", body.readUtf8())
        response.close()

        val tx = storedTransactions().single()
        assertEquals(TransactionState.COMPLETE.value, tx.state)
        assertEquals("data: first\n\ndata: second\n\n", tx.responseBody)
        assertEquals(27, tx.responseBodySize)
    }

    @Test
    fun leavesOneShotRequestBodiesForTheRequest() {
        server.enqueue(MockResponse().setBody("ok"))
        val oneShotBody = object : RequestBody() {
            private var written = false
            override fun contentType() = "text/plain".toMediaType()
            override fun isOneShot() = true
            override fun writeTo(sink: BufferedSink) {
                check(!written) { "One-shot body written twice" }
                written = true
                sink.writeUtf8("payload")
            }
        }

        val request = Request.Builder().url(server.url("/upload")).post(oneShotBody).build()
        client().newCall(request).execute().use { assertEquals(200, it.code) }

        assertEquals("payload", server.takeRequest().body.readUtf8())
        val tx = storedTransactions().single()
        assertEquals("[Body not captured: one-shot or duplex request body]", tx.requestBody)
        assertEquals(TransactionState.COMPLETE.value, tx.state)
    }

    @Test
    fun recordsTheResponseEvenWhenTheDatabaseIsBusy() {
        // Hold the write queue so the insert is still pending when the response arrives.
        // The interceptor used to give up on the id after 50 ms and drop the response.
        val databaseBusy = CompletableDeferred<Unit>()
        Kulse.enqueueWrite { databaseBusy.await() }
        server.enqueue(MockResponse().setBody("hello"))

        try {
            client().newCall(get("/slow")).execute().use {
                assertEquals("hello", it.body!!.string())
            }
        } finally {
            databaseBusy.complete(Unit)
        }

        val tx = storedTransactions().single()
        assertEquals(TransactionState.COMPLETE.value, tx.state)
        assertEquals(200, tx.responseCode)
        assertEquals("hello", tx.responseBody)
    }

    @Test
    fun marksFailedCalls() {
        val unreachable = Interceptor { throw IOException("connection reset") }

        assertThrows(IOException::class.java) {
            client(unreachable).newCall(get("/fail")).execute()
        }

        val tx = storedTransactions().single()
        assertEquals(TransactionState.FAILED.value, tx.state)
        assertEquals("java.io.IOException: connection reset", tx.error)
        assertNotNull(tx.duration)
    }

    @Test
    fun completesResponsesWithoutABodyEvenIfTheyAreNotRead() {
        server.enqueue(MockResponse().setResponseCode(204))

        val response = client().newCall(get("/empty")).execute()

        val tx = storedTransactions().single()
        assertEquals(TransactionState.COMPLETE.value, tx.state)
        assertEquals(204, tx.responseCode)
        response.close()
    }

    @Test
    fun storesAPlaceholderForBodiesOverTheLimitButPassesThemThrough() {
        Kulse.installForTesting(database, KulseConfig(maxResponseBodySize = 10))
        val body = "x".repeat(100)
        server.enqueue(MockResponse().setBody(body))

        client().newCall(get("/large")).execute().use {
            assertEquals(body, it.body!!.string())
        }

        val tx = storedTransactions().single()
        assertEquals("[Body too large: 100 bytes]", tx.responseBody)
        assertEquals(100, tx.responseBodySize)
    }
}
