package com.killingpart.killingpoint.ui.screen.MainScreen


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import com.killingpart.killingpoint.ui.theme.mainGreen
import com.killingpart.killingpoint.R

@Composable
fun RunMusicBox() {
    Column (
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 35.dp)
            .background(Color.Black, RoundedCornerShape(8.dp))
    ){

        Row (
            modifier = Modifier.padding(start = 71.dp, end = 17.dp, top = 8.dp, bottom = 8.dp)
        ){
            Text(
            text = "@KILLINGPART",
            fontFamily = PaperlogyFontFamily,
            fontWeight = FontWeight.Thin,
            fontSize = 14.sp,
            color = mainGreen,
        )
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier.size(41.dp, 16.dp)
                    .background(color = Color(0xFF212123), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.add),
                    contentDescription = "킬링파트 추가 버튼",
                    modifier = Modifier.size(12.dp, 8.dp)
                )
            }
        }

        Column (
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Image(
                painter = painterResource(id = R.drawable.example_video),
                contentDescription = "유튜브 영상 들어가는 곳",
                modifier = Modifier.fillMaxWidth().height(207.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            MusicTimeBar("사랑한단 말의 뜻을 알아가자", 102, 28, 180)

            Spacer(modifier = Modifier.height(12.dp))

            MusicCueBtn()
            
            Spacer(modifier = Modifier.height(24.dp))

            Row (
                modifier = Modifier.fillMaxWidth().height(41.dp).padding(horizontal = 18.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(13.dp)
            ){
                Text(
                    text = "다음곡 : ",
                    fontSize = 14.sp,
                    fontFamily = PaperlogyFontFamily,
                    fontWeight = FontWeight.Light,
                    color = mainGreen
                )

                Text(
                    text = "다음곡은 뭘까요",
                    fontSize = 14.sp,
                    fontFamily = PaperlogyFontFamily,
                    fontWeight = FontWeight.Light,
                    color = Color.White
                )
            }
        }
    }
}

@Preview
@Composable
fun RunBoxPreview() {
    RunMusicBox()
}