package com.killingpart.killingpoint.ui.screen.MainScreen


import android.view.RoundedCorner
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import com.killingpart.killingpoint.ui.theme.mainGreen
import com.killingpart.killingpoint.ui.viewmodel.UserViewModel
import com.killingpart.killingpoint.ui.viewmodel.UserUiState
import com.killingpart.killingpoint.R

@Composable
fun RunMusicBox() {
    val context = LocalContext.current
    val userViewModel: UserViewModel = viewModel()
    val userState by userViewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        userViewModel.loadUserInfo(context)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 35.dp)
    ) {
        Column (
            modifier = Modifier.fillMaxWidth()
                .background(Color.Black, RoundedCornerShape(8.dp))
        ){

            Row (
                modifier = Modifier.padding(start = 71.dp, end = 17.dp, top = 8.dp, bottom = 8.dp)
            ){
                Text(
                    text = when (val currentState = userState) {
                        is UserUiState.Success -> "@ ${currentState.userInfo.username}"
                        is UserUiState.Loading -> "LOADING..."
                        is UserUiState.Error -> "KILLINGPART"
                    },
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
                YoutubeBox(
                    artist = "nct dream",
                    title = "미니카"
                )

                Spacer(modifier = Modifier.height(40.dp))

                MusicTimeBar("재생중인 곡", 102, 28, 180)

                Spacer(modifier = Modifier.height(12.dp))

                NextSongList("다음 재생할 곡")

                Spacer(modifier = Modifier.height(12.dp))

                MusicCueBtn()
            }
        }

        // 여기부터 프로필 사진 (오버레이)
        val currentUserState = userState
        when (currentUserState) {
            is UserUiState.Success -> {
                AsyncImage(
                    model = currentUserState.userInfo.profileImageUrl,
                    contentDescription = "프로필 사진",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(60.dp)
                        .align(Alignment.TopStart)
                        .offset(x = (-10).dp, y = (-10).dp)
                        .clip(RoundedCornerShape(50))
                        .border(3.dp, mainGreen, RoundedCornerShape(50)),
                    placeholder = painterResource(id = R.drawable.default_profile),
                    error = painterResource(id = R.drawable.default_profile)
                )
            }
            else -> {
                Image(
                    painter = painterResource(id = R.drawable.default_profile),
                    contentDescription = "프로필 사진",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(60.dp)
                        .align(Alignment.TopStart)
                        .offset(x = (-10).dp, y = (-10).dp)
                        .clip(RoundedCornerShape(50))
                        .border(3.dp, mainGreen, RoundedCornerShape(50))
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