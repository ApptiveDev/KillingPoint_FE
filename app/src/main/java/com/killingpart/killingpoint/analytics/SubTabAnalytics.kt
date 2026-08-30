package com.killingpart.killingpoint.analytics

object SubTabAnalytics {

    object Tab {
        const val MY = "my"
        const val SOCIAL = "social"
    }

    /** sub_tab 허용값 (이벤트 로그 정의서) */
    object SubTab {
        const val COLLECTION = "collection"
        const val KILLINGPART_PLAY = "killingpart_play"
        const val MUSIC_CALENDAR = "music_calendar"
        const val FEED = "feed"
        const val FRIENDS = "friends"
        const val NOTIFICATION = "notification"
        const val UNKNOWN = "unknown"
        const val NONE = "none"

        private val allowed =
            setOf(COLLECTION, KILLINGPART_PLAY, MUSIC_CALENDAR, FEED, FRIENDS, NOTIFICATION)

        fun normalize(value: String?): String =
            if (value != null && value in allowed) value else UNKNOWN
    }

    object EntryType {
        const val APP_LAUNCH = "app_launch"
        const val APP_FOREGROUND = "app_foreground"
        const val TAB_ENTER = "tab_enter"
        const val USER_SELECT = "user_select"
    }

    object EndReason {
        const val SUB_TAB_CHANGE = "sub_tab_change"
        const val TAB_CHANGE = "tab_change"
        const val APP_BACKGROUND = "app_background"
        const val VIEW_DISAPPEAR = "view_disappear"
    }

    private data class ActiveSubTab(val tab: String, val subTab: String, val sinceMs: Long)

    private var active: ActiveSubTab? = null

    // 상위 탭별로 마지막에 표시된 서브탭 (프로세스 생존 동안 기억). 하단 탭 재진입 시 복원 목적지로 사용.
    private var lastMySubTab: String = SubTab.KILLINGPART_PLAY
    private var lastSocialSubTab: String = SubTab.FEED
    private var lastSocialPillSubTab: String = SubTab.FEED

    // 프로세스 시작 시 true. 최초로 표시되는 서브탭 화면의 entry_type을 app_launch로 만든다.
    private var pendingAppLaunch: Boolean = true

    // 앱이 백그라운드로 갈 때 어떤 상위 탭이 활성 상태였는지 기억. 복귀 시 entry_type을 app_foreground로 만든다.
    private var backgroundedTab: String? = null

    // 알림 서브탭에서 뒤로가기로 소셜로 돌아왔을 때, 복귀 이벤트를 보내야 함을 표시.
    private var pendingSocialReturn: Boolean = false

    fun rememberedMySubTab(): String = lastMySubTab

    fun rememberedSocialPillSubTab(): String = lastSocialPillSubTab

    fun mainScreenArgFor(subTab: String): String = when (subTab) {
        SubTab.COLLECTION -> "profile"
        SubTab.MUSIC_CALENDAR -> "calendar"
        else -> "play"
    }

    fun socialScreenArgFor(subTab: String): String = when (subTab) {
        SubTab.FRIENDS -> "friend"
        else -> "feed"
    }

    /** 유저의 명시적 서브탭 선택이 아닌 진입(앱 실행/포그라운드 복귀/상위 탭 진입)에 사용 */
    fun enterTab(tab: String, subTab: String) {
        val normalized = SubTab.normalize(subTab)
        val entryType = when {
            pendingAppLaunch -> EntryType.APP_LAUNCH
            backgroundedTab == tab -> EntryType.APP_FOREGROUND
            else -> EntryType.TAB_ENTER
        }
        pendingAppLaunch = false
        if (backgroundedTab == tab) backgroundedTab = null

        open(tab, normalized, entryType, previousSubTab = null)
    }

    /** 같은 상위 탭 안에서 유저가 서브탭(pill/버튼)을 직접 선택했을 때 사용 */
    fun selectSubTab(tab: String, subTab: String) {
        val normalized = SubTab.normalize(subTab)
        val current = active
        if (current != null && current.tab == tab && current.subTab == normalized) {
            return // 같은 서브탭 재선택 - 발사하지 않음
        }
        val previousSubTab = current?.takeIf { it.tab == tab }?.subTab
        open(tab, normalized, EntryType.USER_SELECT, previousSubTab)
    }

    /** 하단 탭에서 다른 상위 탭으로 이동하기 직전 호출 */
    fun onLeavingTopTabs() {
        close(EndReason.TAB_CHANGE)
    }

    /** 서브탭 화면이 서브탭 전환/상위 탭 전환/백그라운드 외의 사유로 사라졌을 때 (상세 화면 진입, 뒤로가기 등) */
    fun onScreenDisappeared(tab: String) {
        if (active?.tab == tab) close(EndReason.VIEW_DISAPPEAR)
    }

    fun onAppBackgrounded() {
        val current = active ?: return
        backgroundedTab = current.tab
        close(EndReason.APP_BACKGROUND)
    }

    /** 서브탭 화면이 다시 RESUME됐을 때 호출. 백그라운드에서 돌아온 경우에만 재진입 이벤트를 보낸다. */
    fun onScreenResumed(tab: String, currentSubTab: () -> String) {
        if (active?.tab == tab) return
        if (backgroundedTab != tab) return
        enterTab(tab, currentSubTab())
    }

    /** 알림 서브탭에서 뒤로가기로 소셜 피드/친구로 돌아갈 때 호출 */
    fun leaveNotificationBackToSocial() {
        if (active?.tab == Tab.SOCIAL && active?.subTab == SubTab.NOTIFICATION) {
            close(EndReason.SUB_TAB_CHANGE)
            pendingSocialReturn = true
        }
    }

    fun consumeSocialReturnIfPending(): Boolean {
        if (!pendingSocialReturn) return false
        pendingSocialReturn = false
        return true
    }

    private fun rememberSubTab(tab: String, subTab: String) {
        when (tab) {
            Tab.MY -> lastMySubTab = subTab
            Tab.SOCIAL -> {
                lastSocialSubTab = subTab
                if (subTab == SubTab.FEED || subTab == SubTab.FRIENDS) {
                    lastSocialPillSubTab = subTab
                }
            }
        }
    }

    private fun open(tab: String, subTab: String, entryType: String, previousSubTab: String?) {
        val current = active
        if (current != null) {
            val endReason = if (current.tab != tab) EndReason.TAB_CHANGE else EndReason.SUB_TAB_CHANGE
            close(endReason)
        }

        AmplitudeAnalytics.track(
            "sub_tab_selected",
            mapOf(
                "tab" to tab,
                "sub_tab" to subTab,
                "entry_type" to entryType,
                "previous_sub_tab" to
                    if (entryType == EntryType.USER_SELECT) SubTab.normalize(previousSubTab) else SubTab.NONE
            )
        )

        rememberSubTab(tab, subTab)
        active = ActiveSubTab(tab, subTab, System.currentTimeMillis())
    }

    private fun close(endReason: String) {
        val current = active ?: return
        val stayDurationSec = (System.currentTimeMillis() - current.sinceMs) / 1000.0
        AmplitudeAnalytics.track(
            "sub_tab_stayed",
            mapOf(
                "tab" to current.tab,
                "sub_tab" to current.subTab,
                "stay_duration_sec" to stayDurationSec,
                "end_reason" to endReason
            )
        )
        active = null
    }
}
