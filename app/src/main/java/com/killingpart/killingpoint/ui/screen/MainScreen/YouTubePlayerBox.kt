package com.killingpart.killingpoint.ui.screen.MainScreen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun YouTubePlayerBox(diary: Diary?, startSeconds: Float) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    
    Log.d("YouTubePlayerBox", "YouTubePlayerBox called with diary: ${diary?.musicTitle}, videoUrl: ${diary?.videoUrl}")
    
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
            
            // YouTube Player with custom background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        color = Color.Transparent,
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                // diary가 변경될 때마다 AndroidView 재생성
                key(diary) {
                    Log.d("YouTubePlayerBox", "AndroidView 재생성 - diary: ${diary.musicTitle}, videoUrl: ${diary.videoUrl}")
                    AndroidView(
                        factory = { context ->
                            Log.d("YouTubePlayerBox", "YouTubePlayerView factory 호출 - videoId: $videoId")
                            YouTubePlayerView(context).apply {
                                // YouTube Player 자체에 corner radius 적용
                                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                                
                                addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                                override fun onReady(youTubePlayer: YouTubePlayer) {
                                    Log.d("YouTubePlayerBox", "Player ready for video: $videoId")
                                    // 자동재생 시작 (백그라운드 재생 가능)
                                    youTubePlayer.loadVideo(videoId, startSeconds)
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
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp))
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            // 제목 (AlbumDiaryBox와 동일한 스타일)
            diary.musicTitle?.let { title ->
                Text(
                    text = title,
                    fontFamily = PaperlogyFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 가수 (AlbumDiaryBox와 동일한 스타일)
            diary.artist?.let { artist ->
                Text(
                    text = artist,
                    fontFamily = PaperlogyFontFamily,
                    fontWeight = FontWeight.Light,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

        }
    }
}
