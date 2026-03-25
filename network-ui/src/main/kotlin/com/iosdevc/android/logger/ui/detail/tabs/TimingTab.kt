package com.iosdevc.android.logger.ui.detail.tabs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iosdevc.android.logger.model.HttpTransaction
import com.iosdevc.android.logger.ui.detail.components.TimingChart
import com.iosdevc.android.logger.ui.detail.components.TimingRow

@Composable
fun TimingTab(
    transaction: HttpTransaction,
    modifier: Modifier = Modifier,
) {
    val dnsMs = computeDuration(transaction.dnsStart, transaction.dnsEnd)
    val connectMs = computeDuration(transaction.connectStart, transaction.connectEnd)
    val tlsMs = computeDuration(transaction.tlsStart, transaction.tlsEnd)
    val requestMs = computeDuration(transaction.requestStart, transaction.requestEnd)
    val responseMs = computeDuration(transaction.responseStart, transaction.responseEnd)

    val totalKnown = (dnsMs ?: 0L) + (connectMs ?: 0L) + (tlsMs ?: 0L) +
            (requestMs ?: 0L) + (responseMs ?: 0L)
    val totalDuration = transaction.duration ?: totalKnown
    val waitMs = if (totalDuration > totalKnown) totalDuration - totalKnown else 0L

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text(
            text = "Request Timing",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(16.dp))

        TimingChart(
            dnsMs = dnsMs ?: 0L,
            connectMs = connectMs ?: 0L,
            tlsMs = tlsMs ?: 0L,
            requestMs = requestMs ?: 0L,
            waitMs = waitMs,
            responseMs = responseMs ?: 0L,
            totalMs = totalDuration,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Phase Details",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(8.dp))

        TimingRow(label = "DNS Lookup", durationMs = dnsMs, colorHex = 0xFF9C27B0)
        TimingRow(label = "Connection", durationMs = connectMs, colorHex = 0xFFFF9800)
        TimingRow(label = "TLS Handshake", durationMs = tlsMs, colorHex = 0xFF4CAF50)
        TimingRow(label = "Request Send", durationMs = requestMs, colorHex = 0xFF2196F3)
        TimingRow(label = "Waiting (TTFB)", durationMs = if (waitMs > 0) waitMs else null, colorHex = 0xFF9E9E9E)
        TimingRow(label = "Response Receive", durationMs = responseMs, colorHex = 0xFF00BCD4)
        TimingRow(label = "Total", durationMs = totalDuration, colorHex = null, isBold = true)
    }
}

private fun computeDuration(start: Long?, end: Long?): Long? {
    if (start == null || end == null) return null
    val diff = end - start
    return if (diff >= 0) diff else null
}
