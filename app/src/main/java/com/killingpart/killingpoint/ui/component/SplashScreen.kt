package com.killingpart.killingpoint.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.killingpart.killingpoint.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    var startFadeOut by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        // 로고 페이드인 대기
        delay(800)
        // 페이드아웃 시작
        startFadeOut = true
        // 페이드아웃 애니메이션 시간 대기 후 콜백 호출
        delay(400)
        onTimeout()
    }

    // 로고 페이드인 애니메이션
    val logoFadeIn = animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(
            durationMillis = 800,
            easing = LinearEasing
        )
    )
    
    // 전체 화면 페이드아웃 애니메이션
    val fadeOut = animateFloatAsState(
        targetValue = if (startFadeOut) 0f else 1f,
        animationSpec = tween(
            durationMillis = 400,
            easing = LinearEasing
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .alpha(fadeOut.value),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(246.dp))

        Image(
            painter = painterResource(id = R.drawable.splash_logo),
            contentDescription = "스플래시 로고",
            modifier = Modifier
                .size(width = 300.dp, height = 78.dp)
                .alpha(logoFadeIn.value)
        )
    }

}

@Preview
@Composable
fun SplashPreview(){
    SplashScreen (onTimeout = {})
}