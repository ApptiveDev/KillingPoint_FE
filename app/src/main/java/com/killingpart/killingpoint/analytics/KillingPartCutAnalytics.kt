package com.killingpart.killingpoint.analytics

object KillingPartCutAnalytics {
    object HandleSide {
        const val LEFT = "left"
        const val RIGHT = "right"
        const val SPECTRUM_BAR = "spectrum_bar"
    }

    fun killingpartCutStarted() {
        AmplitudeAnalytics.track("killingpart_cut_started")
    }

    fun trackSelected(songId: String?) {
        val properties = songId?.let { mapOf("selected_songid" to it) } ?: emptyMap()
        AmplitudeAnalytics.track("track_selected", properties)
    }

    fun cutHandleAdjusted(handleSide: String) {
        AmplitudeAnalytics.track(
            "cut_handle_adjusted",
            mapOf("handle_side" to handleSide)
        )
    }

    fun cutCompleted() {
        AmplitudeAnalytics.track("cut_completed")
    }

    fun killingpartCutCompleted() {
        AmplitudeAnalytics.track("killingpart_cut_completed")
        AmplitudeAnalytics.incrementUserProperty("total_killingpart_registered_count")
    }

    fun onboardingKillingpartCutCompleted() {
        AmplitudeAnalytics.track("onboarding_killingpart_cut_completed")
    }
}
