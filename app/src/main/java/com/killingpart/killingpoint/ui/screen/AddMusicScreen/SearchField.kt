package com.killingpart.killingpoint.ui.screen.AddMusicScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material3.TextFieldDefaults

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.killingpart.killingpoint.R

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val korean_font_thin = FontFamily(Font(R.font.paperlogy_thin))
val korean_font_medium = FontFamily(Font(R.font.paperlogy_medium))
@Composable
fun SearchField(
    modifier: Modifier = Modifier
) {
    var searchText by remember { mutableStateOf("") }

    TextField(
        value = searchText,
        onValueChange = { searchText = it },
        modifier = modifier
            .fillMaxWidth(0.8f)   // 전체 화면 너비의 90%
            .height(48.dp),
        placeholder = {
            Text(
                text = "곡 검색",
                color = Color(0xFF7B7B7B),
                style = TextStyle(
                    fontFamily = korean_font_medium,
                    fontSize = 14.sp
                )
            )
        },
        textStyle = TextStyle(
            color = Color.White,
            fontFamily = korean_font_medium,
            fontSize = 16.sp
        ),
        shape = RoundedCornerShape(48.dp), // 둥근 박스
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFF101010),
            unfocusedContainerColor = Color(0xFF101010),
            cursorColor = Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search Icon",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    )

}


@Preview
@Composable
fun SearchFieldPreview() {
    Surface(color = Color.Black) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SearchField()
        }
    }
}