package com.iosdevc.android.logger.ui.detail

import android.app.Application
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Html
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.iosdevc.android.logger.ui.detail.tabs.RequestTab
import com.iosdevc.android.logger.ui.detail.tabs.ResponseTab
import com.iosdevc.android.logger.ui.detail.tabs.SummaryTab

private val TABS = listOf("Response", "Summary", "Request")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    transactionId: Long,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val viewModel: TransactionDetailViewModel = viewModel(
        factory = TransactionDetailViewModel.Factory(application, transactionId),
    )
    val transaction by viewModel.transaction.collectAsState()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var showShareSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = transaction?.let { "${it.method ?: ""} ${it.path ?: ""}" }
                            ?: "Transaction",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showShareSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth(),
            ) {
                TABS.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) },
                    )
                }
            }

            transaction?.let { tx ->
                when (selectedTab) {
                    0 -> ResponseTab(transaction = tx)
                    1 -> SummaryTab(transaction = tx)
                    2 -> RequestTab(transaction = tx)
                }
            }
        }
    }

    if (showShareSheet) {
        ShareRequestLogSheet(
            onDismiss = { showShareSheet = false },
            onFormatSelected = { format ->
                showShareSheet = false
                viewModel.shareAs(context, format)
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShareRequestLogSheet(
    onDismiss: () -> Unit,
    onFormatSelected: (ShareFormat) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
        ) {
            Text(
                text = "Share Request Log",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )

            ShareOption(
                icon = Icons.Default.TextSnippet,
                label = "Share as Plain Text",
                onClick = { onFormatSelected(ShareFormat.PLAIN_TEXT) },
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            ShareOption(
                icon = Icons.Default.Description,
                label = "Share as Markdown",
                onClick = { onFormatSelected(ShareFormat.MARKDOWN) },
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            ShareOption(
                icon = Icons.Default.Html,
                label = "Share as HTML",
                onClick = { onFormatSelected(ShareFormat.HTML) },
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            ShareOption(
                icon = Icons.Default.Code,
                label = "Share as cURL",
                onClick = { onFormatSelected(ShareFormat.CURL) },
            )

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ShareOption(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    ListItem(
        headlineContent = { Text(label) },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        trailingContent = {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        modifier = Modifier.clickable(onClick = onClick),
    )
}
