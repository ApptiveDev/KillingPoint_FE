package com.killingpart.killingpoint.analytics

object KillingPartCutAnalytics {
    /** cut_handle_adjusted.control 허용값 (이벤트 로그 정의서) */
    object HandleSide {
        const val LEFT = "left"
        const val RIGHT = "right"
        const val SPECTRUM = "spectrum"
        const val MINIMAP = "minimap"
        const val NUDGE_MINUS_1S = "nudge_minus_1s"
        const val NUDGE_PLUS_1S = "nudge_plus_1s"
        const val UNKNOWN = "unknown"

        private val allowed = setOf(
            LEFT, RIGHT, SPECTRUM, MINIMAP, NUDGE_MINUS_1S, NUDGE_PLUS_1S, UNKNOWN
        )

        fun normalize(control: String): String =
            if (control in allowed) control else UNKNOWN
    }

    fun killingpartCutStarted() {
        AmplitudeAnalytics.track("killingpart_cut_started")
    }

    fun trackSelected(songId: String?) {
        val properties = songId?.let { mapOf("selected_songid" to it) } ?: emptyMap()
        AmplitudeAnalytics.track("track_selected", properties)
    }

    fun cutHandleAdjusted(control: String, startSec: Float, endSec: Float, clipDurationSec: Float) {
        AmplitudeAnalytics.track(
            "cut_handle_adjusted",
            mapOf(
                "control" to HandleSide.normalize(control),
                "start_sec" to round2(startSec),
                "end_sec" to round2(endSec),
                "clip_duration_sec" to round2(clipDurationSec)
            )
        )
    }

    /** 정의서: float, 소수점 둘째 자리. Amplitude에는 Double로 보내 이진 오차를 줄인다. */
    private fun round2(value: Float): Double =
        kotlin.math.round(value.toDouble() * 100.0) / 100.0

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
