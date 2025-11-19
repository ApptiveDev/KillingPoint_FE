package com.killingpart.killingpoint.ui.screen.WriteDiaryScreen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.killingpart.killingpoint.R
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import com.killingpart.killingpoint.ui.theme.mainGreen
import kotlin.math.roundToInt

/**
 * duration과 startSeconds의 단위가 float이기 때문에
 * 포맷팅을 통해 Int 정수화 처리 후 출력
 */
fun formatTime(seconds: Float): String {
    val totalSeconds = seconds.toInt()
    val minutes = totalSeconds / 60
    val secs = totalSeconds % 60
    return String.format("%02d:%02d", minutes, secs)
}

@Composable
fun KillingPartSelector(
    totalDuration: Int,
    selectedDuration: Int,
    onStartChange: (Float) -> Unit
) {
    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    val totalBars = totalDuration
    val barWidth = 6.dp
    val gap = 8.dp
    val gapPx = with(density) { gap.toPx() }

    val leftBoxWidth = 16.dp
    val rightBoxWidth = 16.dp
    val spaceBetweenBoxes = 170.dp
    val greenBoxTotalWidth = leftBoxWidth + spaceBetweenBoxes + rightBoxWidth

    val barHeights = remember(totalBars) {
        (0 until totalBars).map { (20..50).random().dp }
    }

    var parentWidthPx by remember { mutableStateOf(0f) }

    val barWidthPx = with(density) { barWidth.toPx() }
    val barSpacingPx = (barWidthPx + gapPx).roundToInt().toFloat()
    val leftBoxWidthPx = with(density) { leftBoxWidth.toPx() }
    val spaceBetweenBoxesPx = with(density) { spaceBetweenBoxes.toPx() }
    val greenBoxTotalWidthPx = with(density) { greenBoxTotalWidth.toPx() }

    val initialSpacerWidthPx = remember(parentWidthPx, greenBoxTotalWidthPx) {
        if (parentWidthPx > 0f) {
            parentWidthPx / 2f - greenBoxTotalWidthPx / 2f
        } else {
            0f
        }
    }
    
    val initialSpacerWidthDp = with(density) { initialSpacerWidthPx.toDp() }

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

            val totalRowContentPx = initialSpacerWidthPx + totalBarSectionPx
            val finalSpacer = (parentWidthPx - greenBoxTotalWidthPx) / 2f
            finalSpacer.coerceAtLeast(0f)
        } else 0f
    }
    
    val finalSpacerWidthDp = with(density) { finalSpacerWidthPx.toDp() }

    val minDistancePx = 250f
    val timelineWidthPx = 350f
    val pxPerSecond = 20f
    val minDurationSec = 20f
    val maxDurationSec = 30f
    var currentStartSeconds by remember { mutableStateOf(0f) }
    var leftHandleX by remember { mutableStateOf(100f) }
    var rightHandleX by remember {mutableStateOf(100f + pxPerSecond * maxDurationSec)}

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { coordinates ->
                parentWidthPx = coordinates.size.width.toFloat()
            }
            .clipToBounds(),
        contentAlignment = Alignment.TopStart
    ) {
        Row(
            modifier = Modifier
                .wrapContentWidth()
                .height(88.dp)
                .horizontalScroll(scrollState),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (parentWidthPx > 0f) {
                Spacer(modifier = Modifier.width(initialSpacerWidthDp))
            }
            
            repeat(totalBars) { i ->
                val currentScrollValue = scrollState.value.toFloat()

                val barPositionInRowPx = initialSpacerWidthPx + i * barSpacingPx
                val barLeftPx = barPositionInRowPx - currentScrollValue
                val barRightPx = barLeftPx + barWidthPx

                val greenBoxCenterPx = parentWidthPx / 2f
                val greenBoxLeftBoxRightPx = greenBoxCenterPx - greenBoxTotalWidthPx / 2f + leftBoxWidthPx
                val greenBoxRightBoxLeftPx = greenBoxCenterPx - greenBoxTotalWidthPx / 2f + leftBoxWidthPx + spaceBetweenBoxesPx

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

            if (parentWidthPx > 0f && finalSpacerWidthPx > 0f) {
                Spacer(modifier = Modifier.width(finalSpacerWidthDp))
            }
        }
            Column (
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .offset { IntOffset(leftHandleX.roundToInt(), 0) }
                    .pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val tryX = leftHandleX + dragAmount.x
                            val durationIfMoved = (rightHandleX - tryX) / pxPerSecond

                            if (durationIfMoved >= minDurationSec && durationIfMoved <=maxDurationSec)
                                leftHandleX = tryX
                        }
                    )

                }
            ) {
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .height(70.dp)
                        .background(
                            mainGreen,
                            RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp)
                        )
                        .border(
                            2.dp,
                            mainGreen,
                            RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.move),
                        contentDescription = "left",
                        modifier = Modifier
                            .size(7.dp, 13.dp)
                            .rotate(180f)
                    )
                }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = formatTime(currentStartSeconds),
                        fontFamily = PaperlogyFontFamily,
                        fontWeight = FontWeight.W400,
                        fontSize = 14.sp,
                        color = Color.White
                    )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .offset { IntOffset(rightHandleX.roundToInt(), 0)}
                    .pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val tryX = rightHandleX + dragAmount.x
                            val durationIfMoved = (tryX - leftHandleX) / pxPerSecond

                            if (durationIfMoved >= minDurationSec && durationIfMoved <= maxDurationSec) {
                                rightHandleX = tryX
                            }
                        }
                    )
                }
            ) {
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .height(70.dp)
                        .background(
                            mainGreen,
                            RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp)
                        )
                        .border(
                            2.dp,
                            mainGreen,
                            RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.move),
                        contentDescription = "left",
                        modifier = Modifier
                            .size(7.dp, 13.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = formatTime(currentStartSeconds + selectedDuration),
                    fontFamily = PaperlogyFontFamily,
                    fontWeight = FontWeight.W400,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
        }


    LaunchedEffect(scrollState.value, parentWidthPx) {
        if (parentWidthPx > 0f) {
            val currentScrollValue = scrollState.value.toFloat()

            val greenBoxCenterPx = parentWidthPx / 2f
            val greenBoxLeftBoxRightPx =
                greenBoxCenterPx - greenBoxTotalWidthPx / 2f + leftBoxWidthPx

            val totalRowWidthPx =
                initialSpacerWidthPx +
                        (totalBars * barWidthPx) +
                        ((totalBars - 1) * gapPx) +
                        finalSpacerWidthPx

            val visibleStartInRowPx = currentScrollValue + greenBoxLeftBoxRightPx

            val normalized =
                ((visibleStartInRowPx - initialSpacerWidthPx) / (totalRowWidthPx - initialSpacerWidthPx))
                    .coerceIn(0f, 1f)

            val barIndex = normalized * (totalBars - 1)

            val startSeconds = barIndex / 2f

            currentStartSeconds = startSeconds
            onStartChange(startSeconds)
        }
    }
}

@Preview
@Composable
fun KillingPartSelectorPreview() {
    KillingPartSelector(185, 14, {})
}
