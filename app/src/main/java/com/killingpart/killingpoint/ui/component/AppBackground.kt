package com.killingpart.killingpoint.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

@Composable
fun AppBackground(content: @Composable () -> Unit) {
    Box (
        modifier = Modifier.fillMaxSize()
            .background(Color(0xFF1D1E20)),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color(0xFF060606),
                radius = size.minDimension * 0.85f,
                center = Offset(size.width * 0.1f, size.height * 0.37f)
            )

            drawCircle(
                color = Color(0xFF060606),
                radius = size.minDimension * 1.5f,
                center = Offset(size.width * 1.1f, size.height * 1.2f)
            )
        }

        content()
    }
}
