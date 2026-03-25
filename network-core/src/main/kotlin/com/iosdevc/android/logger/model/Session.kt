package com.iosdevc.android.logger.model

/**
 * Represents an application execution session.
 *
 * Each time [Kulse.install][com.iosdevc.android.logger.Kulse.install] is called,
 * a new session is created that groups all transactions and log messages
 * recorded during that execution.
 *
 * @property id Unique identifier of the session (UUID).
 * @property startedAt Timestamp in milliseconds of the session start.
 * @property appVersion Version name of the application (e.g., "1.2.0").
 * @property buildNumber Build number of the application.
 * @property deviceInfo Device information serialized as JSON (manufacturer, model, SDK).
 */
data class Session(
    val id: String,
    val startedAt: Long,
    val appVersion: String?,
    val buildNumber: String?,
    val deviceInfo: String?,
)
