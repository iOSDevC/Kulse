package com.iosdevc.android.logger

import android.content.Context
import android.os.Build
import android.util.Log
import com.iosdevc.android.logger.db.KulseDatabase
import com.iosdevc.android.logger.db.entity.HttpTransactionEntity
import com.iosdevc.android.logger.db.entity.LogMessageEntity
import com.iosdevc.android.logger.db.entity.SessionEntity
import com.iosdevc.android.logger.internal.RetentionManager
import com.iosdevc.android.logger.model.HttpTransaction
import com.iosdevc.android.logger.model.Session
import com.iosdevc.android.logger.repository.KulseRepository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import okhttp3.Interceptor
import okhttp3.EventListener
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Main entry point of the Kulse library for network inspection on Android.
 *
 * Kulse intercepts and records HTTP transactions made with OkHttp,
 * storing them in a local database for later querying,
 * exporting, or viewing in the built-in UI.
 *
 * Must be initialized once via [install] before using any
 * other functionality.
 *
 * @see KulseConfig
 * @see KulseActivity
 */
object Kulse {

    private val TAG = "Kulse"

    private var _database: KulseDatabase? = null
    private var _config: KulseConfig = KulseConfig()
    private var _currentSessionId: String = UUID.randomUUID().toString()
    private val _initialized = AtomicBoolean(false)

