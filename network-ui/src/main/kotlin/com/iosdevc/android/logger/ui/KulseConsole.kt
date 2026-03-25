package com.iosdevc.android.logger.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.iosdevc.android.logger.ui.navigation.KulseNavGraph
import com.iosdevc.android.logger.ui.theme.KulseTheme

/**
 * Root Composable for the Kulse network inspection console.
 *
 * Wraps the complete navigation graph within the [KulseTheme].
 * Can be used directly if you want to embed the console within
 * another Activity or composable instead of using [KulseActivity].
 *
 * @see KulseActivity
 */
@Composable
fun KulseConsole() {
    KulseTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            KulseNavGraph()
        }
    }
}
