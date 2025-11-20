package com.killingpart.killingpoint.ui.screen.WriteDiaryScreen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import androidx.compose.ui.zIndex
import com.killingpart.killingpoint.R
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import com.killingpart.killingpoint.ui.theme.mainGreen
import kotlin.math.roundToInt

fun formatTime(seconds: Float): String {
    val totalSeconds = seconds.toInt().coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val secs = totalSeconds % 60
    return String.format("%02d:%02d", minutes, secs)
}

@Composable
fun KillingPartSelector(
    totalDuration: Int,
    onStartChange: (start: Float, end: Float, duration: Float) -> Unit
) {
    val scrollState = rememberScrollState()
    val density = LocalDensity.current

    // 바 1개 = 1초
    val barWidth = 6.dp
    val gap = 8.dp
    val barWidthPx = with(density) { barWidth.toPx() }
    val gapPx = with(density) { gap.toPx() }
    val pxPerSecond = barWidthPx + gapPx

    val timelineWidthPx = totalDuration * pxPerSecond
    val timelineWidthDp = with(density) { timelineWidthPx.toDp() }

    val minDurationSec = 10f
    val maxDurationSec = 30f.coerceAtMost(totalDuration.toFloat())

    var leftTime by remember { mutableStateOf(5f) }
    var rightTime by remember { mutableStateOf(15f) }

    val duration by remember {
        derivedStateOf { (rightTime - leftTime).coerceAtLeast(0f) }
    }

    val barHeights = remember(totalDuration) {
        (0 until totalDuration).map { (20..50).random().dp }
    }

    var previousScrollX by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .onGloballyPositioned {
            }
    ) {
        Row(
            modifier = Modifier
                .width(timelineWidthDp)
                .fillMaxHeight()
                .horizontalScroll(scrollState)
        ) {
            Canvas(
                modifier = Modifier
                    .width(timelineWidthDp)
                    .fillMaxHeight()
            ) {
                val scrollX = scrollState.value.toFloat()

                for (i in 0 until totalDuration) {
                    val barAbsX = i * pxPerSecond
                    val barVisibleX = barAbsX - scrollX

                    if (barVisibleX + barWidthPx < 0 || barVisibleX > size.width) continue

                    val barHeightPx = barHeights[i].toPx()
                    val top = (size.height - barHeightPx) / 2f

                    val barCenterAbsX = barAbsX + barWidthPx / 2f
                    val barCenterSec = barCenterAbsX / pxPerSecond
                    val inSelection = barCenterSec >= leftTime && barCenterSec <= rightTime

                    val color = if (inSelection) Color.White else Color(0xFF454545)

                    drawRoundRect(
                        color = color,
                        topLeft = Offset(barVisibleX, top),
                        size = Size(barWidthPx, barHeightPx),
                        cornerRadius = CornerRadius(6f, 6f)
                    )
                }
            }
        }

        val scrollX = scrollState.value.toFloat()
        val leftVisibleX by remember {
            derivedStateOf { (leftTime * pxPerSecond) - scrollX }
        }
        val rightVisibleX by remember {
            derivedStateOf { (rightTime * pxPerSecond) - scrollX }
        }

        val handleYOffsetPx = with(density) { 20.dp.toPx().roundToInt() }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .offset { IntOffset(leftVisibleX.roundToInt(), handleYOffsetPx) }
                .pointerInput(Unit) {
                    detectDragGestures { change, drag ->
                        change.consume()

                        val deltaSec = drag.x / pxPerSecond
                        var newLeft = leftTime + deltaSec

                        // 범위 clamp
                        if (newLeft < 0f) newLeft = 0f
                        if (rightTime - newLeft < minDurationSec)
                            newLeft = rightTime - minDurationSec
                        if (rightTime - newLeft > maxDurationSec)
                            newLeft = rightTime - maxDurationSec

                        leftTime = newLeft.coerceIn(0f, totalDuration.toFloat())
                    }
                }
                .zIndex(10f)
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
                text = formatTime(leftTime),
                fontFamily = PaperlogyFontFamily,
                fontWeight = FontWeight.W400,
                fontSize = 14.sp,
                color = Color.White
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .offset { IntOffset(rightVisibleX.roundToInt(), handleYOffsetPx) }
                .pointerInput(Unit) {
                    detectDragGestures { change, drag ->
                        change.consume()

                        val deltaSec = drag.x / pxPerSecond
                        var newRight = rightTime + deltaSec

                        if (newRight > totalDuration) newRight = totalDuration.toFloat()
                        if (newRight - leftTime < minDurationSec)
                            newRight = leftTime + minDurationSec
                        if (newRight - leftTime > maxDurationSec)
                            newRight = leftTime + maxDurationSec

                        rightTime = newRight.coerceIn(0f, totalDuration.toFloat())
                    }
                }
                .zIndex(10f)
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
                    contentDescription = "right",
                    modifier = Modifier.size(7.dp, 13.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = formatTime(rightTime),
                fontFamily = PaperlogyFontFamily,
                fontWeight = FontWeight.W400,
                fontSize = 14.sp,
                color = Color.White
            )
        }
    }

    LaunchedEffect(scrollState.value) {
        val newScrollX = scrollState.value.toFloat()
        val deltaPx = newScrollX - previousScrollX

        if (deltaPx != 0f) {
            val deltaSec = deltaPx / pxPerSecond
            val currentDuration = (rightTime - leftTime).coerceIn(minDurationSec, maxDurationSec)

            var newLeft = leftTime + deltaSec
            var newRight = rightTime + deltaSec

            if (newLeft < 0f) {
                newLeft = 0f
                newRight = currentDuration
            } else if (newRight > totalDuration) {
                newRight = totalDuration.toFloat()
                newLeft = newRight - currentDuration
            }

            leftTime = newLeft
            rightTime = newRight
        }

        previousScrollX = newScrollX
    }

    LaunchedEffect(leftTime, rightTime, duration) {
        onStartChange(leftTime, rightTime, duration)
    }
}

@Preview
@Composable
fun KillingPartSelectorPreview() {
    KillingPartSelector(185) { _, _, _ -> }
}
