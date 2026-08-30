package com.killingpart.killingpoint.analytics

object KillingPartCutAnalytics {
    object HandleSide {
        const val LEFT = "left"
        const val RIGHT = "right"
        const val SPECTRUM = "spectrum"
        const val MINIMAP = "minimap"
        const val NUDGE_MINUS_1S = "nudge_minus_1s"
        const val NUDGE_PLUS_1S = "nudge_plus_1s"
        const val UNKNOWN = "unknown"
    }

    fun killingpartCutStarted() {
        AmplitudeAnalytics.track("killingpart_cut_started")
    }

    fun trackSelected(songId: String?) {
        val properties = songId?.let { mapOf("selected_songid" to it) } ?: emptyMap()
        AmplitudeAnalytics.track("track_selected", properties)
    }

    fun cutHandleAdjusted(control: String, startSec: Float, endSec: Float, clipDurationSec: Float) {
        // round to 2 decimal places as spec'd
        val s = (kotlin.math.round(startSec * 100f) / 100f)
        val e = (kotlin.math.round(endSec * 100f) / 100f)
        val d = (kotlin.math.round(clipDurationSec * 100f) / 100f)
        AmplitudeAnalytics.track(
            "cut_handle_adjusted",
            mapOf(
                "control" to control,
                "start_sec" to s,
                "end_sec" to e,
                "clip_duration_sec" to d
            )
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
