package com.killingpart.killingpoint.ui.screen.DiaryDetailScreen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.killingpart.killingpoint.data.repository.AuthRepository
import com.killingpart.killingpoint.ui.screen.MainScreen.TimeLabelCentered
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import com.killingpart.killingpoint.ui.theme.mainGreen
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
fun MusicTimeBarForDiaryDetail(
    artist: String,
    musicTitle: String,
    start: Int,
    during: Int,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    var barSize by remember { mutableStateOf(IntSize.Zero) }
    
    // YouTube 비디오 전체 길이 (초 단위)
    var videoTotalDuration by remember { mutableStateOf<Int?>(null) }
    val repo = remember { AuthRepository(context) }
    
    // YouTube API에서 duration 가져오기
    LaunchedEffect(artist, musicTitle) {
        videoTotalDuration = null
        if (artist.isNotEmpty() && musicTitle.isNotEmpty()) {
            try {
                val videos = repo.searchVideos(artist, musicTitle)
                val firstVideo = videos.firstOrNull()
                firstVideo?.duration?.let { durationStr ->
                    val totalSeconds = parseDurationToSeconds(durationStr)
                    videoTotalDuration = totalSeconds
                    android.util.Log.d("MusicTimeBarForDiaryDetail", "YouTube video duration: $durationStr -> $totalSeconds seconds")
                }
            } catch (e: Exception) {
                android.util.Log.e("MusicTimeBarForDiaryDetail", "Failed to fetch video duration: ${e.message}")
                videoTotalDuration = null
            }
        }
    }
    
    // total은 YouTube API에서 가져온 비디오 전체 길이 사용, 없으면 기본값 180초
    val total = videoTotalDuration ?: 180

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { barSize = it.size },
                contentAlignment = Alignment.CenterStart
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                ) {
                    val h = size.height
                    val w = size.width

                    drawLine(
                        color = Color.White.copy(alpha = 0.85f),
                        start = Offset(0f, h / 2f),
                        end = Offset(w, h / 2f),
                        strokeWidth = with(density) { 2.dp.toPx() }
                    )

                    val startX = (start.toFloat() / total) * w
                    val endX = ((start + during).toFloat() / total) * w
                    
                    android.util.Log.d("MusicTimeBarForDiaryDetail", "Canvas drawing values:")
                    android.util.Log.d("MusicTimeBarForDiaryDetail", "  - start: $start, during: $during, total: $total")
                    android.util.Log.d("MusicTimeBarForDiaryDetail", "  - width: $w, height: $h")
                    android.util.Log.d("MusicTimeBarForDiaryDetail", "  - startX: $startX, endX: $endX")
                    
                    drawLine(
                        color = mainGreen,
                        start = Offset(startX, h / 2f),
                        end = Offset(endX, h / 2f),
                        strokeWidth = with(density) { 6.dp.toPx() },
                        cap = StrokeCap.Round
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            if (barSize.width > 0) {
                val barWidthPx = barSize.width.toFloat()
                val barWidthDp = with(density) { barWidthPx.toDp() }.value

                val x0 = 0f
                val xStart = (start.toFloat() / total) * barWidthDp
                val xEnd = ((start + during).toFloat() / total) * barWidthDp
                val xTotal = barWidthDp
                
                // 텍스트 측정을 위한 measurer
                val textMeasurer = rememberTextMeasurer()
                val textStyle = TextStyle(
                    fontSize = 10.sp,
                    fontFamily = PaperlogyFontFamily,
                    fontWeight = FontWeight.Thin
                )
                
                val startTimeText = formatTime(start)
                val endTimeText = formatTime(start + during)
                
                val startTextWidth = with(density) { 
                    textMeasurer.measure(startTimeText, textStyle).size.width.toDp().value 
                }
                val endTextWidth = with(density) { 
                    textMeasurer.measure(endTimeText, textStyle).size.width.toDp().value 
                }
                
                // 최소 간격 (텍스트 폭의 절반씩 + 여유 공간)
                val minSpacing = (startTextWidth + endTextWidth) / 2f + 8f
                val startEndDistance = kotlin.math.abs(xEnd - xStart)
                
                // 두 레이블이 겹치지 않도록 위치 조정
                val adjustedXStart = if (startEndDistance < minSpacing && xStart < xEnd) {
                    // start를 왼쪽으로, end를 오른쪽으로 밀어내기
                    xStart - (minSpacing - startEndDistance) / 2f
                } else {
                    xStart
                }
                
                val adjustedXEnd = if (startEndDistance < minSpacing && xStart < xEnd) {
                    xEnd + (minSpacing - startEndDistance) / 2f
                } else {
                    xEnd
                }

                Box(Modifier.fillMaxWidth().height(20.dp)) {
                    // 0 초
                    TimeLabelCentered("0", x0, barWidthDp)

                    // start
                    TimeLabelCentered(startTimeText, adjustedXStart, barWidthDp)
                    
                    // start + during
                    TimeLabelCentered(endTimeText, adjustedXEnd, barWidthDp)

                    // total
                    TimeLabelCentered(formatTime(total), xTotal, barWidthDp)
                }
            }
        }
    }
}

private fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%d:%02d".format(m, s)
}

