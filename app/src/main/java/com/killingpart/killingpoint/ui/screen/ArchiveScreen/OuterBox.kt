package com.killingpart.killingpoint.ui.screen.ArchiveScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Surface
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.killingpart.killingpoint.R
import com.killingpart.killingpoint.data.model.Diary
import com.killingpart.killingpoint.ui.screen.ArchiveScreen.DiaryCard
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import com.killingpart.killingpoint.ui.theme.mainGreen
import com.killingpart.killingpoint.ui.viewmodel.UserUiState
import com.killingpart.killingpoint.ui.viewmodel.UserViewModel

@Composable
fun OuterBox(
    diaries: List<Diary>,
    modifier: Modifier = Modifier,
    onProfileClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val userViewModel: UserViewModel = viewModel()
    val userState by userViewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        userViewModel.loadUserInfo(context)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(color = Color.Black, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 20.dp))
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // 프로필 영역
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 프로필 사진과 이름
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 프로필 사진
                    when (val s = userState) {
                        is UserUiState.Success -> {
                            AsyncImage(
                                model = s.userInfo.profileImageUrl,
                                contentDescription = "프로필 사진",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(60.dp)
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
                                    .clip(RoundedCornerShape(50))
                                    .border(3.dp, mainGreen, RoundedCornerShape(50))
                            )
                        }
                    }

                    // username과 tag (클릭 가능)
                    Column(
                        modifier = Modifier.clickable { onProfileClick() }
                    ) {
                        Text(
                            text = when (val s = userState) {
                                is UserUiState.Success -> s.userInfo.username
                                is UserUiState.Loading -> "LOADING..."
                                is UserUiState.Error -> "KILLING_PART"
                            },
                            fontFamily = PaperlogyFontFamily,
                            fontWeight = FontWeight.W400,
                            fontSize = 14.sp,
                            color = mainGreen,
                        )
                        Text(
                            text = when (val s = userState) {
                                is UserUiState.Success -> "@${s.userInfo.tag}"
                                is UserUiState.Loading -> "@LOADING"
                                is UserUiState.Error -> "@KILLING_PART"
                            },
                            fontFamily = PaperlogyFontFamily,
                            fontWeight = FontWeight.W400,
                            fontSize = 14.sp,
                            color = mainGreen,
                        )
                    }
                }


            }

            Spacer(modifier = Modifier.height(20.dp))

            // 다이어리 그리드 (2x2가 한 화면에 보이도록 높이 계산)
            val configuration = LocalConfiguration.current
            val screenWidth = configuration.screenWidthDp.dp
            val horizontalContainerPadding = 20.dp // Box padding
            val interColumnSpacing = 12.dp
            val rowSpacing = 20.dp
            val itemSize = (screenWidth - horizontalContainerPadding * 2 - interColumnSpacing) / 2

            val chunkedDiaries = diaries.chunked(2)
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemSize * 2 + rowSpacing)
            ) {
                items(chunkedDiaries.size) { index ->
                    val rowItems = chunkedDiaries[index]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = rowSpacing),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowItems.forEach { diary ->
                            DiaryCard(
                                diary = diary,
                                modifier = Modifier
                                    .weight(1f)
                            )
                        }
                        // 홀수 개일 경우 빈 공간 추가
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OuterBoxPreview() {
    Surface(
        color = Color(0xFF060606)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            val mockDiaries = listOf(
                Diary(
                    artist = "Michael Jackson",
                    musicTitle = "Xscape",
                    albumImageUrl = "https://i.scdn.co/image/ab67616d0000b27375cc718da9eb0b39bd9cbfb3",
                    content = "목데이터1",
                    videoUrl = "https://www.youtube-nocookie.com/embed/ki08IcGubwQ",
                    scope = com.killingpart.killingpoint.data.model.Scope.PUBLIC,
                    duration = "string",
                    start = "string",
                    end = "string",
                    createDate = "1999.12.12",
                    updateDate = "string"
                ),
                Diary(
                    artist = "The Notorious B.I.G.",
                    musicTitle = "Ready to Die",
                    albumImageUrl = "https://i.scdn.co/image/ab67616d0000b27375cc718da9eb0b39bd9cbfb3",
                    content = "목데이터2",
                    videoUrl = "https://www.youtube-nocookie.com/embed/ki08IcGubwQ",
                    scope = com.killingpart.killingpoint.data.model.Scope.PUBLIC,
                    duration = "string",
                    start = "string",
                    end = "string",
                    createDate = "1999.12.12",
                    updateDate = "string"
                ),
                Diary(
                    artist = "Artist 3",
                    musicTitle = "Title 3",
                    albumImageUrl = "https://i.scdn.co/image/ab67616d0000b27375cc718da9eb0b39bd9cbfb3",
                    content = "목데이터3",
                    videoUrl = "https://www.youtube-nocookie.com/embed/ki08IcGubwQ",
                    scope = com.killingpart.killingpoint.data.model.Scope.PUBLIC,
                    duration = "string",
                    start = "string",
                    end = "string",
                    createDate = "1999.12.12",
                    updateDate = "string"
                ),
                Diary(
                    artist = "Artist 4",
                    musicTitle = "Title 4",
                    albumImageUrl = "https://i.scdn.co/image/ab67616d0000b27375cc718da9eb0b39bd9cbfb3",
                    content = "목데이터4",
                    videoUrl = "https://www.youtube-nocookie.com/embed/ki08IcGubwQ",
                    scope = com.killingpart.killingpoint.data.model.Scope.PUBLIC,
                    duration = "string",
                    start = "string",
                    end = "string",
                    createDate = "1999.12.12",
                    updateDate = "string"
                )
            )
            OuterBox(diaries = mockDiaries)
        }
    }
}
