package com.killingpart.killingpoint.ui.screen.MainScreen

import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebSettings
import android.os.Build
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.killingpart.killingpoint.R
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import com.killingpart.killingpoint.data.model.Diary
import androidx.compose.runtime.Composable

/**
 * YouTube embed URL을 자동재생이 가능한 URL로 변환
 */
private fun getYouTubeAutoPlayUrl(embedUrl: String): String {
    return try {
        val videoId = embedUrl.substringAfter("/embed/").substringBefore("?")
        "https://www.youtube-nocookie.com/embed/$videoId?autoplay=1&mute=0&controls=1&playsinline=1&enablejsapi=1&rel=0&modestbranding=1"
    } catch (e: Exception) {
        embedUrl
    }
}

@Composable
fun YoutubeBox(diary: Diary?) {
    Column (
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        if (diary != null) {
            val autoPlayUrl = getYouTubeAutoPlayUrl(diary.videoUrl)
            Log.d("YoutubeBox", "Loading URL: $autoPlayUrl")

            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                Log.d("YoutubeBox", "Page finished loading: $url")
                            }

                            override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                                super.onReceivedError(view, errorCode, description, failingUrl)
                                Log.e("YoutubeBox", "WebView error: $errorCode - $description")
                            }
                        }

                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                super.onProgressChanged(view, newProgress)
                                Log.d("YoutubeBox", "Loading progress: $newProgress%")
                            }
                        }

                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            mediaPlaybackRequiresUserGesture = false
                            allowFileAccess = true
                            allowContentAccess = true
                            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            useWideViewPort = true
                            loadWithOverviewMode = true
                            cacheMode = WebSettings.LOAD_DEFAULT
                            databaseEnabled = true

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                            }

                            // User Agent 설정 (에뮬레이터에서 YouTube 호환성 개선)
                            userAgentString = "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.120 Mobile Safari/537.36"
                        }

                        if (Build.FINGERPRINT.contains("generic") || Build.FINGERPRINT.contains("unknown")) {
                            // 에뮬레이터인 경우
                            setLayerType(WebView.LAYER_TYPE_SOFTWARE, null)
                        } else {
                            // 실제 기기인 경우
                            setLayerType(WebView.LAYER_TYPE_HARDWARE, null)
                        }

                        loadUrl(autoPlayUrl)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(207.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = diary.musicTitle,
                fontFamily = PaperlogyFontFamily,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = diary.artist,
                fontFamily = PaperlogyFontFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Light,
                color = Color.White
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.basic_youtube),
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
                text = "로딩 중...",
                fontFamily = PaperlogyFontFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Light,
                color = Color.White
            )
        }
    }
}