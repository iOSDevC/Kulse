package com.iosdevc.android.logger.sample

import android.app.Application
import com.iosdevc.android.logger.Kulse
import com.iosdevc.android.logger.KulseConfig

class SampleApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Kulse.install(
            context = this,
            config = KulseConfig(
                sensitiveHeaders = setOf("Authorization", "X-Api-Key"),
            ),
        )
    }
}
