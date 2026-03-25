package com.iosdevc.android.logger.ui.filter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val HTTP_METHODS = listOf("GET", "POST", "PUT", "DELETE", "PATCH")

private data class StatusRange(
    val label: String,
    val min: Int,
    val max: Int,
)

private val STATUS_RANGES = listOf(
    StatusRange("2xx", 200, 299),
    StatusRange("3xx", 300, 399),
    StatusRange("4xx", 400, 499),
    StatusRange("5xx", 500, 599),
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterSheet(
    sheetState: SheetState,
    currentFilter: FilterState,
    hosts: List<String>,
    onApply: (FilterState) -> Unit,
    onDismiss: () -> Unit,
) {
    var selectedMethod by remember(currentFilter) { mutableStateOf(currentFilter.method) }
    var selectedHost by remember(currentFilter) { mutableStateOf(currentFilter.host ?: "") }
    var selectedMinStatus by remember(currentFilter) { mutableStateOf(currentFilter.minStatusCode) }
    var selectedMaxStatus by remember(currentFilter) { mutableStateOf(currentFilter.maxStatusCode) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = "Filter Transactions",
                style = MaterialTheme.typography.titleLarge,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "HTTP Method",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                HTTP_METHODS.forEach { method ->
                    FilterChip(
                        selected = selectedMethod == method,
                        onClick = {
                            selectedMethod = if (selectedMethod == method) null else method
                        },
                        label = { Text(method) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Status Code",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                STATUS_RANGES.forEach { range ->
                    val isSelected = selectedMinStatus == range.min && selectedMaxStatus == range.max
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (isSelected) {
                                selectedMinStatus = null
                                selectedMaxStatus = null
                            } else {
                                selectedMinStatus = range.min
                                selectedMaxStatus = range.max
                            }
                        },
                        label = { Text(range.label) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Host",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = selectedHost,
                onValueChange = { selectedHost = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. api.example.com") },
                singleLine = true,
            )

            if (hosts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    hosts.forEach { host ->
                        FilterChip(
                            selected = selectedHost == host,
                            onClick = {
                                selectedHost = if (selectedHost == host) "" else host
                            },
                            label = { Text(host, maxLines = 1) },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                OutlinedButton(
                    onClick = {
                        selectedMethod = null
                        selectedHost = ""
                        selectedMinStatus = null
                        selectedMaxStatus = null
                        onApply(FilterState.EMPTY.copy(sessionId = currentFilter.sessionId))
                    },
                ) {
                    Text("Reset")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        onApply(
                            FilterState(
                                sessionId = currentFilter.sessionId,
                                method = selectedMethod,
                                host = selectedHost.ifBlank { null },
                                minStatusCode = selectedMinStatus,
                                maxStatusCode = selectedMaxStatus,
                                afterDate = currentFilter.afterDate,
                                beforeDate = currentFilter.beforeDate,
                            )
                        )
                    },
                ) {
                    Text("Apply")
                }
            }
        }
    }
}
