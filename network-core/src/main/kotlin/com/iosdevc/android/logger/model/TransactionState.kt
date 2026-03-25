package com.iosdevc.android.logger.model

/**
 * Lifecycle state of an HTTP transaction.
 *
 * @property value Numeric representation used for database persistence.
 */
enum class TransactionState(val value: Int) {
    /** The request has been sent but the response has not yet been received. */
    PENDING(0),
    /** The transaction completed successfully with a response from the server. */
    COMPLETE(1),
    /** The transaction failed due to a network error, timeout, or other exception. */
    FAILED(2);

    companion object {
        /**
         * Gets the [TransactionState] corresponding to the given numeric value.
         *
         * @param value Numeric value of the state.
         * @return The corresponding state, or [PENDING] if not found.
         */
        fun fromInt(value: Int): TransactionState =
            entries.firstOrNull { it.value == value } ?: PENDING
    }
}
