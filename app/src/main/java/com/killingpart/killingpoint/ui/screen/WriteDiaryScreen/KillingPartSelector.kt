package com.killingpart.killingpoint.ui.screen.WriteDiaryScreen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.killingpart.killingpoint.R
import com.killingpart.killingpoint.ui.theme.mainGreen
import kotlin.math.roundToInt

@Composable
fun KillingPartSelector(
    totalDuration: Int,
    selectedDuration: Int,
    onStartChange: (Float) -> Unit
) {
    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    val totalBars = totalDuration * 2
    val barWidth = 6.dp
    val gap = 12.dp
    val gapPx = with(density) { gap.toPx() }

    val leftBoxWidth = 24.dp
    val rightBoxWidth = 24.dp
    val spaceBetweenBoxes = 170.dp
    val greenBoxTotalWidth = leftBoxWidth + spaceBetweenBoxes + rightBoxWidth // 218.dp

    val barHeights = remember(totalBars) {
        (0 until totalBars).map { (30..70).random().dp }
    }

    var parentWidthPx by remember { mutableStateOf(0f) }

    val barWidthPx = with(density) { barWidth.toPx() }
    val barSpacingPx = (barWidthPx + gapPx).roundToInt().toFloat()
    val leftBoxWidthPx = with(density) { leftBoxWidth.toPx() }
    val spaceBetweenBoxesPx = with(density) { spaceBetweenBoxes.toPx() }
    val greenBoxTotalWidthPx = with(density) { greenBoxTotalWidth.toPx() }
    
    // 초록색 박스의 왼쪽 끝과 바 시작 위치를 맞추기 위한 초기 Spacer 너비 (픽셀 단위로 정확히 계산)
    val initialSpacerWidthPx = remember(parentWidthPx, greenBoxTotalWidthPx) {
        if (parentWidthPx > 0f) {
            parentWidthPx / 2f - greenBoxTotalWidthPx / 2f
        } else {
            0f
        }
    }
    
    val initialSpacerWidthDp = with(density) { initialSpacerWidthPx.toDp() }
    
    // 초록색 박스의 오른쪽 끝과 마지막 바를 맞추기 위한 마지막 Spacer 너비
    val finalSpacerWidthPx = remember(
        parentWidthPx,
        greenBoxTotalWidthPx,
        totalBars,
        barSpacingPx,
        barWidthPx,
        initialSpacerWidthPx
    ) {
        if (parentWidthPx > 0f) {
            val totalBarSectionPx =
                (totalBars * barWidthPx) + ((totalBars - 1) * gapPx)

            // Row 전체 폭에서 (initial spacer + 모든 bar들)을 뺀 나머지가 final spacer
            val totalRowContentPx = initialSpacerWidthPx + totalBarSectionPx
            val finalSpacer = (parentWidthPx - greenBoxTotalWidthPx) / 2f
            // 즉, 초록박스 좌우 여백이 같게
            finalSpacer.coerceAtLeast(0f)
        } else 0f
    }
    
    val finalSpacerWidthDp = with(density) { finalSpacerWidthPx.toDp() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(88.dp)
            .onGloballyPositioned { coordinates ->
                parentWidthPx = coordinates.size.width.toFloat()
            }
            .clipToBounds(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .wrapContentWidth()
                .height(88.dp)
                .horizontalScroll(scrollState),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 초록색 박스의 왼쪽 끝과 바 시작 위치를 맞추기 위한 Spacer
            if (parentWidthPx > 0f) {
                Spacer(modifier = Modifier.width(initialSpacerWidthDp))
            }
            
            repeat(totalBars) { i ->
                val currentScrollValue = scrollState.value.toFloat()
                
                // Row 내에서의 바 위치 (Spacer 포함)
                val barPositionInRowPx = initialSpacerWidthPx + i * barSpacingPx
                // 화면 좌표계에서의 바 위치 (Row의 시작은 부모의 왼쪽 끝, 스크롤 오프셋만큼 이동)
                val barLeftPx = barPositionInRowPx - currentScrollValue
                val barRightPx = barLeftPx + barWidthPx

                val greenBoxCenterPx = parentWidthPx / 2f
                // 초록색 박스의 왼쪽 박스 오른쪽 끝 = 초록색 박스 시작 + 왼쪽 박스 너비
                val greenBoxLeftBoxRightPx = greenBoxCenterPx - greenBoxTotalWidthPx / 2f + leftBoxWidthPx
                // 초록색 박스의 오른쪽 박스 왼쪽 끝 = 초록색 박스 시작 + 왼쪽 박스 너비 + 중간 영역 너비
                val greenBoxRightBoxLeftPx = greenBoxCenterPx - greenBoxTotalWidthPx / 2f + leftBoxWidthPx + spaceBetweenBoxesPx

                // 중간 영역(투명한 부분)에만 흰색으로 표시 - 바가 중간 영역과 겹치면 흰색
                val isInsideGreenBox = barLeftPx < greenBoxRightBoxLeftPx && barRightPx > greenBoxLeftBoxRightPx

                Box(
                    modifier = Modifier
                        .width(barWidth)
                        .height(barHeights[i])
                        .background(
                            if (isInsideGreenBox) Color(0xFFFAFAFA) else Color(0xFF454545),
                            RoundedCornerShape(3.dp)
                        )
                )

                if (i != totalBars - 1) {
                    Spacer(modifier = Modifier.width(gap))
                }
            }
            
            // 초록색 박스의 오른쪽 끝과 마지막 바를 맞추기 위한 Spacer
            if (parentWidthPx > 0f && finalSpacerWidthPx > 0f) {
                Spacer(modifier = Modifier.width(finalSpacerWidthDp))
            }
        }

        Row(
            modifier = Modifier
                .width(greenBoxTotalWidth)
                .height(88.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 왼쪽 박스
            Box(
                modifier = Modifier
                    .width(24.dp)
                    .fillMaxHeight()
                    .background(mainGreen, RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp))
                    .border(2.dp, mainGreen, RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.move),
                    contentDescription = "left",
                    modifier = Modifier.size(7.dp, 13.dp)
                )
            }

            // 중간 영역 (배경 보이도록 투명)
            Box(
                modifier = Modifier
                    .width(170.dp)
                    .fillMaxHeight()
                    .background(Color.Transparent)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 2.dp.toPx()
                    drawLine(
                        color = mainGreen,
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        strokeWidth = strokeWidth
                    )
                    drawLine(
                        color = mainGreen,
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = strokeWidth
                    )
                }
            }

            // 오른쪽 박스
            Box(
                modifier = Modifier
                    .width(24.dp)
                    .fillMaxHeight()
                    .background(mainGreen, RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                    .border(2.dp, mainGreen, RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.move),
                    contentDescription = "right",
                    modifier = Modifier
                        .size(7.dp, 13.dp)
                        .rotate(180f)
                )
            }
        }
    }

    // 스크롤에 따른 startSeconds 계산
    LaunchedEffect(scrollState.value, parentWidthPx) {
        if (parentWidthPx > 0f) {
            val currentScrollValue = scrollState.value.toFloat()

            val greenBoxCenterPx = parentWidthPx / 2f
            val greenBoxLeftBoxRightPx =
                greenBoxCenterPx - greenBoxTotalWidthPx / 2f + leftBoxWidthPx

            // Row 전체 실제 길이 (초기 Spacer + 바들 + 마지막 Spacer)
            val totalRowWidthPx =
                initialSpacerWidthPx +
                        (totalBars * barWidthPx) +
                        ((totalBars - 1) * gapPx) +
                        finalSpacerWidthPx

            // 초록박스 시작 좌표 → Row 좌표로 변환
            val visibleStartInRowPx = currentScrollValue + greenBoxLeftBoxRightPx

            // 전체 길이에 맞춰 비율 보정 (끝부분 오차 제거)
            val normalized =
                ((visibleStartInRowPx - initialSpacerWidthPx) / (totalRowWidthPx - initialSpacerWidthPx))
                    .coerceIn(0f, 1f)

            // barIndex는 0~(totalBars-1) 사이에서 보간
            val barIndex = normalized * (totalBars - 1)

            val startSeconds = barIndex / 2f
            onStartChange(startSeconds)
        }
    }
}

@Preview
@Composable
fun KillingPartSelectorPreview() {
    KillingPartSelector(185, 14, {})
}
