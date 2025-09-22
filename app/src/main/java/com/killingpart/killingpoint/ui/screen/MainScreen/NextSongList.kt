package com.killingpart.killingpoint.ui.screen.MainScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.killingpart.killingpoint.R
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import com.killingpart.killingpoint.ui.theme.mainGreen

@Composable
fun NextSongList(title: String? = null) {
    Row (
        modifier = Modifier.fillMaxWidth()
            .height(41.dp).padding(horizontal = 18.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ){
        Text(
            text = "다음곡 : ",
            fontSize = 14.sp,
            fontFamily = PaperlogyFontFamily,
            fontWeight = FontWeight.Light,
            color = mainGreen
        )

        Spacer(modifier = Modifier.width(13.dp))

        Text(
            text = title ?: "로딩 중...",
            fontSize = 14.sp,
            fontFamily = PaperlogyFontFamily,
            fontWeight = FontWeight.Light,
            color = Color.White
        )

        Spacer(modifier = Modifier.weight(1f))

        Image(
            painter = painterResource(id = R.drawable.music_list),
            contentDescription = "음악 리스트",
            modifier = Modifier.size(24.dp)
        )
    }
}

