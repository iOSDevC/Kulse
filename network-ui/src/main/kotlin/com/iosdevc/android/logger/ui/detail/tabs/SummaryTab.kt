package com.iosdevc.android.logger.ui.detail.tabs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iosdevc.android.logger.model.HttpTransaction
import com.iosdevc.android.logger.ui.detail.components.HeadersSection
import com.iosdevc.android.logger.ui.detail.components.TimingChart

@Composable
fun SummaryTab(
    transaction: HttpTransaction,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        // -- Request Info --
        SectionTitle("Request")
        Spacer(Modifier.height(8.dp))
        SummaryRow("URL", transaction.url ?: "-")
        SummaryRow("Method", transaction.method ?: "-")
        SummaryRow("Host", transaction.host ?: "-")
        SummaryRow("Path", transaction.path ?: "-")
        SummaryRow("Scheme", transaction.scheme ?: "-")

        Spacer(Modifier.height(16.dp))

        // -- Response Info --
        SectionTitle("Response")
        Spacer(Modifier.height(8.dp))
        SummaryRow(
            "Status",
            if (transaction.responseCode != null) {
                "${transaction.responseCode} ${transaction.responseMessage ?: ""}"
            } else {
                "Pending"
            }
        )
        SummaryRow("Protocol", transaction.protocol ?: "-")
        SummaryRow(
            "Duration",
            if (transaction.duration != null) "${transaction.duration} ms" else "-"
        )
        SummaryRow("Request Size", formatBytes(transaction.requestBodySize))
        SummaryRow("Response Size", formatBytes(transaction.responseBodySize))

        val errorMsg = transaction.error
        if (errorMsg != null) {
            SummaryRow("Error", errorMsg)
        }

        Spacer(Modifier.height(16.dp))

        // -- TLS --
        val tlsVer = transaction.tlsVersion
        if (tlsVer != null) {
            SectionTitle("Security")
            Spacer(Modifier.height(8.dp))
            SummaryRow("TLS Version", tlsVer)
            SummaryRow("Cipher Suite", transaction.cipherSuite ?: "-")
            SummaryRow("Cache", if (transaction.isFromCache) "Yes" else "No")
            Spacer(Modifier.height(16.dp))
        }

        // -- Timing --
        val hasTimingData = transaction.dnsStart != null || transaction.connectStart != null
        if (hasTimingData) {
            SectionTitle("Timing")
            Spacer(Modifier.height(12.dp))
            TimingChart(
                dnsMs = computeDuration(transaction.dnsStart, transaction.dnsEnd) ?: 0L,
                connectMs = computeDuration(transaction.connectStart, transaction.connectEnd) ?: 0L,
                tlsMs = computeDuration(transaction.tlsStart, transaction.tlsEnd) ?: 0L,
                requestMs = computeDuration(transaction.requestStart, transaction.requestEnd) ?: 0L,
                waitMs = 0L,
                responseMs = computeDuration(transaction.responseStart, transaction.responseEnd) ?: 0L,
                totalMs = transaction.duration ?: 0L,
            )
            Spacer(Modifier.height(16.dp))
        }

        // -- Request Headers --
        if (transaction.requestHeaders.isNotEmpty()) {
            HeadersSection(
                title = "Request Headers",
                headers = transaction.requestHeaders,
            )
            Spacer(Modifier.height(16.dp))
        }

        // -- Response Headers --
        if (transaction.responseHeaders.isNotEmpty()) {
            HeadersSection(
                title = "Response Headers",
                headers = transaction.responseHeaders,
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
    )
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(0.3f),
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(0.7f),
            )
        }
        Spacer(Modifier.height(4.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    }
}

private fun computeDuration(start: Long?, end: Long?): Long? {
    if (start == null || end == null) return null
    val diff = end - start
    return if (diff >= 0) diff else null
}

private fun formatBytes(bytes: Long): String {
    return when {
        bytes <= 0 -> "-"
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "%.1f KB".format(bytes / 1024.0)
        else -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
    }
}
