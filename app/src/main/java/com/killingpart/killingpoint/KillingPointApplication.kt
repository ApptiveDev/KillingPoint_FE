package com.killingpart.killingpoint

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.amplitude.android.Amplitude
import com.amplitude.android.AutocaptureOption
import com.amplitude.android.Configuration
import com.amplitude.android.plugins.SessionReplayPlugin
import com.killingpart.killingpoint.analytics.AmplitudeAnalytics
import com.killingpart.killingpoint.analytics.SubTabAnalytics

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

        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStop(owner: LifecycleOwner) {
                SubTabAnalytics.onAppBackgrounded()
            }
        })
    }
}
