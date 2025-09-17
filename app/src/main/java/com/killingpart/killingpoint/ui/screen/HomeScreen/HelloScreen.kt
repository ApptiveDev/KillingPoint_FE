package com.killingpart.killingpoint.ui.screen.HomeScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.killingpart.killingpoint.R
import com.killingpart.killingpoint.ui.theme.darkGray
import com.killingpart.killingpoint.ui.theme.mainGreen
import com.killingpart.killingpoint.ui.theme.textGray1
import com.killingpart.killingpoint.ui.theme.UnboundedFontFamily
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily

@Composable
fun HelloScreen() {
    Column (
        modifier = Modifier.fillMaxSize()
            .background(color = mainGreen),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Image(
            painter = painterResource(id = R.drawable.killingpart_logo),
            contentDescription = "KillingPart Logo",
            modifier = Modifier.size(width = 300.dp, height = 78.dp)
        )

        Spacer(modifier = Modifier.height(278.dp))

        Row (
            modifier = Modifier.size(240.dp, 54.dp)
                .background(color = darkGray, RoundedCornerShape(100.dp)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ){
            Text(
                text = "LOGIN",
                color = Color.White,
                fontFamily = UnboundedFontFamily,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "SNS로 간편 로그인",
            color = textGray1,
            fontSize = 12.sp,
            fontFamily = PaperlogyFontFamily,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.clickable{}
        )

    }
}

@Preview
@Composable
fun HelloScreenPreview() {
    HelloScreen()
}