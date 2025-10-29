package com.killingpart.killingpoint.ui.screen.MainScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.materialcore.screenHeightDp
import com.killingpart.killingpoint.data.model.Diary
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import kotlinx.serialization.json.Json.Default.configuration
import androidx.compose.ui.platform.LocalConfiguration


@Composable
fun MusicListBox(
    currentIndex: Int,
    expanded: Boolean,
    onToggle: (Boolean) -> Unit,
    diaries: List<Diary>
)
{

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .animateContentSize()
    ) {
            val nextDiary = diaries.getOrNull(currentIndex + 1)

            NextSongList(
                title = nextDiary?.musicTitle,
                onToggle = { onToggle(!expanded) }
            )

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .background(color = Color.Black, shape = RoundedCornerShape(12.dp))
                        .padding(horizontal = 20.dp, vertical = 12.dp),
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
            }
    }
}