    // C4: CoroutineExceptionHandler to prevent silent crashes
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, "Coroutine error", throwable)
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO + exceptionHandler)

    private val _events = MutableSharedFlow<Event>(extraBufferCapacity = 64)
    /**
     * Flow of events emitted when transactions are inserted or updated.
     *
     * Allows observing changes in real time from the UI layer or other consumers.
     *
     * @see Event
     */
    val events: SharedFlow<Event> = _events.asSharedFlow()

    internal val callTransactionMap = ConcurrentHashMap<Any, Long>()

    /** Active configuration. Reflects what was provided in [install]. */
    val config: KulseConfig get() = _config

    /** Unique identifier of the current session, generated when [install] is called. */
    val currentSessionId: String get() = _currentSessionId

    // C6: Internal-only database access
    internal val database: KulseDatabase
        get() = _database ?: error("Kulse not initialized. Call Kulse.install(context) first.")

    // C6: Public repository facade
    private var _repository: KulseRepository? = null

    /**
     * Repository for querying stored transactions and sessions.
     *
     * @throws IllegalStateException if accessed before calling [install].
     */
    val repository: KulseRepository
        get() = _repository ?: error("Kulse not initialized. Call Kulse.install(context) first.")

    /**
     * Initializes the Kulse library. Must be called once, ideally in
     * `Application.onCreate()`. Subsequent calls are ignored with a warning.
     *
     * Usage example:
     * ```kotlin
     * // In your Application class
     * class MyApp : Application() {
     *     override fun onCreate() {
     *         super.onCreate()
     *         Kulse.install(
     *             context = this,
     *             config = KulseConfig(
     *                 maxAge = 7.days,
     *                 sensitiveHeaders = setOf("Authorization", "X-Custom-Token"),
     *             ),
     *         )
     *     }
     * }
     * ```
     *
     * @param context Application context. Uses `applicationContext` internally.
     * @param config Optional configuration. If not provided, default values are used.
     * @see KulseConfig
     */
    fun install(context: Context, config: KulseConfig = KulseConfig()) {
        if (!_initialized.compareAndSet(false, true)) {
            Log.w(TAG, "Kulse.install() called more than once. Ignoring.")
            return
        }
        _config = config
        _database = KulseDatabase.getInstance(context)
        _repository = KulseRepository(_database!!)
        startNewSession(context)
        scheduleRetention()
    }

    internal var hasEventListenerFactory: Boolean = false
        private set

    /**
     * Creates an OkHttp [Interceptor] that records all HTTP transactions.
     *
     * Should be added as an application interceptor to capture complete requests and responses,
     * including headers and bodies.
     *
     * Usage example:
     * ```kotlin
     * val client = OkHttpClient.Builder()
     *     .addInterceptor(Kulse.interceptor())
     *     .eventListenerFactory(Kulse.eventListenerFactory()) // optional, for timing metrics
     *     .build()
     * ```
     *
     * @return [Interceptor] instance ready to add to the OkHttp client.
     * @see eventListenerFactory
     */
    fun interceptor(): Interceptor = KulseInterceptor(this)

    /**
     * Creates an [EventListener.Factory] that captures detailed timing metrics
     * for each HTTP call: DNS resolution, TCP connection, TLS handshake,
     * request sending, and response receiving.
     *
     * Optional but recommended for obtaining complete performance information.
     *
     * @return [EventListener] factory to assign to the [OkHttpClient.Builder].
     * @see interceptor
     */
    fun eventListenerFactory(): EventListener.Factory {
        hasEventListenerFactory = true
        return KulseEventListener.Factory(this)
    }

    /**
     * Records a custom log message associated with the current session.
     *
     * Messages are persisted in the database and are visible from the Kulse UI
     * alongside HTTP transactions.
     *
     * @param level Severity level of the message. Defaults to [LogLevel.DEBUG].
     * @param tag Optional tag to categorize the message.
     * @param message Text of the message to record.
     */
    fun log(
        level: LogLevel = LogLevel.DEBUG,
        tag: String? = null,
        message: String,
    ) {
        scope.launch {
            database.logMessageDao().insert(
                LogMessageEntity(
                    sessionId = _currentSessionId,
                    timestamp = System.currentTimeMillis(),
                    level = level.value,
                    message = message,
                    tag = tag,
                )
            )
        }
    }

    // C1: Async insert -- no runBlocking on the network thread
    internal fun insertTransactionAsync(
        entity: HttpTransactionEntity,
        onInserted: (Long) -> Unit,
    ) {
        scope.launch {
            val id = database.transactionDao().insert(entity)
            _events.tryEmit(Event.TransactionInserted(id))
            onInserted(id)
        }
    }

    internal fun updateTransaction(entity: HttpTransactionEntity) {
        scope.launch {
            database.transactionDao().update(entity)
            _events.tryEmit(Event.TransactionUpdated(entity.id))
        }
    }

    internal fun updateTransactionTimings(
        transactionId: Long,
        dnsStart: Long?,
        dnsEnd: Long?,
        connectStart: Long?,
        connectEnd: Long?,
        tlsStart: Long?,
        tlsEnd: Long?,
        requestStart: Long?,
        requestEnd: Long?,
        responseStart: Long?,
        responseEnd: Long?,
    ) {
        scope.launch {
            val tx = database.transactionDao().getById(transactionId) ?: return@launch
            database.transactionDao().update(
                tx.copy(
                    dnsStart = dnsStart,
                    dnsEnd = dnsEnd,
                    connectStart = connectStart,
                    connectEnd = connectEnd,
                    tlsStart = tlsStart,
                    tlsEnd = tlsEnd,
                    requestStart = requestStart,
                    requestEnd = requestEnd,
                    responseStart = responseStart,
                    responseEnd = responseEnd,
                )
            )
        }
    }

    /**
     * Deletes all stored HTTP transactions and log messages.
     *
     * The operation runs asynchronously. Sessions are not deleted.
     */
    fun clearAll() {
        scope.launch {
            database.transactionDao().deleteAll()
            database.logMessageDao().deleteAll()
        }
    }

    private fun startNewSession(context: Context) {
        val appContext = context.applicationContext
        val pm = appContext.packageManager
        val pi = pm.getPackageInfo(appContext.packageName, 0)
        scope.launch {
            database.sessionDao().insertOrReplace(
                SessionEntity(
                    id = _currentSessionId,
                    startedAt = System.currentTimeMillis(),
                    appVersion = pi.versionName,
                    buildNumber = pi.longVersionCode.toString(),
                    deviceInfo = JsonObject(
                        mapOf(
                            "manufacturer" to JsonPrimitive(Build.MANUFACTURER),
                            "model" to JsonPrimitive(Build.MODEL),
                            "sdk" to JsonPrimitive(Build.VERSION.SDK_INT),
                        )
                    ).toString(),
                )
            )
        }
    }

    private fun scheduleRetention() {
        scope.launch {
            while (true) {
                delay(config.sweepInterval.inWholeMilliseconds)
                RetentionManager.sweep(database, config)
            }
        }
    }

    /** Events emitted by Kulse when stored transactions change. */
    sealed interface Event {
        /** A new transaction with the given [id] has been inserted. */
        data class TransactionInserted(val id: Long) : Event
        /** The transaction with the given [id] has been updated (e.g., upon receiving the response). */
        data class TransactionUpdated(val id: Long) : Event
    }
}
