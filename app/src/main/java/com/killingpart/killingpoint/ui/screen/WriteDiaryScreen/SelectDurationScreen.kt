package com.killingpart.killingpoint.ui.screen.WriteDiaryScreen

import android.R.attr.fontWeight
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Language
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.killingpart.killingpoint.data.model.CreateDiaryRequest
import com.killingpart.killingpoint.data.repository.AuthRepository
import com.killingpart.killingpoint.data.spotify.SimpleTrack
import com.killingpart.killingpoint.R
import com.killingpart.killingpoint.ui.screen.AddMusicScreen.korean_font_medium
import com.killingpart.killingpoint.ui.screen.MainScreen.AlbumDiaryBox
import com.killingpart.killingpoint.data.model.Diary
import com.killingpart.killingpoint.ui.component.BottomBar
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


@Composable
fun SelectDurationScreen(
    navController: NavController,
    title: String,
    artist: String,
    imageUrl: String
) {

    // Todo : 자동 스크롤 및 유튜브 플레이어 추가


    var duration by remember { mutableStateOf("10") }
    var start by remember { mutableStateOf("0") }
    var end = start + duration

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF060606))
    ) {

        Column(
            modifier = Modifier
                .weight(1f) // ← 여기!
                .fillMaxWidth()
                .padding(horizontal = 15.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Top bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        modifier = Modifier.size(50.dp),
                        imageVector = Icons.Filled.KeyboardArrowLeft,
                        contentDescription = "back",
                        tint = Color.White
                    )
                }
                Text(
                    text = "Killing Part",
                    fontSize = 33.sp,
                    fontFamily = eng_font_extrabold,
                    color = Color(0xFF1D1E20),
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            AlbumDiaryBoxWithoutContent(
                track = SimpleTrack(
                    title = title,
                    artist = artist,
                    albumImageUrl = imageUrl
                )
            )

            Spacer(modifier = Modifier.height(38.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "킬링파트 자르기",
                    fontFamily = PaperlogyFontFamily,
                    fontWeight = FontWeight.Light,
                    fontSize = 14.sp,
                    color = Color(0xFFEBEBEB)
                )
                Spacer(Modifier.height(18.dp))

                // Todo : 구간 슬라이드 구현
                Spacer(Modifier.height(80.dp))

                Spacer(Modifier.height(38.dp))

                // Todo : 구간 설정 슬라이드
                Text(
                    text = "킬링파트 길이 설정",
                    fontFamily = PaperlogyFontFamily,
                    fontWeight = FontWeight.Light,
                    fontSize = 14.sp,
                    color = Color(0xFFEBEBEB)
                )
            }
        }


        Button(
            onClick = {
                val encodedDuration = Uri.encode(duration)
                val encodedStart = Uri.encode(start)
                val encodedEnd = Uri.encode(end)
                navController.navigate(
                    "write_diary" +
                            "?title=${Uri.encode(title)}" +
                            "&artist=${Uri.encode(artist)}" +
                            "&image=${Uri.encode(imageUrl)}" +
                            "&duration=$encodedDuration" +
                            "&start=$encodedStart" +
                            "&end=$encodedEnd"
                )
            },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .align(Alignment.CenterHorizontally),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFCCFF33),
                contentColor = Color.Black
            )
        ) {
            Text(
                fontFamily = korean_font_medium,
                text = "일기작성 →"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))


        BottomBar(navController = navController)
    }
}
@Preview
@Composable
fun SelectDurationPreview() {
    SelectDurationScreen(
        navController = rememberNavController(),
        title = "Death Sonnet von Dat",
        artist = "Davinci Leo",
        imageUrl = "https://i.scdn.co/image/ab67616d00001e02c6b31f5f1ce2958380fdb9b0"
    )
}


