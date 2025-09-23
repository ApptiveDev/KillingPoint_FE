package com.killingpart.killingpoint.ui.screen.MainScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.killingpart.killingpoint.data.model.Diary
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import com.killingpart.killingpoint.ui.viewmodel.DiaryUiState
import com.killingpart.killingpoint.ui.viewmodel.DiaryViewModel

@Composable
fun MusicListBox(currentIndex: Int) {

    val diaryViewModel: DiaryViewModel = viewModel()
    val diaryState = diaryViewModel.state.collectAsState()
    val diaries = (diaryState.value as? DiaryUiState.Success)?.diaries ?: emptyList()

    Column (
        modifier = Modifier.size(380.dp, 678.dp)
            .background(color = Color.Black, shape = RoundedCornerShape(20.dp))
    ){
        Column (
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 15.dp)
        ) {
            val nextDiary = diaries.getOrNull(1)
            NextSongList(nextDiary?.musicTitle)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn (
                modifier = Modifier.fillMaxWidth()
                    .background(color = Color.Black)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (diaries.isEmpty()) {
                    item {
                        Text(
                            text = "플레이리스트를 불러오는 중...",
                            fontFamily = PaperlogyFontFamily,
                            fontWeight = FontWeight.Light,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                } else {
                    items(diaries.size) { index ->
                        val d = diaries[index]
                        MusicListOne(
                            imageUrl = d.albumImageUrl,
                            musicTitle = d.musicTitle,
                            artist = d.artist,
                            isNow = if (index == currentIndex) Color(0xFF060606) else Color.Transparent
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            MusicCueBtn()
        }
    }
}
