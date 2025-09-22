package com.killingpart.killingpoint.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.killingpart.killingpoint.R

@Composable
fun BottomBar() {
    Row (
        modifier = Modifier.fillMaxWidth()
            .height(72.dp)
            .padding(horizontal = 42.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.spacedBy(38.dp),
        verticalAlignment = Alignment.CenterVertically
    ){
        Image(
            painter = painterResource(id = R.drawable.navi_home),
            contentDescription = "MY 네비게이션 바",
            modifier = Modifier.size(48.dp)
        )
        Image(
            painter = painterResource(id = R.drawable.navi_search),
            contentDescription = "탐색 네비게이션 바",
            modifier = Modifier.size(48.dp)
        )
        Image(
            painter = painterResource(id = R.drawable.navi_social),
            contentDescription = "소셜 네비게이션 바",
            modifier = Modifier.size(48.dp)
        )
        Image(
            painter = painterResource(id = R.drawable.navi_add),
            contentDescription = "추가 네비게이션 바",
            modifier = Modifier.size(48.dp)
        )
    }
}

@Preview
@Composable
fun BottomBarPreview() {
    BottomBar()
}