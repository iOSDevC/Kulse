package com.iosdevc.android.logger.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Green = Color(0xFF4CAF50)
private val Blue = Color(0xFF2196F3)
private val Amber = Color(0xFFFFC107)
private val Red = Color(0xFFF44336)
private val Gray = Color(0xFF9E9E9E)

@Composable
fun StatusCodeBadge(
    statusCode: Int?,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = when {
        statusCode == null -> Gray
        statusCode in 200..299 -> Green
        statusCode in 300..399 -> Blue
        statusCode in 400..499 -> Amber
        statusCode in 500..599 -> Red
        else -> Gray
    }

    val displayText = statusCode?.toString() ?: "---"

    Text(
        text = displayText,
        modifier = modifier
            .background(
                color = backgroundColor.copy(alpha = 0.2f),
                shape = RoundedCornerShape(4.dp),
            )
            .padding(horizontal = 6.dp, vertical = 2.dp),
        color = backgroundColor,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
    )
}
