package com.iosdevc.android.logger.ui.detail.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val DnsColor = Color(0xFF9C27B0)
private val ConnectColor = Color(0xFFFF9800)
private val TlsColor = Color(0xFF4CAF50)
private val RequestColor = Color(0xFF2196F3)
private val WaitColor = Color(0xFF9E9E9E)
private val ResponseColor = Color(0xFF00BCD4)

private data class TimingSegment(
    val label: String,
    val durationMs: Long,
    val color: Color,
)

@Composable
fun TimingChart(
    dnsMs: Long,
    connectMs: Long,
    tlsMs: Long,
    requestMs: Long,
    waitMs: Long,
    responseMs: Long,
    totalMs: Long,
    modifier: Modifier = Modifier,
) {
    val segments = listOf(
        TimingSegment("DNS", dnsMs, DnsColor),
        TimingSegment("Connect", connectMs, ConnectColor),
        TimingSegment("TLS", tlsMs, TlsColor),
        TimingSegment("Request", requestMs, RequestColor),
        TimingSegment("Wait", waitMs, WaitColor),
        TimingSegment("Response", responseMs, ResponseColor),
    )

    val effectiveTotal = if (totalMs > 0) totalMs.toFloat() else 1f

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp),
        ) {
            val barHeight = size.height
            var xOffset = 0f

            segments.forEach { segment ->
                if (segment.durationMs > 0) {
                    val segmentWidth = (segment.durationMs / effectiveTotal) * size.width
                    drawRoundRect(
                        color = segment.color,
                        topLeft = Offset(xOffset, 0f),
                        size = Size(segmentWidth.coerceAtLeast(2f), barHeight),
                        cornerRadius = CornerRadius(4f, 4f),
                    )
                    xOffset += segmentWidth
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            segments.filter { it.durationMs > 0 }.forEach { segment ->
                Row(
                    modifier = Modifier.padding(end = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Canvas(modifier = Modifier.size(8.dp)) {
                        drawCircle(color = segment.color)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = segment.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Total: ${totalMs}ms",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
fun TimingRow(
    label: String,
    durationMs: Long?,
    colorHex: Long?,
    modifier: Modifier = Modifier,
    isBold: Boolean = false,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (colorHex != null) {
                Canvas(modifier = Modifier.size(10.dp)) {
                    drawCircle(color = Color(colorHex))
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )

            Text(
                text = if (durationMs != null) "${durationMs} ms" else "-",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    }
}
