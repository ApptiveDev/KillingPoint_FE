package com.killingpart.killingpoint.ui.screen.MainScreen

import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.killingpart.killingpoint.R
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import com.killingpart.killingpoint.ui.viewmodel.YouTubeViewModel
import com.killingpart.killingpoint.ui.viewmodel.YouTubeUiState

/**
 * YouTube embed URL을 자동재생이 가능한 URL로 변환
 */
private fun getYouTubeAutoPlayUrl(embedUrl: String): String {
    return try {
        if (embedUrl.contains("?")) {
            "$embedUrl&autoplay=1&mute=1"
        } else {
            "$embedUrl?autoplay=1&mute=1"
        }
    } catch (e: Exception) {
        embedUrl
    }
}

@Composable
fun YoutubeBox(
    artist: String,
    title: String
) {
    val context = LocalContext.current
    val youTubeViewModel: YouTubeViewModel = viewModel()
    val youTubeState by youTubeViewModel.state.collectAsState()

    LaunchedEffect(artist, title) {
        youTubeViewModel.searchVideos(context, artist, title)
    }

    Column (
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        when (val currentState = youTubeState) {
            is YouTubeUiState.Success -> {
                val autoPlayUrl = getYouTubeAutoPlayUrl(currentState.video.url)
                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            webViewClient = WebViewClient()
                            webChromeClient = WebChromeClient()
                            settings.javaScriptEnabled = true
                            settings.mediaPlaybackRequiresUserGesture = false
                            settings.domStorageEnabled = true
                            loadUrl(autoPlayUrl)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(207.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = currentState.video.title,
                    fontFamily = PaperlogyFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = artist,
                    fontFamily = PaperlogyFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Light,
                    color = Color.White
                )
            }
            is YouTubeUiState.Loading -> {
                Image(
                    painter = painterResource(id = R.drawable.example_video),
                    contentDescription = "유튜브 영상 로딩 중",
                    modifier = Modifier.fillMaxWidth().height(207.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "로딩 중...",
                    fontFamily = PaperlogyFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = artist,
                    fontFamily = PaperlogyFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Light,
                    color = Color.White
                )
            }
            is YouTubeUiState.Error -> {
                Image(
                    painter = painterResource(id = R.drawable.example_video),
                    contentDescription = "유튜브 영상 에러",
                    modifier = Modifier.fillMaxWidth().height(207.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = title,
                    fontFamily = PaperlogyFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = artist,
                    fontFamily = PaperlogyFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Light,
                    color = Color.White
                )
            }
        }
    }
}