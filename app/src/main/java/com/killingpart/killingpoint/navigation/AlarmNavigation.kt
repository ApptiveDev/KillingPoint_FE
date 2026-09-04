package com.killingpart.killingpoint.navigation

import android.net.Uri
import androidx.navigation.NavController
import com.killingpart.killingpoint.analytics.NotificationAnalytics
import com.killingpart.killingpoint.data.model.AlarmDeepLink
import com.killingpart.killingpoint.data.model.DiaryDetail
import com.killingpart.killingpoint.data.model.Scope
import com.killingpart.killingpoint.data.repository.AuthRepository

fun NavController.navigateToDiaryDetailFromAlarm(
    detail: DiaryDetail,
    myUserId: Long?,
    entryPoint: String? = null,
    notificationType: String? = null
) {
    val diary = detail.toDiary()
    val isOwnDiary = myUserId != null && detail.userId == myUserId
    val authorParams = if (!isOwnDiary && detail.username.isNotBlank() && detail.tag.isNotBlank()) {
        "&authorUsername=${Uri.encode(detail.username)}&authorTag=${Uri.encode(detail.tag)}"
    } else ""
    val diaryIdParam = diary.id?.let { "&diaryId=$it" }.orEmpty()
    val totalDurationParam = diary.totalDuration?.let { "&totalDuration=$it" }.orEmpty()
    val notifParams = (entryPoint?.let { "&notifEntryPoint=${Uri.encode(it)}" }.orEmpty()) +
        (notificationType?.let { "&notifType=${Uri.encode(it)}" }.orEmpty())
    val displayContent = if (diary.scope == Scope.PRIVATE) "비공개 일기입니다." else diary.content
    navigate(
        "diary_detail" +
            "?artist=${Uri.encode(diary.artist)}" +
            "&musicTitle=${Uri.encode(diary.musicTitle)}" +
            "&albumImageUrl=${Uri.encode(diary.albumImageUrl)}" +
            "&content=${Uri.encode(displayContent)}" +
            "&videoUrl=${Uri.encode(diary.videoUrl)}" +
            "&duration=${Uri.encode(diary.duration)}" +
            "&start=${Uri.encode(diary.start)}" +
            "&end=${Uri.encode(diary.end)}" +
            "&createDate=${Uri.encode(diary.createDate)}" +
            "&scope=${diary.scope.name}" +
            diaryIdParam +
            totalDurationParam +
            "&fromTab=social" +
            notifParams +
            authorParams
    )
}

fun NavController.navigateFromSubscribeAlarm(deepLink: String, entryPoint: String? = null) {
    if (!AlarmDeepLink.isSubscribeFansDeepLink(deepLink)) return
    val entryParam = entryPoint?.let { "&notifEntryPoint=${Uri.encode(it)}" }.orEmpty()
    navigate("social?tab=friend&friendListTab=fans$entryParam")
}

suspend fun handleAlarmNavigation(
    navController: NavController,
    type: String,
    deepLink: String,
    repo: AuthRepository,
    entryPoint: String? = null,
    onError: ((String) -> Unit)? = null
) {
    when (type) {
        "LIKE_ALARM", "DIARY_ALARM" -> {
            repo.getDiaryDetailByAlarmDeepLink(deepLink).fold(
                onSuccess = { detail ->
                    val myUserId = repo.getUserIdFromToken()
                    navController.navigateToDiaryDetailFromAlarm(
                        detail,
                        myUserId,
                        entryPoint = entryPoint,
                        notificationType = NotificationAnalytics.NotificationType.fromAlarmType(type)
                    )
                },
                onFailure = { e ->
                    onError?.invoke(mapAlarmNavigationError(type, e.message))
                }
            )
        }
        "SUBSCRIBE_ALARM" -> {
            navController.navigateFromSubscribeAlarm(deepLink, entryPoint)
        }
    }
}

fun mapAlarmNavigationError(alarmType: String, rawMessage: String?): String {
    val message = rawMessage.orEmpty()
    val isNotFound = message.contains("404") || message.contains("HTTP 404", ignoreCase = true)
    if (!isNotFound) return rawMessage ?: "페이지를 불러오지 못했습니다."
    return when (alarmType) {
        "LIKE_ALARM", "DIARY_ALARM" -> "삭제되었거나 존재하지 않는 일기입니다."
        "SUBSCRIBE_ALARM" -> "탈퇴했거나 존재하지 않는 회원입니다."
        else -> "요청한 정보를 찾을 수 없습니다."
    }
}
