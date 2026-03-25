package com.iosdevc.android.logger.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val GetColor = Color(0xFF4CAF50)
private val PostColor = Color(0xFF2196F3)
private val PutColor = Color(0xFFFF9800)
private val DeleteColor = Color(0xFFF44336)
private val PatchColor = Color(0xFF9C27B0)
private val DefaultColor = Color(0xFF9E9E9E)

@Composable
fun MethodBadge(
    method: String?,
    modifier: Modifier = Modifier,
) {
    val color = when (method?.uppercase()) {
        "GET" -> GetColor
        "POST" -> PostColor
        "PUT" -> PutColor
        "DELETE" -> DeleteColor
        "PATCH" -> PatchColor
        else -> DefaultColor
    }

    Text(
        text = method?.uppercase() ?: "?",
        modifier = modifier,
        color = color,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
    )
}
