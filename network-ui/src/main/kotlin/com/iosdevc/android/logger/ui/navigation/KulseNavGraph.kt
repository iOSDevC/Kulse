package com.iosdevc.android.logger.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.iosdevc.android.logger.ui.console.ConsoleScreen
import com.iosdevc.android.logger.ui.detail.TransactionDetailScreen
import com.iosdevc.android.logger.ui.session.SessionListScreen
import com.iosdevc.android.logger.ui.settings.SettingsScreen

object NavRoutes {
    const val CONSOLE = "console"
    const val TRANSACTION_DETAIL = "transaction/{id}"
    const val SESSIONS = "sessions"
    const val SETTINGS = "settings"

    fun transactionDetail(id: Long): String = "transaction/$id"
}

@Composable
fun KulseNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = NavRoutes.CONSOLE,
    ) {
        composable(NavRoutes.CONSOLE) { backStackEntry ->
            val sessionId = backStackEntry.savedStateHandle.get<String?>("sessionId")
            ConsoleScreen(
                sessionId = sessionId,
                onTransactionClick = { id ->
                    navController.navigate(NavRoutes.transactionDetail(id))
                },
                onSettingsClick = {
                    navController.navigate(NavRoutes.SETTINGS)
                },
            )
        }

        composable(
            route = NavRoutes.TRANSACTION_DETAIL,
            arguments = listOf(
                navArgument("id") { type = NavType.LongType }
            ),
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getLong("id") ?: return@composable
            TransactionDetailScreen(
                transactionId = transactionId,
                onBack = { navController.popBackStack() },
            )
        }

        composable(NavRoutes.SESSIONS) {
            SessionListScreen(
                onBack = { navController.popBackStack() },
                onSessionSelected = { sessionId ->
                    // Navigate back to console with session filter
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("sessionId", sessionId)
                    navController.popBackStack()
                },
            )
        }

        composable(NavRoutes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onBrowseSessions = {
                    navController.navigate(NavRoutes.SESSIONS)
                },
            )
        }
    }
}
