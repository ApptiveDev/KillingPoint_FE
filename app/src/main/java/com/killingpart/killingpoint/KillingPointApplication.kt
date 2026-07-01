package com.killingpart.killingpoint

import android.app.Application
import com.amplitude.android.Amplitude
import com.amplitude.android.AutocaptureOption
import com.amplitude.android.Configuration
import com.amplitude.android.plugins.SessionReplayPlugin
import com.killingpart.killingpoint.analytics.AmplitudeAnalytics

class KillingPointApplication : Application() {
    lateinit var amplitude: Amplitude
        private set

    override fun onCreate() {
        super.onCreate()
        amplitude = Amplitude(
            Configuration(
                apiKey = BuildConfig.AMPLITUDE_API_KEY,
                context = applicationContext,
                autocapture = setOf(AutocaptureOption.APP_LIFECYCLES)
            )
        )
        amplitude.add(SessionReplayPlugin())
        AmplitudeAnalytics.init(amplitude)
    }
}
