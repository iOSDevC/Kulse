package com.iosdevc.android.logger.ui.session

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.iosdevc.android.logger.Kulse
import com.iosdevc.android.logger.model.Session
import com.iosdevc.android.logger.ui.components.EmptyState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionListScreen(
    onBack: () -> Unit,
    onSessionSelected: (String) -> Unit,
) {
    val repository = remember { Kulse.repository }
    var sessions by remember { mutableStateOf(emptyList<Session>()) }

    LaunchedEffect(Unit) {
        sessions = repository.getSessionsWithData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sessions") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { paddingValues ->
        if (sessions.isEmpty()) {
            EmptyState(
                icon = Icons.Default.History,
                message = "No sessions with data",
                modifier = Modifier.padding(paddingValues),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            ) {
                items(
                    items = sessions,
                    key = { it.id },
                ) { session ->
                    SessionItem(
                        session = session,
                        isCurrent = session.id == Kulse.currentSessionId,
                        onClick = { onSessionSelected(session.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SessionItem(
    session: Session,
    isCurrent: Boolean,
    onClick: () -> Unit,
) {
    val dateFormatter = remember {
        SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault())
    }

    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = {
            Text(
                text = buildString {
                    append(dateFormatter.format(Date(session.startedAt)))
                    if (isCurrent) append("  (current)")
                },
                style = MaterialTheme.typography.titleSmall,
            )
        },
        supportingContent = {
            val details = buildString {
                val ver = session.appVersion
                if (ver != null) {
                    append("v$ver")
                    val build = session.buildNumber
                    if (build != null) {
                        append(" ($build)")
                    }
                }
            }
            if (details.isNotEmpty()) {
                Text(
                    text = details,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    )
}
