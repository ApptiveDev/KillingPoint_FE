package com.killingpart.killingpoint.ui.screen.SocialScreen

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
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
import com.killingpart.killingpoint.data.model.Diary
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
    var openingDiaryId by remember { mutableStateOf<Long?>(null) }

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
                            verticalArrangement = Arrangement.spacedBy(26.dp)
                        ) {
                            items(state.alarms, key = { it.alarmId }) { alarm ->
                                val diaryId = parseDiaryIdFromDeepLink(alarm.deepLink)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(enabled = diaryId != null && openingDiaryId == null) {
                                            val id = diaryId ?: return@clickable
                                            openingDiaryId = id
                                            coroutineScope.launch {
                                                repo.getDiaryById(id).fold(
                                                    onSuccess = { diary ->
                                                        navigateToDiaryDetail(navController, diary)
                                                    },
                                                    onFailure = { e ->
                                                        Toast.makeText(
                                                            context,
                                                            e.message ?: "일기를 불러올 수 없습니다",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                )
                                                openingDiaryId = null
                                            }
                                        },
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = alarm.content,
                                        color = Color.White,
                                        fontFamily = PaperlogyFontFamily,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 14.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.size(12.dp))
                                    Text(
                                        text = formatAlarmDate(alarm.createDate),
                                        color = Color(0xFFA4A4A6),
                                        fontFamily = PaperlogyFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp
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

private fun parseDiaryIdFromDeepLink(deepLink: String): Long? {
    if (deepLink.isBlank()) return null
    val match = Regex("""diaries/(\d+)""").find(deepLink) ?: return null
    return match.groupValues[1].toLongOrNull()
}

private fun navigateToDiaryDetail(navController: NavController, diary: Diary) {
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
            "&fromTab=social"
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
