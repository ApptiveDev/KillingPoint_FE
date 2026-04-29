package com.killingpart.killingpoint.ui.screen.ProfileScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.killingpart.killingpoint.R
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import com.killingpart.killingpoint.ui.theme.mainGreen
import com.killingpart.killingpoint.ui.viewmodel.UserUiState
import com.killingpart.killingpoint.ui.viewmodel.UserViewModel

@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val userViewModel: UserViewModel = viewModel()
    val userState by userViewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        userViewModel.loadUserInfo(context)
    }

    SettingsBackgroundBox {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SettingsBackground)
                .padding(horizontal = 16.dp)
        ) {
            SettingsTopBar(
                title = "설정",
                onBack = { navController.popBackStack() }
            )

            Spacer(modifier = Modifier.height(12.dp))

            when (val state = userState) {
                is UserUiState.Success -> {
                    SettingsProfileCard(
                        profileImageUrl = state.userInfo.profileImageUrl,
                        username = state.userInfo.username,
                        tag = state.userInfo.tag,
                        onNameClick = { navController.navigate("settings/name") },
                        onTagClick = { navController.navigate("settings/tag") },
                        onProfileImageClick = { navController.navigate("settings/profile-image") }
                    )
                }

                is UserUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(142.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = mainGreen)
                    }
                }

                is UserUiState.Error -> {
                    Text(
                        text = state.message,
                        color = Color(0xFFFF5A5A),
                        fontFamily = PaperlogyFontFamily,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsProfileCard(
    profileImageUrl: String,
    username: String,
    tag: String,
    onNameClick: () -> Unit,
    onTagClick: () -> Unit,
    onProfileImageClick: () -> Unit
) {
    SettingsListCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = profileImageUrl,
                contentDescription = "프로필 이미지",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape),
                placeholder = painterResource(id = R.drawable.default_profile),
                error = painterResource(id = R.drawable.default_profile)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = username,
                    color = Color.White,
                    fontFamily = PaperlogyFontFamily,
                    fontWeight = FontWeight.W600,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "@$tag",
                    color = Color(0xFF8E8E93),
                    fontFamily = PaperlogyFontFamily,
                    fontWeight = FontWeight.W400,
                    fontSize = 12.sp
                )
            }
        }
        SettingsDivider()
        SettingsTab(tabTitle = "이름 변경", onClick = onNameClick)
        SettingsDivider()
        SettingsTab(tabTitle = "태그 변경", onClick = onTagClick)
        SettingsDivider()
        SettingsTab(tabTitle = "프로필 이미지 변경", onClick = onProfileImageClick)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212, widthDp = 360, heightDp = 800)
@Composable
private fun SettingsScreenPreview() {
    SettingsBackgroundBox {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SettingsBackground)
                .padding(horizontal = 16.dp)
        ) {
            SettingsTopBar(
                title = "설정",
                onBack = {}
            )

            Spacer(modifier = Modifier.height(12.dp))

            SettingsProfileCard(
                profileImageUrl = "",
                username = "기영",
                tag = "angrybadboy",
                onNameClick = {},
                onTagClick = {},
                onProfileImageClick = {}
            )
        }
    }
}
