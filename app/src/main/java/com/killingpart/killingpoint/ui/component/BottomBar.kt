package com.killingpart.killingpoint.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.killingpart.killingpoint.R
import com.killingpart.killingpoint.ui.theme.UnboundedFontFamily

@Composable
fun BottomBar(navController: NavController) {
    Row (
        modifier = Modifier.fillMaxWidth()
            .height(94.dp)
            .padding(horizontal = 42.dp,),
        horizontalArrangement = Arrangement.spacedBy(38.dp),
        verticalAlignment = Alignment.CenterVertically
    ){
        Column (
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable { navController.navigate("main")}
        ){
            Image(
                painter = painterResource(id = R.drawable.navi_home),
                contentDescription = "MY 네비게이션 바",
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "MY",
                fontFamily = UnboundedFontFamily,
                fontWeight = FontWeight.Light,
                fontSize = 14.sp,
                color = Color.White
            )
        }

        Column (
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Image(
                painter = painterResource(id = R.drawable.navi_search),
                contentDescription = "탐색 네비게이션 바",
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "탐색",
                fontFamily = UnboundedFontFamily,
                fontWeight = FontWeight.Light,
                fontSize = 14.sp,
                color = Color.White
            )
        }
        Column (
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Image(
                painter = painterResource(id = R.drawable.navi_social),
                contentDescription = "소셜 네비게이션 바",
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "소셜",
                fontFamily = UnboundedFontFamily,
                fontWeight = FontWeight.Light,
                fontSize = 14.sp,
                color = Color.White
            )
        }
        Column (
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Image(
                painter = painterResource(id = R.drawable.navi_add),
                contentDescription = "추가 네비게이션 바",
                modifier = Modifier.size(48.dp).clickable { navController.navigate("add_music") }
            )
            Text(
                text = "추가",
                fontFamily = UnboundedFontFamily,
                fontWeight = FontWeight.Light,
                fontSize = 14.sp,
                color = Color.White
            )
        }
    }
}
