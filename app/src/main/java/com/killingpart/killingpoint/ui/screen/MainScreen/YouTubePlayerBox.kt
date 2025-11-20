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
import androidx.compose.runtime.DisposableEffect

/**
 * YouTube Player API를 사용한 백그라운드 재생 가능한 플레이어
 */
@Composable
fun YouTubePlayerBox(
    diary: Diary?, 
    startSeconds: Float, 
    durationSeconds: Float = 0f,
    onVideoReady: () -> Unit = {},
    isPlayingState: Boolean? = null
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var player by remember { mutableStateOf<YouTubePlayer?>(null) }
    var currentTime by remember { mutableStateOf(0f) }
    
    // 콜백을 remember로 저장하여 리스너에서 사용
    val videoReadyCallback = remember(onVideoReady) { onVideoReady }
    
    val endSeconds = if (durationSeconds > 0f) {
        startSeconds + durationSeconds
    } else {
        null
    }
    
    Log.d("YouTubePlayerBox", "YouTubePlayerBox called with diary: ${diary?.musicTitle}, videoUrl: ${diary?.videoUrl}, startSeconds: $startSeconds, durationSeconds: $durationSeconds, endSeconds: $endSeconds")
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (diary != null) {
            val videoId = diary.videoUrl.substringAfter("/embed/").substringBefore("?")
            
            // startSeconds가 변경될 때 seekTo로 위치 이동 (디바운싱 적용)
            LaunchedEffect(startSeconds) {
                if (player != null) {
                    // 500ms 디바운싱: 사용자가 슬라이더를 계속 움직이면 마지막 값만 적용
                    kotlinx.coroutines.delay(500)
                    player?.seekTo(startSeconds)
                }
            }

            LaunchedEffect(currentTime, endSeconds, startSeconds, durationSeconds, player) {
                if (isPlaying && player != null && endSeconds != null && durationSeconds > 0f && currentTime >= endSeconds) {
                    player?.seekTo(startSeconds)
                }
            }

            LaunchedEffect(isPlayingState, player) {
                if (player != null && isPlayingState != null) {
                    if (isPlayingState) {
                        player?.play()
                    } else {
                        player?.pause()
                    }
                }
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
                // videoUrl이 변경될 때만 AndroidView 재생성 (startSeconds는 seekTo로 처리)
                key(diary?.videoUrl) {
                    
                    // videoUrl 변경 시에만 정리 작업
                    DisposableEffect(diary?.videoUrl) {
                        onDispose {
                            player = null
                        }
                    }
                    
                    AndroidView(
                        factory = { context ->
                            YouTubePlayerView(context).apply {
                                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                                var hasCalledReady = false
                                
                                addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                                override fun onReady(youTubePlayer: YouTubePlayer) {
                                    player = youTubePlayer
                                    hasCalledReady = false
                                    youTubePlayer.loadVideo(videoId, startSeconds)
                                    youTubePlayer.play()
                                    isPlaying = true
                                }
                                
                                override fun onStateChange(youTubePlayer: YouTubePlayer, state: com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState) {
                                    super.onStateChange(youTubePlayer, state)
                                    when (state) {
                                        com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState.PLAYING -> {
                                            isPlaying = true
                                            if (!hasCalledReady) {
                                                hasCalledReady = true
                                                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                                    videoReadyCallback()
                                                }, 500)
                                            }
                                        }
                                        com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState.PAUSED -> {
                                            isPlaying = false
                                        }
                                        com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState.ENDED -> {
                                            isPlaying = false
                                            if (endSeconds == null || durationSeconds == 0f) {
                                                youTubePlayer.seekTo(startSeconds)
                                                youTubePlayer.play()
                                            }
                                        }
                                        else -> {}
                                    }
                                }
                                
                                override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                                    super.onCurrentSecond(youTubePlayer, second)
                                    currentTime = second
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
