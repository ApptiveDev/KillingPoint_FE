package com.killingpart.killingpoint.analytics

import com.amplitude.android.Amplitude
import com.amplitude.android.events.Identify

object AmplitudeAnalytics {
    private var amplitude: Amplitude? = null

    fun init(amplitudeInstance: Amplitude) {
        amplitude = amplitudeInstance
    }

    fun track(event: String, properties: Map<String, Any?> = emptyMap()) {
        if (properties.isEmpty()) {
            amplitude?.track(event)
        } else {
            amplitude?.track(event, properties.toMutableMap())
        }
    }

    fun incrementUserProperty(property: String, by: Double = 1.0) {
        amplitude?.identify(Identify().add(property, by))
    }
}
