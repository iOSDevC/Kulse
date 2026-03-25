package com.iosdevc.android.logger.ui.filter

data class FilterState(
    val sessionId: String? = null,
    val method: String? = null,
    val host: String? = null,
    val minStatusCode: Int? = null,
    val maxStatusCode: Int? = null,
    val afterDate: Long? = null,
    val beforeDate: Long? = null,
) {
    val isActive: Boolean
        get() = method != null || host != null || minStatusCode != null ||
                maxStatusCode != null || afterDate != null || beforeDate != null

    companion object {
        val EMPTY = FilterState()
    }
}
