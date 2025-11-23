package com.killingpart.killingpoint.ui.component

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.killingpart.killingpoint.R

@Composable
fun AnimatedStage(stage: Int) {
    Crossfade(targetState = stage, animationSpec = tween(350)) { st ->
        when (st) {
            0 -> Stage0()
            1 -> Stage1Animated()
            2 -> Stage2Animated()
            3 -> Stage3Animated()
            4 -> Stage4Animated()
        }
    }
}
@Composable
fun Stage0() {
    Row ( modifier = Modifier.fillMaxSize(),
    horizontalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.splash_k),
            contentDescription = "스플래시1 K",
            modifier = Modifier.size(36.dp, 32.dp))

        Spacer(modifier = Modifier.width(12.dp))

        Image(
            painter = painterResource(id = R.drawable.splash_p),
            contentDescription = "스플래시1 P",
            modifier = Modifier.size(36.dp, 32.dp)) } }

@Composable
fun Stage1Animated() {

    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(250)
    )

    val width by animateDpAsState(
        targetValue = 35.dp,
        animationSpec = tween(250)
    )

    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row (
            verticalAlignment = Alignment.CenterVertically
        ){
            Image(
                painterResource(R.drawable.splash_k),
                null,
                Modifier.size(36.dp))

            Spacer(Modifier.width(7.dp))

            Image(
                painter = painterResource(R.drawable.splash_1),
                contentDescription = null,
                modifier = Modifier
                    .width(width)
                    .height(10.dp)
                    .alpha(alpha)
            )

            Spacer(Modifier.width(7.dp))

            Image(
                painterResource(R.drawable.splash_p),
                null,
                Modifier.size(36.dp))
        }

    }
}

@Composable
fun Stage2Animated() {

    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(250)
    )

    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row (
            verticalAlignment = Alignment.CenterVertically
        ){
            Image(
                painterResource(R.drawable.splash_k),
                null,
                Modifier.size(36.dp))

            Spacer(Modifier.width(35.dp))

            Image(
                painterResource(R.drawable.splash_3),
                contentDescription = null,
                modifier = Modifier
                    .size(144.dp, 36.dp)
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale
                    )
            )

            Spacer(Modifier.width(35.dp))

            Image(
                painterResource(R.drawable.splash_p),
                null,
                Modifier.size(36.dp))
        }
    }
}

@Composable
fun Stage3Animated() {

    val offsetK by animateDpAsState(targetValue = 0.dp, animationSpec = tween(250))
    val offsetP by animateDpAsState(targetValue = (6).dp, animationSpec = tween(250))

    Column(
        modifier = Modifier.fillMaxWidth()
            .padding(start = 80.dp),
    ) {
        Column{
            Image(
                painterResource(R.drawable.splash_k),
                contentDescription = null,
                modifier = Modifier
                    .offset(y = offsetK)
                    .size(36.dp)
            )

            Row (
                verticalAlignment = Alignment.CenterVertically
            ){
                Image(
                    painterResource(R.drawable.splash_3),
                    null, Modifier.size(144.dp, 36.dp)
                )
                Spacer(Modifier.width(6.dp))
                Image(
                    painterResource(R.drawable.splash_p),
                    null,
                    modifier = Modifier
                        .size(36.dp)
                        .offset(x = offsetP)
                )
            }
        }

    }
}

@Preview
@Composable
fun SplashComPreview(){
    //SplashScreen (onTimeout = {})
    Stage2Animated()
}

@Composable
fun Stage4Animated() {

    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(400)
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Column{
            Image(
                painterResource(R.drawable.splash_killing),
                null,
                modifier = Modifier
                    .size(214.dp, 33.dp)
                    .alpha(alpha)
            )
            Spacer(Modifier.height(7.dp))
            Row {
                Image(
                    painterResource(R.drawable.splash_3),
                    null,
                    Modifier.size(144.dp, 36.dp))
                Spacer(Modifier.width(6.dp))
                Image(
                    painterResource(R.drawable.splash_part),
                    null,
                    modifier = Modifier
                        .size(143.dp, 36.dp)
                        .alpha(alpha)
                )
            }
        }

    }
}


