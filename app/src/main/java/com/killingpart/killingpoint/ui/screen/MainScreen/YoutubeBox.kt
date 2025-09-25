package com.killingpart.killingpoint.ui.screen.MainScreen

import android.graphics.Shader
import android.content.Intent
import android.net.Uri
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebSettings
import android.os.Build
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.killingpart.killingpoint.R
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import com.killingpart.killingpoint.data.model.Diary
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview

/**
 * YouTube embed URL을 자동재생이 가능한 URL로 변환
 */
private fun getYouTubeAutoPlayUrl(embedUrl: String): String {
    return try {
        val videoId = embedUrl.substringAfter("/embed/").substringBefore("?")
        // 여러 가지 URL 형식 시도
        val baseUrl = "https://www.youtube-nocookie.com/embed/$videoId"
        val params = listOf(
            "autoplay=1",
            "mute=1",  // 음소거로 자동재생 정책 준수
            "controls=1",
            "playsinline=1",
            "enablejsapi=1",
            "rel=0",
            "modestbranding=1",
            "origin=${android.net.Uri.encode("https://www.youtube.com")}",
            "widget_referrer=${android.net.Uri.encode("https://www.youtube.com")}",
            "iv_load_policy=3",
            "fs=1",
            "cc_load_policy=0",
            "start=0",
            "end=0",
            "loop=0",
            "playlist=$videoId",
            "showinfo=0",
            "disablekb=0",
            "enablejsapi=1"
        ).joinToString("&")
        
        "$baseUrl?$params"
    } catch (e: Exception) {
        Log.e("YoutubeBox", "Error parsing YouTube URL: $embedUrl", e)
        embedUrl
    }
}

@Composable
fun YoutubeBox(diary: Diary?) {
    var webView by remember { mutableStateOf<WebView?>(null) }
    val context = LocalContext.current
    
    Column (
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        if (diary != null) {
            val autoPlayUrl = getYouTubeAutoPlayUrl(diary.videoUrl)
            Log.d("YoutubeBox", "Loading URL: $autoPlayUrl")

            // 재생 버튼들 추가
            Row(
                modifier = Modifier.padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        // 사용자가 직접 터치했을 때 재생 시도
                        Log.d("YoutubeBox", "User clicked play button")
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            webView?.evaluateJavascript("""
                                try {
                                    var iframe = document.querySelector('iframe');
                                    if (iframe && iframe.contentWindow) {
                                        iframe.contentWindow.postMessage('{"event":"command","func":"playVideo","args":""}', '*');
                                        iframe.contentWindow.postMessage('{"event":"command","func":"unMute","args":""}', '*');
                                    }
                                } catch (e) {
                                    console.log('Manual play error:', e);
                                }
                            """.trimIndent(), null)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCCFF33), contentColor = Color.Black)
                ) {
                    Text("▶ 재생하기", fontFamily = PaperlogyFontFamily)
                }
                
                Button(
                    onClick = {
                        // 외부 브라우저로 열기
                        val videoId = diary.videoUrl.substringAfter("/embed/").substringBefore("?")
                        val youtubeUrl = "https://www.youtube.com/watch?v=$videoId"
                        Log.d("YoutubeBox", "Opening in browser: $youtubeUrl")
                        
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(youtubeUrl))
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Log.e("YoutubeBox", "Failed to open browser", e)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF666666), contentColor = Color.White)
                ) {
                    Text("🌐 브라우저에서 열기", fontFamily = PaperlogyFontFamily)
                }
            }

            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        webView = this
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                Log.d("YoutubeBox", "Page finished loading: $url")
                                
                                // YouTube iframe이 로드된 후 JavaScript로 재생 시도
                                view?.evaluateJavascript("""
                                    try {
                                        // iframe 내부의 YouTube 플레이어에 접근
                                        var iframe = document.querySelector('iframe');
                                        if (iframe && iframe.contentWindow) {
                                            iframe.contentWindow.postMessage('{"event":"command","func":"playVideo","args":""}', '*');
                                        }
                                        
                                        // 직접적인 YouTube API 호출 시도
                                        if (typeof window.postMessage === 'function') {
                                            window.postMessage('{"event":"command","func":"playVideo","args":""}', '*');
                                        }
                                        
                                        // 여러 번의 재생 시도 (재생 정책 우회)
                                        setTimeout(function() {
                                            if (iframe && iframe.contentWindow) {
                                                iframe.contentWindow.postMessage('{"event":"command","func":"playVideo","args":""}', '*');
                                            }
                                        }, 500);
                                        
                                        setTimeout(function() {
                                            if (iframe && iframe.contentWindow) {
                                                iframe.contentWindow.postMessage('{"event":"command","func":"playVideo","args":""}', '*');
                                            }
                                        }, 1500);
                                        
                                        setTimeout(function() {
                                            if (iframe && iframe.contentWindow) {
                                                iframe.contentWindow.postMessage('{"event":"command","func":"playVideo","args":""}', '*');
                                            }
                                        }, 3000);
                                        
                                        // 사용자 제스처 시뮬레이션 및 음소거 해제
                                        setTimeout(function() {
                                            var clickEvent = new MouseEvent('click', {
                                                view: window,
                                                bubbles: true,
                                                cancelable: true
                                            });
                                            if (iframe) {
                                                iframe.dispatchEvent(clickEvent);
                                                // 음소거 해제 시도
                                                iframe.contentWindow.postMessage('{"event":"command","func":"unMute","args":""}', '*');
                                            }
                                        }, 2000);
                                        
                                        // 추가 음소거 해제 시도
                                        setTimeout(function() {
                                            if (iframe && iframe.contentWindow) {
                                                iframe.contentWindow.postMessage('{"event":"command","func":"unMute","args":""}', '*');
                                            }
                                        }, 4000);
                                        
                                    } catch (e) {
                                        console.log('YouTube play error:', e);
                                    }
                                """.trimIndent(), null)
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
                            setSupportZoom(false)
                            builtInZoomControls = false
                            displayZoomControls = false
                            setSupportMultipleWindows(false)
                            setGeolocationEnabled(false)
                            setRenderPriority(WebSettings.RenderPriority.HIGH)
                            setLayoutAlgorithm(WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING)
                            setLoadsImagesAutomatically(true)
                            setBlockNetworkImage(false)
                            setBlockNetworkLoads(false)

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                            }

                            // User Agent 설정 (에뮬레이터에서 YouTube 호환성 개선)
                            userAgentString = "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
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

            Spacer(modifier = Modifier.height(40.dp))


            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MusicTimeBar(
                    title = diary.musicTitle,
                    start = 0,
                    during = 20,
                    total = 180
                )
            }
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
            Spacer(modifier = Modifier.height(40.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MusicTimeBar(
                    title = "로딩 중...",
                    start = 0,
                    during = 20,
                    total = 180
                )
            }

        }
    }
}

@Preview
@Composable
fun YoutubeBoxPreview() {
    YoutubeBox(diary = null)
}