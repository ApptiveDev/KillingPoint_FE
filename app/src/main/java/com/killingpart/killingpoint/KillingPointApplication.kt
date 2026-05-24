package com.killingpart.killingpoint

import android.app.Application
import com.amplitude.android.Amplitude
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
                // 커스텀 이벤트만 수집 (Application Opened, Screen Viewed 등 자동 추적 비활성화)
                autocapture = emptySet()
            )
        )
        amplitude.add(SessionReplayPlugin())
        AmplitudeAnalytics.init(applicationContext)
    }
}
