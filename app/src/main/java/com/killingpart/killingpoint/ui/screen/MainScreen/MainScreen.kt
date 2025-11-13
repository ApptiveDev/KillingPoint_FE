package com.killingpart.killingpoint.ui.screen.MainScreen

import android.view.RoundedCorner
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.killingpart.killingpoint.R
import com.killingpart.killingpoint.ui.component.AppBackground
import com.killingpart.killingpoint.ui.component.BottomBar
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import com.killingpart.killingpoint.ui.theme.UnboundedFontFamily
import com.killingpart.killingpoint.ui.theme.mainGreen
import com.killingpart.killingpoint.ui.viewmodel.DiaryUiState
import com.killingpart.killingpoint.ui.viewmodel.DiaryViewModel
import com.killingpart.killingpoint.ui.viewmodel.UserUiState
import com.killingpart.killingpoint.ui.viewmodel.UserViewModel
import com.killingpart.killingpoint.ui.screen.ArchiveScreen.DiaryCard
import com.killingpart.killingpoint.ui.screen.ArchiveScreen.OuterBox
import com.killingpart.killingpoint.ui.screen.MusicCalendarScreen.MusicCalendarScreen
import kotlinx.coroutines.launch

enum class MainTab {
    STORAGE, PLAY, CALENDAR
}
@Composable
fun MainScreen(navController: NavController) {
    var selected by remember { mutableStateOf(MainTab.PLAY) }
    var currentIndex by remember { mutableStateOf(0) }
    val mainListState = rememberLazyListState()
    
    // DiaryViewModel을 MainScreen에서 관리
    val diaryViewModel: DiaryViewModel = viewModel()
    val diaryState by diaryViewModel.state.collectAsState()
    val diaries = (diaryState as? DiaryUiState.Success)?.diaries ?: emptyList()
    
    // UserViewModel을 MainScreen에서 관리
    val userViewModel: UserViewModel = viewModel()
    val userState by userViewModel.state.collectAsState()

    val MusicCueBtnHeight = 60.dp
    val BottomBarHeight = 94.dp
    val MusicCueBtnGap = 12.dp

    var listExpanded by remember { mutableStateOf(false ) }

    val density = LocalDensity.current
    val innerTopPadding = 20.dp
    val pullUpOffsetPx = with(density) {innerTopPadding.roundToPx()}
    val listIndex = 1
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        diaryViewModel.loadDiaries(context)
        userViewModel.loadUserInfo(context)
        
        // WebView 캐시 초기화
        try {
            val webView = android.webkit.WebView(context)
            webView.clearCache(true)
            webView.clearHistory()
            webView.destroy()
            android.util.Log.d("MainScreen", "WebView cache cleared on startup")
        } catch (e: Exception) {
            android.util.Log.e("MainScreen", "Failed to clear WebView cache: ${e.message}")
        }
    }

    LaunchedEffect(listExpanded) {
        kotlinx.coroutines.delay(250)
        if (listExpanded) {
            mainListState.animateScrollToItem(1)
        } else {
            mainListState.animateScrollToItem(0)
        }
    }


    AppBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

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

                TopPillTabs(
                    options = listOf("내 프로필", "킬링파트 재생", "뮤직캘린더"),
                    selectedIndex = when (selected) {
                        MainTab.STORAGE -> 0
                        MainTab.PLAY -> 1
                        MainTab.CALENDAR -> 2
                    },
                    onSelected = { idx ->
                        selected = when (idx) {
                            0 -> MainTab.STORAGE
                            1 -> MainTab.PLAY
                            else -> MainTab.CALENDAR
                        }
                        listExpanded = false
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 30.dp)
                )

                Spacer(modifier = Modifier.height(15.dp))

                when (selected) {
                    MainTab.STORAGE -> {
                        when (val state = diaryState) {
                            is DiaryUiState.Loading -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = mainGreen)
                                }
                            }

                            is DiaryUiState.Success -> {

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .padding(horizontal = 16.dp)
                                ) {
                                    // 다이어리만 스크롤
                                    val musicListHeight = if (listExpanded) 260.dp else 80.dp
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(bottom = musicListHeight + MusicCueBtnHeight + MusicCueBtnGap + 16.dp)
                                    ) {
                                        item {
                                            OuterBox(diaries = state.diaries)
                                        }
                                    }
                                    // Overlay MusicListBox
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .fillMaxWidth()
                                    ) {
                                        MusicListBox(
                                            currentIndex = currentIndex,
                                            expanded = listExpanded,
                                            onToggle = { willOpen -> listExpanded = willOpen },
                                            diaries = state.diaries,
                                            showCurrentHeader = true
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(MusicCueBtnHeight + MusicCueBtnGap + 10.dp))
                            }

                            is DiaryUiState.Error -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = state.message,
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontFamily = PaperlogyFontFamily
                                    )
                                }
                            }
                        }
                    }

                    MainTab.PLAY -> {
                        LazyColumn(
                            state = mainListState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
//                            contentPadding = PaddingValues(bottom = BottomBarHeight + MusicCueBtnHeight + MusicCueBtnGap)
                            contentPadding = PaddingValues(bottom = BottomBarHeight)
                        ) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .background(color = Color.Black, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                                ) {
                                    RunMusicBox(
                                        currentIndex = currentIndex,
                                        currentDiary = diaries.getOrNull(currentIndex)
                                    )
                                }
                            }

                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 16.dp)
                                        .background(color = Color.Black, RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                                ) {
                                    MusicListBox(
                                        currentIndex = currentIndex,
                                        expanded = listExpanded,
                                        onToggle = { willOpen ->
                                            listExpanded = willOpen
                                        },
                                        diaries = diaries
                                    )
                                }
                            }
                        }
                    }
                    MainTab.CALENDAR -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            MusicCalendarScreen(diaries = diaries)
                        }
                    }
                }

                BottomBar(navController = navController)
            }

            MusicCueBtn(
                modifier = Modifier
                    .align (Alignment.BottomCenter)
                    .padding(bottom = BottomBarHeight + MusicCueBtnGap),
                onPrevious = {
                    if (currentIndex > 0) {
                        currentIndex--
                        android.util.Log.d("MainScreen", "Previous clicked, new index: $currentIndex")
                        // 비디오로 스크롤하지 않음 - 앨범 부분 유지
                    }
                },
                onNext = {
                    if (currentIndex < diaries.size - 1) {
                        currentIndex++
                        android.util.Log.d("MainScreen", "Next clicked, new index: $currentIndex")
                        // 비디오로 스크롤하지 않음 - 앨범 부분 유지
                    }
                }
            )

            // STORAGE 탭: 하단 고정 배치는 제거(리스트 내부로 복구)

            if (!listExpanded && selected == MainTab.PLAY) {
                when (val s = userState) {
                    is UserUiState.Success -> {
                        AsyncImage(
                            model = s.userInfo.profileImageUrl,
                            contentDescription = "프로필 사진",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(60.dp)
                                .align(Alignment.TopStart)
                                .offset(x = 20.dp, y = 220.dp)
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
                                .offset(x = 20.dp, y = 220.dp)
                                .clip(RoundedCornerShape(50))
                                .border(3.dp, mainGreen, RoundedCornerShape(50))
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun MainPreivew() {
    MainScreen(navController = rememberNavController())
}