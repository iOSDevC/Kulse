package com.iosdevc.android.logger.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.iosdevc.android.logger.ui.theme.KulseTheme

/**
 * Activity that presents the Kulse network inspection console.
 *
 * Displays the list of captured HTTP transactions, their details, and
 * allows exporting or sharing the logs.
 *
 * To launch the console, use the companion method [start] or create an Intent
 * with [intent] to integrate it into your own navigation.
 *
 * @see KulseConsole
 */
class KulseActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KulseTheme {
                KulseConsole()
            }
        }
    }

    companion object {
        /**
         * Launches the Kulse console as a new Activity.
         *
         * Usage example:
         * ```kotlin
         * // From a debug button in your app
         * Button(onClick = { KulseActivity.start(context) }) {
         *     Text("Open Kulse")
         * }
         * ```
         *
         * @param context Context from which the Activity is launched.
         */
        fun start(context: Context) {
            context.startActivity(intent(context))
        }

        /**
         * Creates an [Intent] to launch the Kulse console.
         *
         * Useful when you need to customize the launch, for example
         * to integrate it into a debug menu or a notification.
         *
         * @param context Context needed to build the Intent.
         * @return [Intent] with FLAG_ACTIVITY_NEW_TASK configured.
         */
        fun intent(context: Context): Intent {
            return Intent(context, KulseActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }
    }
}
