package com.killingpart.killingpoint.ui.screen.WriteDiaryScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.materialcore.drawProgressBar
import com.killingpart.killingpoint.R

@Composable
fun KillingPartSelector(
    totalDuration: Int,
    selectedDuration: Int,
    onStartChange: (Float) -> Unit
) {
    val scrollState = rememberScrollState()
    val totalBars = totalDuration * 2
    val barWidth = 6.dp
    val gap = 12.dp

    Box(
        modifier = Modifier.fillMaxWidth().height(88.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.select_bar),
            modifier = Modifier.width(224.dp).height(88.dp),
            contentDescription = "킬링파트 선택 구간"
        )

        Row (
            modifier = Modifier.horizontalScroll(scrollState)
                .align(Alignment.Center)
        ){
            repeat(totalBars) { i ->
                val xOffset = scrollState.value.toFloat()
                val indexToSec = (i / 2f) // 0.5초 단위 변환
                val currentStart = xOffset / barWidth.value + gap.value / 2f
                val isInsideHightLight = indexToSec in currentStart..(currentStart + selectedDuration)

                Box(
                    modifier = Modifier
                        .width(barWidth)
                        .height((30..70).random().dp)
                        .padding(horizontal = gap / 2)
                        .background(
                            if (isInsideHightLight) Color.White else Color.Gray,
                            RoundedCornerShape(3.dp)
                        )
                )
            }
        }
    }

    LaunchedEffect(scrollState.value) {
        val startSeconds = scrollState.value / (barWidth.value + gap.value)
        onStartChange(startSeconds)
    }
}

@Preview
@Composable
fun KillingPartSelectorPreview() {
    KillingPartSelector(185, 14, {})
}