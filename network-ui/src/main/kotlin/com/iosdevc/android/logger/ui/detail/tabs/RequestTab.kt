package com.iosdevc.android.logger.ui.detail.tabs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.iosdevc.android.logger.model.HttpTransaction
import com.iosdevc.android.logger.ui.detail.components.BodyViewer
import com.iosdevc.android.logger.ui.detail.components.HeadersSection

@Composable
fun RequestTab(
    transaction: HttpTransaction,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        HeadersSection(
            title = "Request Headers",
            headers = transaction.requestHeaders,
        )

        BodyViewer(
            title = "Request Body",
            body = transaction.requestBody,
            contentType = transaction.requestContentType,
        )
    }
}
