package com.killingpart.killingpoint.analytics

object OnboardingAnalytics {
    object SkipStep {
        const val TUTORIAL_CHOICE = "tutorial_choice"
        const val TUTORIAL_TRACK_SEARCH = "tutorial_track_search"
        const val TUTORIAL_TRIM = "tutorial_trim"
        const val TUTORIAL_HOME = "tutorial_home"
        const val TUTORIAL_DIARY_DETAIL = "tutorial_diary_detail"
        const val TUTORIAL_NOTIFICATION = "tutorial_notification"
        const val UNKNOWN = "unknown"
    }

    fun appOpened() {
        AmplitudeAnalytics.track("app_opened")
    }

    fun authCompleted(provider: String, isNewUser: Boolean) {
        val event = if (isNewUser) "signup_completed" else "signin_completed"
        AmplitudeAnalytics.track(
            event,
            mapOf(
                "provider" to provider,
                "is_new_user" to isNewUser
            )
        )
    }

    fun onboardSkipped(skipStep: String) {
        AmplitudeAnalytics.track(
            "onboard_skipped",
            mapOf("skip_step" to skipStep)
        )
    }

    fun onboardCompleted() {
        AmplitudeAnalytics.track("onboard_completed")
    }
}
