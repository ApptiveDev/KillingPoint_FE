package com.killingpart.killingpoint.ui.screen.WriteDiaryScreen

import android.R.attr.fontWeight
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Language
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.killingpart.killingpoint.data.model.CreateDiaryRequest
import com.killingpart.killingpoint.data.repository.AuthRepository
import com.killingpart.killingpoint.data.spotify.SimpleTrack
import com.killingpart.killingpoint.R
import com.killingpart.killingpoint.ui.screen.AddMusicScreen.korean_font_medium
import com.killingpart.killingpoint.ui.screen.MainScreen.AlbumDiaryBox
import com.killingpart.killingpoint.ui.screen.MainScreen.YouTubePlayerBox
import com.killingpart.killingpoint.ui.screen.WriteDiaryScreen.AlbumDiaryBoxWithoutContent
import com.killingpart.killingpoint.data.model.Diary
import com.killingpart.killingpoint.data.model.Scope
import com.killingpart.killingpoint.ui.component.BottomBar
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import java.time.LocalDate
import java.util.regex.Pattern

/**
 * ISO 8601 duration 형식(예: "PT2M28S")을 초 단위로 변환
 * @param duration ISO 8601 duration 문자열 (예: "PT2M28S", "PT1H2M30S", "PT30S")
 * @return 초 단위로 변환된 값 (예: 148, 3750, 30)
 */
fun parseDurationToSeconds(duration: String): Int {
    // PT 제거
    val durationStr = duration.removePrefix("PT")
    if (durationStr.isEmpty()) return 0
    
    var totalSeconds = 0
    
    // 시간(H) 파싱
    val hourPattern = Pattern.compile("(\\d+)H")
    val hourMatcher = hourPattern.matcher(durationStr)
    if (hourMatcher.find()) {
        totalSeconds += hourMatcher.group(1).toInt() * 3600
    }
    
    // 분(M) 파싱
    val minutePattern = Pattern.compile("(\\d+)M")
    val minuteMatcher = minutePattern.matcher(durationStr)
    if (minuteMatcher.find()) {
        totalSeconds += minuteMatcher.group(1).toInt() * 60
    }
    
    // 초(S) 파싱
    val secondPattern = Pattern.compile("(\\d+)S")
    val secondMatcher = secondPattern.matcher(durationStr)
    if (secondMatcher.find()) {
        totalSeconds += secondMatcher.group(1).toInt()
    }
    
    return totalSeconds
}

