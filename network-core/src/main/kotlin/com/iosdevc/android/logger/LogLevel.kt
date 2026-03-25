package com.iosdevc.android.logger

/**
 * Severity levels for custom log messages in Kulse.
 *
 * Levels are ordered from lowest to highest severity.
 * They are used both for tagging messages via [Kulse.log] and for
 * configuring the minimum level in [KulseConfig.logLevel].
 *
 * @property value Numeric representation of the level, used internally for persistence.
 */
enum class LogLevel(val value: Int) {
    /** Very detailed trace information. */
    TRACE(0),
    /** General debugging information. */
    DEBUG(1),
    /** Informational messages about the normal flow of the application. */
    INFO(2),
    /** Unexpected situations that do not prevent operation. */
    WARNING(3),
    /** Errors that affect a specific operation. */
    ERROR(4),
    /** Critical errors that may compromise application stability. */
    CRITICAL(5),
}
