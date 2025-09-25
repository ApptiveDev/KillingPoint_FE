package com.killingpart.killingpoint.ui.screen.WriteDiaryScreen

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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

val kor_font_medium = FontFamily(Font(R.font.paperlogy_medium))
val eng_font_extrabold = FontFamily(Font(R.font.unbounded_extrabold))

@Composable
fun WriteDiaryScreen(
    navController: NavController,
    title: String,
    artist: String,
    imageUrl: String
) {
    val coroutineScope = rememberCoroutineScope()
    var content by remember { mutableStateOf("") }
    var scope by remember { mutableStateOf("PUBLIC") }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val repo = remember { AuthRepository(context) }

    val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF060606))
    ) {
        // 본문 영역
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 15.dp)
                .weight(1f),
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

            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.fillMaxWidth(0.9f)) {
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.65f),
                    placeholder = {
                        Text(
                            text = "코멘트 추가…",
                            fontFamily = korean_font_medium,
                            color = Color(0xFFA4A4A6)
                        )
                    },
                    textStyle = TextStyle(fontSize = 16.sp, fontFamily = korean_font_medium),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF232427),
                        unfocusedContainerColor = Color(0xFF232427),
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                        cursorColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                Text(
                    text = today,
                    color = Color(0xFFA4A4A6),
                    fontSize = 12.sp,
                    fontFamily = korean_font_medium,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp, bottom = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(13.dp))

            // Scope header
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .clickable { isDropdownExpanded = !isDropdownExpanded },
            ) {
                Icon(
                    imageVector = Icons.Filled.Language,
                    contentDescription = "globe",
                    tint = Color(0xFFA4A4A6),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "공개 상태 : ",
                    fontFamily = korean_font_medium,
                    color = Color(0xFF7B7B7B),
                    fontSize = 14.sp
                )
                Text(
                    fontFamily = korean_font_medium,
                    text = " ${when (scope) {
                        "PUBLIC" -> "전체 공개"
                        "KILLING_PART" -> "킬링파트만 공개"
                        "PRIVATE" -> "전체 비공개"
                        else -> "전체 공개"
                    }}",
                    color = Color(0xFFFFFFFF),
                    fontSize = 14.sp
                )
                Icon(
                    imageVector = if (isDropdownExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = "dropdown",
                    tint = Color(0xFF7B7B7B),
                    modifier = Modifier.size(20.dp)
                )
                if (isDropdownExpanded) {
                    Box(
                        modifier = Modifier.fillMaxWidth(0.9f),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        Column(
                            modifier = Modifier
                                .background(Color.Transparent, RoundedCornerShape(8.dp))

                        ) {
                            ScopeOption("전체 공개", "PUBLIC", scope) { scope = it; isDropdownExpanded = false }
                            ScopeOption("킬링파트만 공개", "KILLING_PART", scope) { scope = it; isDropdownExpanded = false }
                            ScopeOption("전체 비공개", "PRIVATE", scope) { scope = it; isDropdownExpanded = false }
                        }
                    }
                }
            }
        }
        
        // 저장 버튼 (중앙정렬)
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = {
                    coroutineScope.launch {
                        runCatching {
                            val videos = repo.searchVideos(artist = artist, title = title)
                            val videoUrl = videos.firstOrNull()?.url ?: ""
                            val body = CreateDiaryRequest(
                                artist = artist,
                                musicTitle = title,
                                albumImageUrl = imageUrl,
                                videoUrl = videoUrl,
                                scope = scope,
                                content = content
                            )
                            repo.createDiary(body)
                        }.onSuccess {
                            navController.popBackStack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(0.8f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCCFF33), contentColor = Color.Black)
            ) {
                Text(
                    fontFamily = korean_font_medium,
                    text = "저장하기 →"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // BottomBar
        BottomBar(navController = navController)
    }
}

@Composable private fun ScopeOption(label: String, value: String, selected: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier
                .clickable { onSelect(value) }
                .background( color = if (selected == value) Color(0xFF1D1E20) else Color.Transparent,
                    shape = RoundedCornerShape(8.dp) )
                .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically )
    {
        Text( fontFamily = korean_font_medium, text = if (selected == value) "●" else "○", color = if (selected == value) Color(0xFFFFFFFF) else Color.Gray, fontSize = 13.sp )
        Spacer(modifier = Modifier.width(8.dp))
        Text( fontFamily = korean_font_medium, text = label, color = Color.White, fontSize = 14.sp )
    }
}

@Preview
@Composable
fun WriteDiaryScreenPreview() {
    val isDropdownExpanded = 1
    WriteDiaryScreen(
        navController = rememberNavController(),
        title = "Death Sonnet von Dat",
        artist = "Davinci Leo",
        imageUrl = "https://i.scdn.co/image/ab67616d00001e02c6b31f5f1ce2958380fdb9b0"
    )
}


