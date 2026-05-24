package com.killingpart.killingpoint.analytics

object EngagementAnalytics {
    object MainTab {
        const val MY = "my"
        const val EXPLORE = "explore"
        const val SOCIAL = "social"
        const val ADD = "add"
    }

    private var activeTab: String? = null
    private var activeTabSinceMs: Long = 0L

    fun exploreFeedCardViewed(feedIndex: Int) {
        AmplitudeAnalytics.track(
            "explore_feed_card_viewed",
            mapOf("feed_index" to feedIndex)
        )
    }

    /** 앱 오픈 후 MY 탭 자동 진입 시 타이머만 시작하고 이벤트는 보내지 않는다. */
    fun markAppOpenedOnMyTab() {
        activeTab = MainTab.MY
        activeTabSinceMs = System.currentTimeMillis()
    }

    /** 하단 탭 화면이 표시될 때 머무름 시간 계산 기준을 갱신한다. (이벤트 미전송) */
    fun onMainTabScreenVisible(tab: String) {
        if (activeTab != tab) {
            activeTab = tab
            activeTabSinceMs = System.currentTimeMillis()
        }
    }

    fun mainTabSelected(tab: String) {
        val properties = mutableMapOf<String, Any>("tab" to tab)
        activeTab?.let { previousTab ->
            val stayDurationSec =
                ((System.currentTimeMillis() - activeTabSinceMs) / 1000).coerceAtLeast(0)
            properties["stay_duration_sec"] = stayDurationSec
            if (previousTab != tab) {
                properties["previous_tab"] = previousTab
            }
        }

        AmplitudeAnalytics.track("main_tab_selected", properties)

        activeTab = tab
        activeTabSinceMs = System.currentTimeMillis()
    }
}
