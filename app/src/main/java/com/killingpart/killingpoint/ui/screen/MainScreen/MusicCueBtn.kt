package com.killingpart.killingpoint.ui.screen.MainScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.killingpart.killingpoint.R

@Composable
fun MusicCueBtn() {
    Row (
        modifier = Modifier.fillMaxWidth().height(70.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ){
        Image(
            painter = painterResource(id = R.drawable.shuffle),
            contentDescription = "셔플",
            modifier = Modifier.size(24.dp)
        )

        Box(
            modifier = Modifier.size(44.dp)
                .background(color = Color(0xFF161616), RoundedCornerShape(30.dp))
        )

        Box(
            modifier = Modifier.size(70.dp)
                .background(color = Color.White, RoundedCornerShape(50.dp)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.pause),
                contentDescription = "일시정지",
                modifier = Modifier.size(24.dp)
            )
        }

        Box(
            modifier = Modifier.size(44.dp)
                .background(color = Color(0xFF161616), RoundedCornerShape(30.dp))
        )

        Image(
            painter = painterResource(id = R.drawable.repeat),
            contentDescription = "반복재생",
            modifier = Modifier.size(24.dp)
        )
    }
}

@Preview
@Composable
fun MusicCueBtnPreview() {
    MusicCueBtn()
}