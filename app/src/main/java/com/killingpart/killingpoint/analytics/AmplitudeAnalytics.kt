package com.killingpart.killingpoint.analytics

import android.content.Context
import com.killingpart.killingpoint.KillingPointApplication

object AmplitudeAnalytics {
    private var appContext: Context? = null

    private val amplitude
        get() = (appContext?.applicationContext as? KillingPointApplication)?.amplitude

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun track(event: String, properties: Map<String, Any?> = emptyMap()) {
        if (properties.isEmpty()) {
            amplitude?.track(event)
        } else {
            amplitude?.track(event, properties)
        }
    }
}
