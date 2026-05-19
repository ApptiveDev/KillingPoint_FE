package com.killingpart.killingpoint.data.model

import android.net.Uri

/**
 * 알림 [AlarmItem.type] + [AlarmItem.deepLink] 조합으로 이동 가능 여부·대상을 판별한다.
 * deepLink는 백엔드 API 경로 형식 (예: `/api/diaries/594`, `/api/subscribes/8/fans`).
 */
object AlarmDeepLink {

    fun isNavigable(type: String, deepLink: String): Boolean = when (type) {
        "LIKE_ALARM", "DIARY_ALARM" -> isDiaryDeepLink(deepLink)
        "SUBSCRIBE_ALARM" -> isSubscribeFansDeepLink(deepLink)
        else -> false
    }

    fun isDiaryDeepLink(deepLink: String): Boolean =
        pathSegments(deepLink).let { segments ->
            val i = segments.indexOf("diaries")
            i >= 0 && i < segments.lastIndex && segments[i + 1].toLongOrNull() != null
        }

    fun isSubscribeFansDeepLink(deepLink: String): Boolean {
        val segments = pathSegments(deepLink)
        val subscribeIndex = segments.indexOf("subscribes")
        return subscribeIndex >= 0 &&
            subscribeIndex < segments.lastIndex - 1 &&
            segments.lastOrNull() == "fans" &&
            segments.getOrNull(subscribeIndex + 1)?.toLongOrNull() != null
    }

    fun diaryId(deepLink: String): Long? {
        if (!isDiaryDeepLink(deepLink)) return null
        val segments = pathSegments(deepLink)
        val i = segments.indexOf("diaries")
        return segments.getOrNull(i + 1)?.toLongOrNull()
    }

    private fun pathSegments(deepLink: String): List<String> {
        if (deepLink.isBlank()) return emptyList()
        val uri = Uri.parse(
            if (deepLink.startsWith("http")) deepLink
            else "https://local${if (deepLink.startsWith("/")) deepLink else "/$deepLink"}"
        )
        return uri.pathSegments.orEmpty()
    }
}
