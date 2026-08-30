package com.killingpart.killingpoint.analytics

object NotificationAnalytics {

    /** notification_type 허용값 (이벤트 로그 정의서) */
    object NotificationType {
        const val LIKE = "like"
        const val PICK = "pick"
        const val NEW_KILLINGPART = "new_killingpart"
        const val UNKNOWN = "unknown"

        private val allowed = setOf(LIKE, PICK, NEW_KILLINGPART, UNKNOWN)

        fun normalize(value: String?): String =
            if (value != null && value in allowed) value else UNKNOWN

        /** 알림함/푸시 payload의 alarm type("LIKE_ALARM" 등)을 이벤트 프로퍼티 값으로 변환 */
        fun fromAlarmType(alarmType: String?): String = when (alarmType) {
            "LIKE_ALARM" -> LIKE
            "SUBSCRIBE_ALARM" -> PICK
            "DIARY_ALARM" -> NEW_KILLINGPART
            else -> UNKNOWN
        }
    }

    /** entry_point 허용값 (이벤트 로그 정의서) */
    object EntryPoint {
        const val PUSH = "push"
        const val NOTIFICATION_LIST = "notification_list"
        const val PICK_LIST = "pick_list"
        const val SOCIAL_TAB = "social_tab"
        const val UNKNOWN = "unknown"
    }

    fun pushNotificationOpened(
        notificationType: String,
        notificationId: String? = null,
        isColdStart: Boolean? = null
    ) {
        AmplitudeAnalytics.track(
            "push_notification_opened",
            buildMap {
                put("notification_type", NotificationType.normalize(notificationType))
                notificationId?.let { put("notification_id", it) }
                isColdStart?.let { put("is_cold_start", it) }
            }
        )
    }

    fun notificationListViewed(entryPoint: String? = null, unreadCount: Int? = null) {
        AmplitudeAnalytics.track(
            "notification_list_viewed",
            buildMap {
                entryPoint?.let { put("entry_point", it) }
                unreadCount?.let { put("unread_count", it) }
            }
        )
    }

    fun notificationSelected(
        notificationType: String,
        notificationId: String? = null,
        listPosition: Int? = null
    ) {
        AmplitudeAnalytics.track(
            "notification_selected",
            buildMap {
                put("notification_type", NotificationType.normalize(notificationType))
                notificationId?.let { put("notification_id", it) }
                listPosition?.let { put("list_position", it) }
            }
        )
    }

    fun killingpartDetailViewed(
        notificationType: String? = null,
        entryPoint: String? = null,
        diaryId: String? = null,
        trackId: String? = null
    ) {
        AmplitudeAnalytics.track(
            "killingpart_detail_viewed",
            buildMap {
                notificationType?.let { put("notification_type", NotificationType.normalize(it)) }
                entryPoint?.let { put("entry_point", it) }
                diaryId?.let { put("diary_id", it) }
                trackId?.let { put("track_id", it) }
            }
        )
    }

    fun pickListViewed(entryPoint: String? = null, pickCount: Int? = null) {
        AmplitudeAnalytics.track(
            "pick_list_viewed",
            buildMap {
                entryPoint?.let { put("entry_point", it) }
                pickCount?.let { put("pick_count", it) }
            }
        )
    }

    fun profileViewed(
        entryPoint: String? = null,
        profileUserId: String? = null,
        isOwnProfile: Boolean? = null
    ) {
        AmplitudeAnalytics.track(
            "profile_viewed",
            buildMap {
                entryPoint?.let { put("entry_point", it) }
                profileUserId?.let { put("profile_user_id", it) }
                isOwnProfile?.let { put("is_own_profile", it) }
            }
        )
    }
}
