package com.killingpart.killingpoint.ui.screen.MainScreen

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.killingpart.killingpoint.data.model.Diary
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

/**
 * YouTube Player API를 사용한 백그라운드 재생 가능한 플레이어
 */
@Composable
fun YouTubePlayerBox(diary: Diary?) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (diary != null) {
            val videoId = diary.videoUrl.substringAfter("/embed/").substringBefore("?")
            Log.d("YouTubePlayerBox", "Video ID: $videoId")
            
            // 재생 상태 표시
            if (isPlaying) {
                Text(
                    text = "🎵 재생 중...",
                    fontFamily = PaperlogyFontFamily,
                    color = Color(0xFFCCFF33),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            // YouTube Player
            AndroidView(
                factory = { context ->
                    YouTubePlayerView(context).apply {
                        addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                            override fun onReady(youTubePlayer: YouTubePlayer) {
                                Log.d("YouTubePlayerBox", "Player ready")
                                // 자동재생 시작 (백그라운드 재생 가능)
                                youTubePlayer.loadVideo(videoId, 0f)
                                youTubePlayer.play()
                                isPlaying = true
                            }
                            
                            override fun onStateChange(youTubePlayer: YouTubePlayer, state: com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState) {
                                super.onStateChange(youTubePlayer, state)
                                when (state) {
                                    com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState.PLAYING -> {
                                        isPlaying = true
                                        Log.d("YouTubePlayerBox", "Playing")
                                    }
                                    com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState.PAUSED -> {
                                        isPlaying = false
                                        Log.d("YouTubePlayerBox", "Paused")
                                    }
                                    com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState.ENDED -> {
                                        isPlaying = false
                                        Log.d("YouTubePlayerBox", "Ended")
                                    }
                                    else -> {}
                                }
                            }
                        })
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 컨트롤 버튼들
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        // 재생/일시정지 토글
                        Log.d("YouTubePlayerBox", "Toggle play/pause")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPlaying) Color(0xFFFF6B6B) else Color(0xFFCCFF33),
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = if (isPlaying) "⏸ 일시정지" else "▶ 재생",
                        fontFamily = PaperlogyFontFamily
                    )
                }
                
                Button(
                    onClick = {
                        // 정지
                        Log.d("YouTubePlayerBox", "Stop")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF666666),
                        contentColor = Color.White
                    )
                ) {
                    Text("⏹ 정지", fontFamily = PaperlogyFontFamily)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // 제목 + 우측 고정 버튼 Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 제목: 우측 버튼과 겹치지 않게 가변 폭 + 말줄임
                Text(
                    text = diary.musicTitle,
                    fontFamily = PaperlogyFontFamily,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // 우측 뮤직 리스트 버튼(고정)
                Button(
                    onClick = { Log.d("YouTubePlayerBox", "Open music list") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF232427),
                        contentColor = Color.White
                    )
                ) {
                    Text(text = "리스트", fontFamily = PaperlogyFontFamily)
                }
            }
        }
    }
}
