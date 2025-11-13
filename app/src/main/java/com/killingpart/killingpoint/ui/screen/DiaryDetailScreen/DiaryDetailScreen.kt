package com.killingpart.killingpoint.ui.screen.DiaryDetailScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.killingpart.killingpoint.R
import com.killingpart.killingpoint.ui.component.AppBackground
import com.killingpart.killingpoint.ui.component.BottomBar
import com.killingpart.killingpoint.ui.screen.DiaryDetailScreen.MusicTimeBarForDiaryDetail
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import com.killingpart.killingpoint.ui.theme.mainGreen
import com.killingpart.killingpoint.ui.viewmodel.UserViewModel
import com.killingpart.killingpoint.ui.viewmodel.UserUiState
import java.net.URLDecoder
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DiaryDetailScreen(
    navController: NavController,
    artist: String,
    musicTitle: String,
    albumImageUrl: String,
    content: String,
    videoUrl: String,
    duration: String,
    start: String,
    end: String,
    createDate: String,
    selectedDate: String = ""
) {
    val context = LocalContext.current
    val userViewModel: UserViewModel = viewModel()
    val userState by userViewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        userViewModel.loadUserInfo(context)
    }

    // duration, start, end를 초 단위 Int로 변환
    // duration은 킬링파트의 길이(during), end는 킬링파트의 끝 시간
    val startSeconds = parseTimeToSeconds(start)
    val endSeconds = parseTimeToSeconds(end)
    val duringSeconds = (endSeconds - startSeconds).coerceAtLeast(0)

    // 날짜 포맷팅 (2025.10.26 형식)
    val formattedDate = try {
        val date = LocalDate.parse(createDate.split("T")[0])
        date.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
    } catch (e: Exception) {
        createDate.split("T")[0]
    }

    AppBackground {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 상단 헤더
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 35.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { 
                        // MusicCalendarScreen으로 돌아가기 (CALENDAR 탭 선택 + 선택된 날짜 복원)
                        val selectedDateParam = if (selectedDate.isNotEmpty()) "&selectedDate=${android.net.Uri.encode(selectedDate)}" else ""
                        navController.navigate("main?tab=calendar$selectedDateParam") {
                            // 현재 diary_detail을 스택에서 제거하고 main으로 이동
                            popUpTo("main") { inclusive = false }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "뒤로 가기",
                        tint = Color.White
                    )
                }

                IconButton(
                    onClick = { /* TODO: 편집 기능 */ }
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "편집",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 음악 플레이어 섹션
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 앨범 아트
                    AsyncImage(
                        model = albumImageUrl,
                        contentDescription = "앨범 아트",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = R.drawable.example_video),
                        error = painterResource(id = R.drawable.example_video)
                    )

                    Spacer(modifier = Modifier.width(20.dp))

                    // 음악 정보
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = musicTitle,
                            color = Color.White,
                            fontFamily = PaperlogyFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            maxLines = 2
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = artist,
                            color = Color.White,
                            fontFamily = PaperlogyFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp,
                            maxLines = 1
                        )
                        // 진행 바
                        MusicTimeBarForDiaryDetail(
                            artist = artist,
                            musicTitle = musicTitle,
                            start = startSeconds,
                            during = duringSeconds
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 다이어리 콘텐츠 영역 - 남은 공간을 모두 차지
            Box(
                modifier = Modifier
                    .padding(horizontal = 24.dp) // margin
                    .padding(bottom = 40.dp)
                    .fillMaxWidth()
                    .weight(1f)
                    .background(
                        color = Color(0xFF1D1E20),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(20.dp) // padding
            ) {
                // 다이어리 텍스트 (스크롤 가능)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 60.dp) // 날짜/닉네임 공간 확보
                ) {
                    Text(
                        text = content,
                        color = Color.White,
                        fontFamily = PaperlogyFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        lineHeight = 30.sp
                    )
                }

                // 날짜와 닉네임을 우하단에 배치
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 20.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    // 날짜
                    Text(
                        text = formattedDate,
                        color = Color.White,
                        fontFamily = PaperlogyFontFamily,
                        fontWeight = FontWeight.W400,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 사용자명
                    Text(
                        text = when (val state = userState) {
                            is UserUiState.Success -> "@${state.userInfo.username}"
                            is UserUiState.Loading -> "@KILLINGPART"
                            is UserUiState.Error -> "@KILLINGPART"
                        },
                        color = Color.White,
                        fontFamily = PaperlogyFontFamily,
                        fontWeight = FontWeight.W400,
                        fontSize = 14.sp
                    )
                }
            }

            // 하단 네비게이션 바
            BottomBar(navController = navController)
        }
    }
}

/**
 * 시간 문자열을 초 단위로 변환
 * "MM:SS" 형식, 숫자 문자열(소수점 포함 가능)을 지원
 */
private fun parseTimeToSeconds(timeStr: String): Int {
    return try {
        if (timeStr.contains(":")) {
            // "MM:SS" 형식
            val parts = timeStr.split(":")
            if (parts.size == 2) {
                val minutes = parts[0].toIntOrNull() ?: 0
                val seconds = parts[1].toIntOrNull() ?: 0
                minutes * 60 + seconds
            } else {
                0
            }
        } else {
            // 숫자 문자열 (초 단위, 소수점 포함 가능)
            // Float로 변환 후 Int로 반올림 (RunMusicBox와 동일한 방식)
            timeStr.toFloatOrNull()?.toInt() ?: 0
        }
    } catch (e: Exception) {
        0
    }
}

