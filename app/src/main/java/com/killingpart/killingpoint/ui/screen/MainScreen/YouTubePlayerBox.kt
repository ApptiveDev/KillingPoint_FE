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
    onVideoReady: () -> Unit = {}
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
            Log.d("YouTubePlayerBox", "Video ID: $videoId")
            
            // startSeconds가 변경될 때 seekTo로 위치 이동 (디바운싱 적용)
            LaunchedEffect(startSeconds) {
                if (player != null) {
                    // 500ms 디바운싱: 사용자가 슬라이더를 계속 움직이면 마지막 값만 적용
                    kotlinx.coroutines.delay(500)
                    player?.seekTo(startSeconds)
                    Log.d("YouTubePlayerBox", "seekTo called: $startSeconds (debounced)")
                }
            }
            
            // 반복재생을 위한 체크: currentTime이 endSeconds에 도달하면 startSeconds로 이동
            LaunchedEffect(currentTime, endSeconds, startSeconds, durationSeconds, player) {
                if (isPlaying && player != null && endSeconds != null && durationSeconds > 0f && currentTime >= endSeconds) {
                    Log.d("YouTubePlayerBox", "Reached endSeconds ($endSeconds), seeking to startSeconds ($startSeconds)")
                    player?.seekTo(startSeconds)
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
                    Log.d("YouTubePlayerBox", "AndroidView 재생성 - diary: ${diary.musicTitle}, videoUrl: ${diary.videoUrl}")
                    
                    // videoUrl 변경 시에만 정리 작업
                    DisposableEffect(diary?.videoUrl) {
                        onDispose {
                            Log.d("YouTubePlayerBox", "DisposableEffect onDispose - cleaning up player")
                            try {
                                player = null
                            } catch (e: Exception) {
                                Log.e("YouTubePlayerBox", "Error in onDispose: ${e.message}")
                            }
                        }
                    }
                    
                    AndroidView(
                        factory = { context ->
                            Log.d("YouTubePlayerBox", "YouTubePlayerView factory 호출 - videoId: $videoId, startSeconds: $startSeconds")
                            YouTubePlayerView(context).apply {
                                // YouTube Player 자체에 corner radius 적용
                                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                                
                                // 리스너 내부에서 관리하는 로컬 플래그
                                var hasCalledReady = false
                                
                                addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                                override fun onReady(youTubePlayer: YouTubePlayer) {
                                    Log.d("YouTubePlayerBox", "Player ready for video: $videoId, startSeconds: $startSeconds")
                                    player = youTubePlayer
                                    hasCalledReady = false // 비디오 준비 콜백 플래그 리셋
                                    // 비디오 로드 (startSeconds 위치에서 시작)
                                    youTubePlayer.loadVideo(videoId, startSeconds)
                                    Log.d("YouTubePlayerBox", "onReady: loadVideo called with startSeconds: $startSeconds")
                                    youTubePlayer.play()
                                    isPlaying = true
                                }
                                
                                override fun onStateChange(youTubePlayer: YouTubePlayer, state: com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState) {
                                    super.onStateChange(youTubePlayer, state)
                                    when (state) {
                                        com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState.PLAYING -> {
                                            isPlaying = true
                                            Log.d("YouTubePlayerBox", "Playing at currentTime: $currentTime")
                                            // 비디오가 재생 시작되면 준비 완료 콜백 호출 (한 번만)
                                            if (!hasCalledReady) {
                                                Log.d("YouTubePlayerBox", "Video playing - calling onVideoReady")
                                                hasCalledReady = true
                                                // Handler를 사용하여 메인 스레드에서 지연 후 콜백 호출
                                                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                                    videoReadyCallback()
                                                }, 500)
                                            }
                                        }
                                        com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState.PAUSED -> {
                                            isPlaying = false
                                            Log.d("YouTubePlayerBox", "Paused")
                                        }
                                        com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState.ENDED -> {
                                            isPlaying = false
                                            Log.d("YouTubePlayerBox", "Ended")
                                            // 영상이 끝났을 때 반복재생을 위해 startSeconds로 이동 (duration이 없을 때만)
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
