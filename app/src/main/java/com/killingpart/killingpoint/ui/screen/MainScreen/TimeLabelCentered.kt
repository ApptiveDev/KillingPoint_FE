package com.killingpart.killingpoint.ui.screen.MainScreen

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily

@Composable
fun BoxScope.TimeLabelCentered(
    text: String,
    xDp: Float,
    maxWidthDp: Float,
    fontSizeSp: Float = 10f
) {
    val textMeasurer = rememberTextMeasurer()
    val style = androidx.compose.ui.text.TextStyle(
        fontSize = fontSizeSp.sp,
        color = Color.White,
        fontFamily = PaperlogyFontFamily,
        fontWeight = FontWeight.Thin
    )

    // 텍스트 실제 폭 측정(px)
    val textLayout = textMeasurer.measure(
        text = text,
        style = style
    )
    val density = LocalDensity.current
    val textWidthDp = with(density) { textLayout.size.width.toDp() }.value

    // 중앙 정렬: x - (텍스트폭/2)
    var leftDp = xDp - textWidthDp / 2f

    // 좌우 경계 보정 (0 ~ maxWidth - textWidth)
    if (leftDp < 0f) leftDp = 0f
    val maxLeft = maxWidthDp - textWidthDp
    if (leftDp > maxLeft) leftDp = maxLeft

    Text(
        text = text,
        style = style,
        maxLines = 1,
        overflow = TextOverflow.Clip,
        modifier = Modifier
            .align(Alignment.BottomStart)
            .absoluteOffset(x = leftDp.dp)
    )
}
