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
import com.killingpart.killingpoint.ui.component.AppBackground
import com.killingpart.killingpoint.ui.component.BottomBar
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import com.killingpart.killingpoint.ui.theme.UnboundedFontFamily

@Composable
fun MainScreen() {
    AppBackground {
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
            Spacer(modifier = Modifier.height(26.dp))


            Spacer(modifier = Modifier.weight(1f))
            BottomBar()
        }
    }
}

@Preview
@Composable
fun MainScreenPreivew() {
    MainScreen()
}