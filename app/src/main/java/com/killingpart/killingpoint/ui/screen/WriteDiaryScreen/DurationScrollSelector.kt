package com.killingpart.killingpoint.ui.screen.WriteDiaryScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import com.killingpart.killingpoint.ui.theme.mainGreen
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun DurationScrollSelector(
    selectedDuration: Int,
    onDurationChange: (Int) -> Unit
) {
    val scrollState = rememberScrollState()
    val itemWidth = 60.dp
    val durations = (10..20).toList()
    val highlightWidth = 186.dp
    
    val density = LocalDensity.current
    var initialized by remember { mutableStateOf(false) }
    var isInitializing by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .clipToBounds()
    ) {
        val highlightWidthPx = with(density) { highlightWidth.toPx() }
        val pxPerItem = with(density) { itemWidth.toPx() }
        val leadingSpacerPx = (highlightWidthPx / 2f) - (pxPerItem / 2f)

        // 스크롤 가능한 초 리스트
        Box(
            modifier = Modifier
                .width(highlightWidth)
                .fillMaxHeight()
                .align(Alignment.Center)
                .clipToBounds()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
            ) {
                // 양 끝 스페이서: 하이라이트 박스의 절반 - 항목 절반
                val sideSpacer = (highlightWidth / 2) - (itemWidth / 2)
                Spacer(modifier = Modifier.width(sideSpacer))

                durations.forEach { sec ->
                    Box(
                        modifier = Modifier
                            .width(itemWidth)
                            .height(46.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${sec}초",
                            fontSize = 16.sp,
                            color = if (sec == selectedDuration) Color(0xFFFAFAFA) else Color(0xFF454545),
                            fontFamily = PaperlogyFontFamily,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.width(sideSpacer))
            }
        }

        // 중앙 하이라이트 (초록색 divider 2줄)
        Box(
            modifier = Modifier
                .width(highlightWidth)
                .height(46.dp)
                .align(Alignment.Center)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .align(Alignment.Center),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(mainGreen)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(mainGreen)
                )
            }
        }

        // 1) 초기화: 10초가 가운데에 오고 선택된 상태
        LaunchedEffect(Unit) {
            if (!initialized) {
                isInitializing = true
                
                // 스크롤 상태가 준비될 때까지 대기
                kotlinx.coroutines.delay(100)
                
                // 항상 10초(인덱스 0)로 초기화
                val defaultIndex = 0
                val defaultDuration = durations[defaultIndex] // 10초
                
                // 10초가 중앙에 오도록 스크롤 위치 계산
                val targetOffset = (leadingSpacerPx + defaultIndex * pxPerItem)
                    .coerceIn(0f, scrollState.maxValue.toFloat())
                
                scrollState.scrollTo(targetOffset.toInt())
                
                // 10초로 확실히 설정 (selectedDuration 파라미터와 관계없이)
                onDurationChange(defaultDuration)
                
                // 스크롤 중 로직이 실행되지 않도록 충분히 대기 후 초기화 완료
                kotlinx.coroutines.delay(150)
                initialized = true
                isInitializing = false
            }
        }

        // 2) 스크롤 중: 중앙 기준 일정 범위 내의 초 선택
        LaunchedEffect(scrollState.value) {
            // 초기화가 완료되고 초기화 중이 아닐 때만 스크롤 중 로직 실행
            if (initialized && !isInitializing && scrollState.isScrollInProgress) {
                // 중앙 위치 계산
                val centerPx = scrollState.value + highlightWidthPx / 2f
                
                // 각 항목의 중앙 위치와 중앙으로부터의 거리를 계산
                val itemsWithDistance = durations.mapIndexed { index, duration ->
                    val itemCenterPx = leadingSpacerPx + index * pxPerItem + pxPerItem / 2f
                    val distance = abs(centerPx - itemCenterPx)
                    duration to distance
                }
                
                // 중앙에서 가장 가까운 항목 찾기
                val nearestItem = itemsWithDistance.minByOrNull { it.second }?.first
                    ?: durations.first()
                
                // 현재 선택된 항목의 중앙 위치
                val currentIndex = durations.indexOf(selectedDuration).coerceAtLeast(0)
                val currentCenterPx = leadingSpacerPx + currentIndex * pxPerItem + pxPerItem / 2f
                val distanceFromCurrent = abs(centerPx - currentCenterPx)
                
                // 현재 선택된 항목이 중앙 기준 ±30dp(항목 너비의 절반) 범위 내에 있으면 유지
                val newDuration = if (distanceFromCurrent < pxPerItem / 2f) {
                    selectedDuration
                } else {
                    // 범위를 벗어났으면 가장 가까운 항목 선택
                    nearestItem
                }
                
                if (newDuration != selectedDuration) {
                    onDurationChange(newDuration)
                }
            }
        }
    }
}

@Preview
@Composable
fun DurationSelectorPreview() {
    var duration by remember { mutableStateOf(10) }
    DurationScrollSelector(duration) { duration = it }
}
