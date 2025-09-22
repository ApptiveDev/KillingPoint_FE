package com.killingpart.killingpoint.ui.screen.HomeScreen

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.killingpart.killingpoint.R
import com.killingpart.killingpoint.auth.KakaoLoginClient
import com.killingpart.killingpoint.ui.theme.darkGray
import com.killingpart.killingpoint.ui.theme.mainGreen
import com.killingpart.killingpoint.ui.theme.textGray1
import com.killingpart.killingpoint.ui.theme.UnboundedFontFamily
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import com.killingpart.killingpoint.ui.viewmodel.LoginViewModel
import com.killingpart.killingpoint.ui.viewmodel.LoginUiState

@Composable
fun HelloScreen(navController: NavController) {
    val context = LocalContext.current
    val loginViewModel: LoginViewModel = viewModel()
    val loginState by loginViewModel.state.collectAsState()

    LaunchedEffect(loginState) {
        if (loginState is LoginUiState.Success) {
            navController.navigate("main") {
                popUpTo("home") { inclusive = true }
            }
        }
    }

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
                text = if (loginState is LoginUiState.Loading) "로그인 중..." else "LOGIN",
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
        )
        Spacer(modifier = Modifier.height(10.dp))
        Image(
            painter = painterResource(id = R.drawable.kakao_login_medium_narrow),
            contentDescription = "KakaoLoginBtn",
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                onSnsLoginClick(context, loginViewModel)
            }
        )

    }
}

private fun onSnsLoginClick(context: Context, loginViewModel: LoginViewModel) {
    loginViewModel.loginWithKakao(context) { kakaoAccessToken ->
        loginViewModel.loginWithServer(context, kakaoAccessToken)
    }
}
