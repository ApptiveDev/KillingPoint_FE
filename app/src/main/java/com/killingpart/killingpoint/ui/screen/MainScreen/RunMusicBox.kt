package com.killingpart.killingpoint.ui.screen.MainScreen

import android.os.Build

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import android.graphics.Shader
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.killingpart.killingpoint.R
import com.killingpart.killingpoint.data.model.Diary
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import com.killingpart.killingpoint.ui.theme.mainGreen
import com.killingpart.killingpoint.ui.viewmodel.UserUiState
import com.killingpart.killingpoint.ui.viewmodel.UserViewModel

@Composable
fun RunMusicBox(
    currentIndex: Int,
    currentDiary: Diary?
) {
    android.util.Log.d("RunMusicBox", "RunMusicBox called with index: $currentIndex, diary: ${currentDiary?.musicTitle}")
    val context = LocalContext.current
    val userViewModel: UserViewModel = viewModel()
    val userState by userViewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        userViewModel.loadUserInfo(context)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(start = 71.dp, end = 17.dp, top = 20.dp, bottom = 8.dp)
            ) {
                Text(
                    text = when (val s = userState) {
                        is UserUiState.Success -> "@ ${s.userInfo.username}"
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
                    modifier = Modifier
                        .size(41.dp, 16.dp)
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

            val listState = rememberLazyListState()
            LaunchedEffect(currentDiary) {
                if (currentDiary != null) listState.animateScrollToItem(1)
            }

            // currentDiary가 변경될 때마다 LazyColumn 전체 재생성
            key(currentDiary?.videoUrl) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) { 
                            android.util.Log.d("RunMusicBox", "About to call YouTubePlayerBox with: ${currentDiary?.musicTitle}")
                            YouTubePlayerBox(currentDiary, currentDiary?.start?.toFloatOrNull() ?: 0f)
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            AlbumDiaryBox(currentDiary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .size(316.dp, 80.dp)
                .offset(y = 370.dp)
                .background(color = Color.Transparent, shape = RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        compositingStrategy = CompositingStrategy.Offscreen
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            renderEffect = android.graphics.RenderEffect
                                .createBlurEffect(16f, 16f, Shader.TileMode.CLAMP)
                                .asComposeRenderEffect()
                        }
                    }
                    .background(Color.Black.copy(alpha = 0.2f))
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MusicTimeBar(
                    title = currentDiary?.musicTitle,
                    start = 102,
                    during = 28,
                    total = 180
                )
            }
        }

    }
}

