package com.iosdevc.android.logger.internal

import androidx.core.content.FileProvider

/**
 * FileProvider used to share exported logs. It has its own class because the manifest
 * merger matches providers by class name: declaring androidx's FileProvider directly would
 * clash with any host app that declares it too.
 */
internal class KulseFileProvider : FileProvider()