@Composable
fun SelectDurationScreen(
    navController: NavController,
    title: String,
    artist: String,
    imageUrl: String
) {
    var duration by remember { mutableStateOf("10") }
    var start by remember { mutableStateOf("0") }
    
    // start 값을 Float로 변환 (KillingPartSelector에서 받은 값)
    val startSeconds = remember(start) {
        val seconds = start.toFloatOrNull() ?: 0f
        android.util.Log.d("SelectDurationScreen", "startSeconds updated: $seconds (from start: $start)")
        seconds
    }
    
    // duration 값을 Float로 변환 (DurationScrollSelector에서 받은 값)
    val durationSeconds = remember(duration) {
        val seconds = duration.toFloatOrNull() ?: 10f
        android.util.Log.d("SelectDurationScreen", "durationSeconds updated: $seconds (from duration: $duration)")
        seconds
    }
    
    // end 값 계산: startSeconds + durationSeconds
    val end = remember(startSeconds, durationSeconds) {
        val endValue = (startSeconds + durationSeconds).toString()
        android.util.Log.d("SelectDurationScreen", "end calculated: $endValue (startSeconds: $startSeconds + durationSeconds: $durationSeconds)")
        endValue
    }

    var videoUrl by remember { mutableStateOf<String?>(null) }
    var totalDuration by remember { mutableStateOf(10) } // YouTube 비디오의 전체 길이 (초 단위)
    var isLoadingVideo by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val repo = remember { AuthRepository(context) }

    LaunchedEffect(title, artist) {
        isLoadingVideo = true
        try {
            val videos = repo.searchVideos(artist, title)
            val firstVideo = videos.firstOrNull()
            videoUrl = firstVideo?.url
            // duration 파싱하여 초 단위로 변환
            firstVideo?.duration?.let { durationStr ->
                val seconds = parseDurationToSeconds(durationStr)
                totalDuration = seconds
                android.util.Log.d("SelectDurationScreen", "Video duration: $durationStr -> $seconds seconds")
            } ?: run {
                totalDuration = 10 // 기본값
            }
            android.util.Log.d("SelectDurationScreen", "Search query: $artist - $title")
            android.util.Log.d("SelectDurationScreen", "Found ${videos.size} videos")
            android.util.Log.d("SelectDurationScreen", "First video URL: $videoUrl")
            android.util.Log.d("SelectDurationScreen", "First video title: ${firstVideo?.title}")
        } catch (e: Exception) {
            android.util.Log.e("SelectDurationScreen", "YouTube search failed: ${e.message}")
            videoUrl = null
            totalDuration = 10 // 기본값
        }
        isLoadingVideo = false
    }

    val scrollState = rememberScrollState()
    val density = LocalDensity.current

    // 비디오 URL이 변경되면 자동으로 아래로 스크롤 (KillingPartSelector 보이도록)
    LaunchedEffect(videoUrl) {
        if (videoUrl != null) {
            kotlinx.coroutines.delay(500) // 비디오 렌더링 대기
            android.util.Log.d("SelectDurationScreen", "Auto scrolling down - videoUrl: $videoUrl")
            val scrollOffset = with(density) { 300.dp.toPx().toInt() }
            scrollState.animateScrollTo(scrollOffset)
        }
    }
    
    // startSeconds 변경 시 위로 조금 스크롤하여 비디오 재렌더링 유도
    LaunchedEffect(startSeconds) {
        if (videoUrl != null && startSeconds > 0f) {
            kotlinx.coroutines.delay(100)
            android.util.Log.d("SelectDurationScreen", "startSeconds changed - scrolling up slightly: $startSeconds")
            // 위로 조금 스크롤 (비디오가 뷰포트를 벗어났다가 다시 들어오도록)
            val currentScroll = scrollState.value
            val scrollUpOffset = with(density) { 50.dp.toPx().toInt() }
            val targetScroll = (currentScroll - scrollUpOffset).coerceAtLeast(0)
            scrollState.animateScrollTo(targetScroll)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF060606))
    ) {

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 15.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Top bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        modifier = Modifier.size(50.dp),
                        imageVector = Icons.Filled.KeyboardArrowLeft,
                        contentDescription = "back",
                        tint = Color.White
                    )
                }
                Text(
                    text = "Killing Part",
                    fontSize = 33.sp,
                    fontFamily = eng_font_extrabold,
                    color = Color(0xFF1D1E20),
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                if (isLoadingVideo) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(Color(0xFF1A1A1A), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "YouTube 비디오 검색 중...",
                            fontFamily = PaperlogyFontFamily,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                } else if (videoUrl != null) {
                    val tempDiary = Diary(
                        artist = artist,
                        musicTitle = title,
                        albumImageUrl = imageUrl,
                        videoUrl = videoUrl!!,
                        content = "",
                        scope = Scope.PUBLIC,
                        duration = "0",
                        start = "0",
                        end = "0",
                        createDate = "",
                        updateDate = ""
                    )
                    YouTubePlayerBox(tempDiary, startSeconds, durationSeconds)
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(Color(0xFF1A1A1A), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "YouTube 비디오를 찾을 수 없습니다",
                            fontFamily = PaperlogyFontFamily,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                
                AlbumDiaryBoxWithoutContent(
                    track = SimpleTrack(
                        title = title,
                        artist = artist,
                        albumImageUrl = imageUrl
                    )
                )
                
                Spacer(modifier = Modifier.height(38.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "킬링파트 자르기",
                        fontFamily = PaperlogyFontFamily,
                        fontWeight = FontWeight.Light,
                        fontSize = 14.sp,
                        color = Color(0xFFEBEBEB)
                    )
                    Spacer(Modifier.height(18.dp))

                    KillingPartSelector(
                        totalDuration, duration.toInt(), {start = it.toString()}
                    )

                    Spacer(Modifier.height(38.dp))

                    Text(
                        text = "킬링파트 길이 설정",
                        fontFamily = PaperlogyFontFamily,
                        fontWeight = FontWeight.Light,
                        fontSize = 14.sp,
                        color = Color(0xFFEBEBEB)
                    )

                    Spacer(Modifier.height(12.dp))

                    DurationScrollSelector(duration.toInt(), {duration = it.toString()})
                }
                
                // 하단 패딩 (버튼 공간 확보)
                Spacer(modifier = Modifier.height(80.dp))
            }
        }


        Button(
            onClick = {
                val encodedDuration = Uri.encode(duration)
                val encodedStart = Uri.encode(start)
                val encodedEnd = Uri.encode(end)
                val encodedVideoUrl = Uri.encode(videoUrl ?: "")
                
                android.util.Log.d("SelectDurationScreen", "Navigating to writeDiaryScreen with:")
                android.util.Log.d("SelectDurationScreen", "  - duration: $duration (encoded: $encodedDuration)")
                android.util.Log.d("SelectDurationScreen", "  - start: $start (encoded: $encodedStart)")
                android.util.Log.d("SelectDurationScreen", "  - end: $end (encoded: $encodedEnd)")
                android.util.Log.d("SelectDurationScreen", "  - videoUrl: $videoUrl")
                
                navController.navigate(
                    "write_diary" +
                            "?title=${Uri.encode(title)}" +
                            "&artist=${Uri.encode(artist)}" +
                            "&image=${Uri.encode(imageUrl)}" +
                            "&duration=$encodedDuration" +
                            "&start=$encodedStart" +
                            "&end=$encodedEnd" +
                            "&videoUrl=$encodedVideoUrl"
                )
            },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .align(Alignment.CenterHorizontally),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFCCFF33),
                contentColor = Color.Black
            )
        ) {
            Text(
                fontFamily = korean_font_medium,
                text = "다음으로 →"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))


        BottomBar(navController = navController)
    }
}
@Preview
@Composable
fun SelectDurationPreview() {
    SelectDurationScreen(
        navController = rememberNavController(),
        title = "Death Sonnet von Dat",
        artist = "Davinci Leo",
        imageUrl = "https://i.scdn.co/image/ab67616d00001e02c6b31f5f1ce2958380fdb9b0"
    )
}


