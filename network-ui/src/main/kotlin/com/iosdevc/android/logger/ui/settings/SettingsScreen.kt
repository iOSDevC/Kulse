package com.iosdevc.android.logger.ui.settings

import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iosdevc.android.logger.Kulse
import com.iosdevc.android.logger.repository.StoreStats
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onBrowseSessions: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val repository = remember { Kulse.repository }
    var stats by remember { mutableStateOf<StoreStats?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        stats = repository.getStoreStats()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            // -- Store Info Section --
            SectionHeader("STORE")

            val containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)

            ListItem(
                headlineContent = { Text("Store Info") },
                leadingContent = {
                    Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                supportingContent = {
                    val s = stats
                    if (s != null) {
                        Text("${s.requestCount} requests  ·  ${formatBytes(s.totalBodySize)}")
                    }
                },
                colors = ListItemDefaults.colors(containerColor = containerColor),
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            ListItem(
                headlineContent = {
                    Text("Browse Sessions", color = MaterialTheme.colorScheme.primary)
                },
                leadingContent = {
                    Icon(Icons.Default.FolderOpen, null, tint = MaterialTheme.colorScheme.primary)
                },
                modifier = Modifier.clickable(onClick = onBrowseSessions),
                colors = ListItemDefaults.colors(containerColor = containerColor),
            )

            Spacer(Modifier.height(24.dp))

            // -- Actions Section --
            SectionHeader("ACTIONS")

            ListItem(
                headlineContent = {
                    Text("Remove All Logs", color = MaterialTheme.colorScheme.error)
                },
                leadingContent = {
                    Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                },
                modifier = Modifier.clickable { showDeleteDialog = true },
                colors = ListItemDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                ),
            )

            Spacer(Modifier.height(24.dp))

            // -- App Info Section --
            SectionHeader("APP INFO")

            val appInfoColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)

            ListItem(
                headlineContent = { Text("Device") },
                supportingContent = {
                    Text("${Build.MANUFACTURER} ${Build.MODEL} (API ${Build.VERSION.SDK_INT})")
                },
                colors = ListItemDefaults.colors(containerColor = appInfoColor),
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            ListItem(
                headlineContent = { Text("Session") },
                supportingContent = {
                    Text(
                        text = Kulse.currentSessionId,
                        style = MaterialTheme.typography.bodySmall,
                    )
                },
                colors = ListItemDefaults.colors(containerColor = appInfoColor),
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Remove All Logs") },
            text = { Text("This will permanently delete all network logs and messages. This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            Kulse.clearAll()
                            stats = repository.getStoreStats()
                        }
                        showDeleteDialog = false
                    },
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

private fun formatBytes(bytes: Long): String {
    return when {
        bytes <= 0 -> "0 B"
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "%.1f KB".format(bytes / 1024.0)
        else -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
    }
}
