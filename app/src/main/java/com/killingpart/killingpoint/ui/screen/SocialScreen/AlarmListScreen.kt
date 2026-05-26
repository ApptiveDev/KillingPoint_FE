package com.killingpart.killingpoint.ui.screen.SocialScreen

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.killingpart.killingpoint.data.model.AlarmDeepLink
import com.killingpart.killingpoint.data.model.DiaryDetail
import com.killingpart.killingpoint.data.model.Scope
import com.killingpart.killingpoint.data.repository.AuthRepository
import com.killingpart.killingpoint.R
import com.killingpart.killingpoint.ui.component.AppBackground
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import com.killingpart.killingpoint.ui.theme.mainGreen
import com.killingpart.killingpoint.ui.viewmodel.AlarmUiState
import com.killingpart.killingpoint.ui.viewmodel.AlarmViewModel
import kotlinx.coroutines.launch

@Composable
fun AlarmListScreen(navController: NavController) {
    val alarmViewModel: AlarmViewModel = viewModel()
    val alarmState by alarmViewModel.state.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val repo = remember { AuthRepository(context) }
    val coroutineScope = rememberCoroutineScope()
    var opening by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        alarmViewModel.loadAlarms(context)
    }

    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                alarmViewModel.loadAlarms(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(horizontal = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp)
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "뒤로가기",
                        tint = Color.White
                    )
                }

                Row(
                    modifier = Modifier.align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_bell),
                        contentDescription = "알림",
                        tint = mainGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "알림 목록",
                        color = mainGreen,
                        fontFamily = PaperlogyFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 18.sp
                    )
                }
            }

            when (val state = alarmState) {
                is AlarmUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = mainGreen)
                    }
                }
                is AlarmUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = state.message,
                            color = Color.White,
                            fontFamily = PaperlogyFontFamily
                        )
                    }
                }
                is AlarmUiState.Success -> {
                    if (state.alarms.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "알림 목록이 비어있습니다",
                                color = Color.White,
                                fontFamily = PaperlogyFontFamily
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 36.dp),
                            verticalArrangement = Arrangement.spacedBy(0.dp)
                        ) {
                            itemsIndexed(state.alarms, key = { _, alarm -> alarm.alarmId }) { index, alarm ->
                                val isNavigable = remember(alarm.alarmId, alarm.type, alarm.deepLink) {
                                    AlarmDeepLink.isNavigable(alarm.type, alarm.deepLink)
                                }
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable(enabled = isNavigable && !opening) {
                                                opening = true
                                                coroutineScope.launch {
                                                    try {
                                                        when (alarm.type) {
                                                            "LIKE_ALARM", "DIARY_ALARM" -> {
                                                                repo.getDiaryDetailByAlarmDeepLink(alarm.deepLink).fold(
                                                                    onSuccess = { detail ->
                                                                        val myUserId = repo.getUserIdFromToken()
                                                                        navigateToDiaryDetail(
                                                                            navController,
                                                                            detail,
                                                                            myUserId
                                                                        )
                                                                    },
                                                                    onFailure = { e ->
                                                                        Toast.makeText(
                                                                            context,
                                                                            mapAlarmNavigationErrorMessage(
                                                                                alarmType = alarm.type,
                                                                                rawMessage = e.message
                                                                            ),
                                                                            Toast.LENGTH_SHORT
                                                                        ).show()
                                                                    }
                                                                )
                                                            }

                                                            "SUBSCRIBE_ALARM" -> {
                                                                navigateFromSubscribeDeepLink(
                                                                    navController,
                                                                    alarm.deepLink
                                                                )
                                                            }
                                                        }
                                                    } finally {
                                                        opening = false
                                                    }
                                                }
                                            }
                                            .padding(vertical = 16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = alarm.content,
                                            color = Color.White,
                                            fontFamily = PaperlogyFontFamily,
                                            fontWeight = FontWeight.Normal,
                                            fontSize = 13.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Spacer(modifier = Modifier.size(12.dp))
                                        Text(
                                            text = formatAlarmDate(alarm.createDate),
                                            color = Color(0xFFA4A4A6),
                                            fontFamily = PaperlogyFontFamily,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 10.sp
                                        )
                                    }

                                    if (index != state.alarms.lastIndex) {
                                        HorizontalDivider(
                                            thickness = 1.dp,
                                            color = Color(0xFF2A2A2C),
                                            modifier = Modifier.height(1.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** `/api/subscribes/{userId}/fans` deepLink → 소셜 > 친구 > 팬덤 */
private fun navigateFromSubscribeDeepLink(navController: NavController, deepLink: String) {
    if (!AlarmDeepLink.isSubscribeFansDeepLink(deepLink)) return
    navController.navigate("social?tab=friend&friendListTab=fans")
}

private fun navigateToDiaryDetail(
    navController: NavController,
    detail: DiaryDetail,
    myUserId: Long?
) {
    val diary = detail.toDiary()
    val isOwnDiary = myUserId != null && detail.userId == myUserId
    val authorParams =
        if (!isOwnDiary && detail.username.isNotBlank() && detail.tag.isNotBlank()) {
            "&authorUsername=${Uri.encode(detail.username)}&authorTag=${Uri.encode(detail.tag)}"
        } else {
            ""
        }
    val diaryIdParam = diary.id?.let { "&diaryId=$it" }.orEmpty()
    val totalDurationParam = diary.totalDuration?.let { "&totalDuration=$it" }.orEmpty()
    val scopeParam = "&scope=${diary.scope.name}"
    val displayContent =
        if (diary.scope == Scope.PRIVATE) "비공개 일기입니다." else diary.content
    navController.navigate(
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
            scopeParam +
            diaryIdParam +
            totalDurationParam +
            "&fromTab=social" +
            authorParams
    )
}

private fun formatAlarmDate(raw: String?): String {
    if (raw.isNullOrBlank()) return ""
    return when {
        raw.length >= 10 && raw[4] == '-' && raw[7] == '-' -> {
            val month = raw.substring(5, 7)
            val day = raw.substring(8, 10)
            "$month.$day"
        }
        else -> raw
    }
}

private fun mapAlarmNavigationErrorMessage(
    alarmType: String,
    rawMessage: String?
): String {
    val message = rawMessage.orEmpty()
    val isNotFound = message.contains("404") || message.contains("HTTP 404", ignoreCase = true)
    if (!isNotFound) return rawMessage ?: "페이지를 불러오지 못했습니다."

    return when (alarmType) {
        "LIKE_ALARM", "DIARY_ALARM" -> "삭제되었거나 존재하지 않는 일기입니다."
        "SUBSCRIBE_ALARM" -> "탈퇴했거나 존재하지 않는 회원입니다."
        else -> "요청한 정보를 찾을 수 없습니다."
    }
}
