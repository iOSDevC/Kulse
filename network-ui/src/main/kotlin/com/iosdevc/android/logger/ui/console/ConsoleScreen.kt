package com.iosdevc.android.logger.ui.console

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.iosdevc.android.logger.ui.components.EmptyState
import com.iosdevc.android.logger.ui.components.KulseSearchBar
import com.iosdevc.android.logger.ui.filter.FilterSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsoleScreen(
    sessionId: String?,
    onTransactionClick: (Long) -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: ConsoleViewModel = viewModel(),
) {
    val context = LocalContext.current
    val transactions by viewModel.transactions.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterState by viewModel.filterState.collectAsState()
    val hosts by viewModel.hosts.collectAsState()

    var showFilterSheet by remember { mutableStateOf(false) }
    val filterSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(sessionId) {
        if (sessionId != null) {
            viewModel.setSessionId(sessionId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kulse") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
                actions = {
                    IconButton(onClick = { showFilterSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter",
                            tint = if (filterState.isActive) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                        )
                    }
                    IconButton(onClick = { viewModel.export(context) }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Export",
                        )
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            KulseSearchBar(
                query = searchQuery,
                onQueryChange = viewModel::updateSearchQuery,
            )

            if (transactions.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.Wifi,
                    message = if (searchQuery.isNotBlank() || filterState.isActive) {
                        "No matching requests found"
                    } else {
                        "No network requests yet"
                    },
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(
                        items = transactions,
                        key = { it.id },
                    ) { transaction ->
                        TransactionListItem(
                            transaction = transaction,
                            onClick = { onTransactionClick(transaction.id) },
                        )
                    }
                }
            }
        }
    }

    if (showFilterSheet) {
        FilterSheet(
            sheetState = filterSheetState,
            currentFilter = filterState,
            hosts = hosts,
            onApply = { newFilter ->
                viewModel.updateFilter(newFilter)
                showFilterSheet = false
            },
            onDismiss = { showFilterSheet = false },
        )
    }
}
