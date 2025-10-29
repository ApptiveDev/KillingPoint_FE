package com.killingpart.killingpoint.ui.screen.MainScreen

import android.view.RoundedCorner
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import com.killingpart.killingpoint.ui.theme.mainGreen

@Composable
fun MusicTimeBar(
    title: String? = null,
    start: Int,
    during: Int,
    total: Int,
) {
    val density = LocalDensity.current
    var barSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color.Black.copy(alpha = 0.8f), shape = RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title ?: "로딩 중...",
                fontSize = 14.sp,
                fontFamily = PaperlogyFontFamily,
                fontWeight = FontWeight.Thin,
                color = Color.White
            )
            Spacer(Modifier.weight(1f))
        }

        Spacer(Modifier.height(8.dp))

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
                
                android.util.Log.d("MusicTimeBar", "Canvas drawing values:")
                android.util.Log.d("MusicTimeBar", "  - start: $start, during: $during, total: $total")
                android.util.Log.d("MusicTimeBar", "  - width: $w, height: $h")
                android.util.Log.d("MusicTimeBar", "  - startX: $startX, endX: $endX")
                android.util.Log.d("MusicTimeBar", "  - start ratio: ${start.toFloat() / total}, end ratio: ${(start + during).toFloat() / total}")
                
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

            Box(Modifier.fillMaxWidth().height(20.dp)) {
                // 0 초
                TimeLabelCentered("0", x0, barWidthDp)

                // start
                TimeLabelCentered(formatTime(start), xStart, barWidthDp)

                // start + during
                TimeLabelCentered(formatTime(start + during), xEnd, barWidthDp)

                // total
                TimeLabelCentered(formatTime(total), xTotal, barWidthDp)
            }
        }
        }
    }
}

@Composable
private fun BoxScope.TimeLabel(text: String, x: Float) {
    Text(
        text = text,
        fontSize = 7.sp,
        color = Color.White,
        fontFamily = PaperlogyFontFamily,
        fontWeight = FontWeight.Thin,
        modifier = Modifier
            .align(Alignment.BottomStart)
            .absoluteOffset(x.dp) // X 좌표에 배치
    )
}

private fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%d:%02d".format(m, s)
}


@Preview
@Composable
fun MusicTimeBarPreview() {
    MusicTimeBar("사랑한단 말의 뜻을 알아가자", 102, 28, 180)
}