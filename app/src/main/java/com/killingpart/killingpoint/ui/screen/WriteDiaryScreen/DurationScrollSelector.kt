package com.killingpart.killingpoint.ui.screen.WriteDiaryScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.component1
import androidx.core.graphics.component2
import androidx.core.graphics.component3
import androidx.core.graphics.component4
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import com.killingpart.killingpoint.ui.theme.mainGreen
import kotlinx.coroutines.launch
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

    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val viewportWidth = with(density) {configuration.screenWidthDp.dp.toPx()}

    var initialized by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .width(186.dp)
            .height(46.dp)
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

        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .horizontalScroll(scrollState, enabled = true)
                .padding(horizontal = (itemWidth))
        ) {
            durations.forEach { sec ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 20.dp),
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
        }
    }

    LaunchedEffect(Unit) {
        if (!initialized) {
            val pxPerItem = with(density) { itemWidth.toPx() }
            val defaultIndex = durations.indexOf(selectedDuration).coerceAtLeast(0)
            val startOffset = (defaultIndex * pxPerItem - (viewportWidth / 2f - pxPerItem / 2)).coerceAtLeast(0f)
            scrollState.scrollTo(startOffset.toInt())
            initialized = true
        }
    }

    LaunchedEffect(scrollState.isScrollInProgress) {
        if (!scrollState.isScrollInProgress && initialized) {
            val pxPerItem = with(density) {itemWidth.toPx()}
            val centerPx = scrollState.value + viewportWidth / 2f

            val nearestIndex = durations.indices.minByOrNull { i ->
                val itemCenter = i * pxPerItem + pxPerItem / 2
                abs(centerPx - itemCenter)
            } ?: 0

            val newDuration = durations[nearestIndex]
            if (newDuration != selectedDuration) {
                onDurationChange(newDuration)
            }
        }
    }
}

@Preview
@Composable
fun DurationSelectorPreview() {
    DurationScrollSelector(10, {})
}