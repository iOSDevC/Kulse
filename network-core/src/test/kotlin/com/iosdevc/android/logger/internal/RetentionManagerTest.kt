package com.iosdevc.android.logger.internal

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.iosdevc.android.logger.KulseConfig
import com.iosdevc.android.logger.db.KulseDatabase
import com.iosdevc.android.logger.db.entity.HttpTransactionEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.time.Duration.Companion.days

@RunWith(RobolectricTestRunner::class)
class RetentionManagerTest {

    private lateinit var database: KulseDatabase

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            KulseDatabase::class.java,
        ).allowMainThreadQueries().build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun transaction(requestDate: Long, body: String = "") = HttpTransactionEntity(
        sessionId = "session",
        requestDate = requestDate,
        responseBody = body,
        responseBodySize = body.length.toLong(),
    )

    @Test
    fun deletesTransactionsOlderThanMaxAge(): Unit = runBlocking {
        val dao = database.transactionDao()
        val now = System.currentTimeMillis()
        dao.insert(transaction(now - 15.days.inWholeMilliseconds))
        val recentId = dao.insert(transaction(now))

        RetentionManager.sweep(database, KulseConfig(maxAge = 14.days))

        assertEquals(listOf(recentId), dao.getAll().map { it.id })
    }

    @Test
    fun deletesTheOldestTransactionsUntilTheDatabaseFits(): Unit = runBlocking {
        // Every row is recent. The old size check only deleted rows older than maxAge / 3,
        // so nothing was deleted and the database kept growing.
        val dao = database.transactionDao()
        val now = System.currentTimeMillis()
        val body = "x".repeat(50 * 1024)
        val ids = (1..40).map { dao.insert(transaction(now, body)) }
        val maxStorageSize = 1024L * 1024
        assertTrue(RetentionManager.usedBytes(database) > maxStorageSize)

        RetentionManager.sweep(database, KulseConfig(maxStorageSize = maxStorageSize))

        assertTrue(RetentionManager.usedBytes(database) <= maxStorageSize)
        val remaining = dao.getAll().map { it.id }.sorted()
        assertTrue(remaining.isNotEmpty())
        assertEquals(ids.takeLast(remaining.size), remaining)
    }
}
