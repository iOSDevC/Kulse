package com.iosdevc.android.logger.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.iosdevc.android.logger.ui.KulseActivity
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                SampleScreen()
            }
        }
    }
}

@Composable
private fun SampleScreen() {
    val context = LocalContext.current
    val api = remember { SampleApi.create() }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    fun runRequest(label: String, block: suspend () -> Unit) {
        scope.launch {
            try {
                block()
                snackbarHostState.showSnackbar("$label completed")
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("$label failed: ${e.message}")
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Kulse Sample",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { KulseActivity.start(context) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                Text("Open Kulse")
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "API Requests",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            OutlinedButton(
                onClick = { runRequest("GET /posts") { api.getPosts() } },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("GET /posts")
            }

            OutlinedButton(
                onClick = { runRequest("GET /posts/1") { api.getPost(1) } },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("GET /posts/1")
            }

            OutlinedButton(
                onClick = {
                    runRequest("POST /posts") {
                        api.createPost(Post(userId = 1, title = "Test Post", body = "This is a test post body"))
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("POST /posts")
            }

            OutlinedButton(
                onClick = {
                    runRequest("PUT /posts/1") {
                        api.updatePost(1, Post(id = 1, userId = 1, title = "Updated", body = "Updated body"))
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("PUT /posts/1")
            }

            OutlinedButton(
                onClick = { runRequest("DELETE /posts/1") { api.deletePost(1) } },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("DELETE /posts/1")
            }

            OutlinedButton(
                onClick = { runRequest("GET /comments") { api.getComments() } },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("GET /comments")
            }

            OutlinedButton(
                onClick = { runRequest("GET /users") { api.getUsers() } },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("GET /users")
            }

            OutlinedButton(
                onClick = { runRequest("GET /todos") { api.getTodos() } },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("GET /todos")
            }

            OutlinedButton(
                onClick = {
                    runRequest("Multiple requests") {
                        api.getPosts()
                        api.getUsers()
                        api.getComments()
                        api.getTodos()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Run All Requests")
            }
        }
    }
}
