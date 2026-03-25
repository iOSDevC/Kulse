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

@Composable
fun OverviewTab(
    transaction: HttpTransaction,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        OverviewRow("URL", transaction.url ?: "-")
        OverviewRow("Method", transaction.method ?: "-")
        OverviewRow("Protocol", transaction.protocol ?: "-")
        OverviewRow(
            "Status",
            if (transaction.responseCode != null) {
                "${transaction.responseCode} ${transaction.responseMessage ?: ""}"
            } else {
                "-"
            }
        )
        OverviewRow(
            "Duration",
            if (transaction.duration != null) "${transaction.duration} ms" else "-"
        )
        OverviewRow(
            "Request Size",
            formatBytes(transaction.requestBodySize)
        )
        OverviewRow(
            "Response Size",
            formatBytes(transaction.responseBodySize)
        )
        OverviewRow(
            "TLS",
            buildString {
                val version = transaction.tlsVersion
                val cipher = transaction.cipherSuite
                if (version != null) {
                    append(version)
                    if (cipher != null) append(" / $cipher")
                } else {
                    append("-")
                }
            }
        )
        OverviewRow(
            "Cache",
            if (transaction.isFromCache) "Yes" else "No"
        )
        OverviewRow("Error", transaction.error ?: "-")
    }
}

@Composable
private fun OverviewRow(
    label: String,
    value: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
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
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

private fun formatBytes(bytes: Long?): String {
    if (bytes == null || bytes < 0) return "-"
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "%.1f KB".format(bytes / 1024.0)
        else -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
    }
}
