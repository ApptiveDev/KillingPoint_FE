package com.killingpart.killingpoint.ui.screen.MainScreen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import com.killingpart.killingpoint.ui.theme.UnboundedFontFamily

@Composable
fun MainScreen() {
    Box (
        modifier = Modifier.fillMaxSize()
            .background(Color(0xFF1D1E20)),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color(0xFF060606),
                radius = size.minDimension * 0.8f,
                center = Offset(size.width * 0.1f, size.height * 0.37f)
            )

            drawCircle(
                color = Color(0xFF060606),
                radius = size.minDimension * 1.5f,
                center = Offset(size.width * 1.1f, size.height * 1.2f)
            )
        }

        Column (
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ){

            Spacer(modifier = Modifier.height(60.dp))
            Text(
                text = "MY MUSIC SPACE",
                color = Color.White,
                fontFamily = UnboundedFontFamily,
                fontWeight = FontWeight.Black,
                fontSize = 28.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "나만의 뮤직 스페이스",
                color = Color(0xFFA4A4A6),
                fontFamily = PaperlogyFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp

            )
        }

    }
}

@Preview
@Composable
fun MainScreenPreivew() {
    MainScreen()
}