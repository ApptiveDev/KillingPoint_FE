package com.killingpart.killingpoint.ui.screen.AddMusicScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ColorMatrixColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.killingpart.killingpoint.R
import com.killingpart.killingpoint.ui.component.BottomBar

@Composable
fun AddMusicScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1D1E20))
    ) {
            // Center background logo (subtle)
            Image(
                painter = painterResource(id = R.drawable.killingpart_logo_dark),
                contentDescription = "앱 배경 로고",
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (-120).dp)
                    .size(260.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(60.dp))
                SearchField(modifier = Modifier.fillMaxWidth(0.85f))
                Spacer(modifier = Modifier.weight(1f))
                BottomBar(navController = navController)
            }
    }
}

@Preview
@Composable
fun AddMusicScreenPreview() {
    AddMusicScreen(navController = rememberNavController())
}
