package com.killingpart.killingpoint.ui.screen.ProfileScreen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.killingpart.killingpoint.BuildConfig
import com.killingpart.killingpoint.R
import com.killingpart.killingpoint.data.repository.AuthRepository
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import com.killingpart.killingpoint.ui.theme.mainGreen
import com.killingpart.killingpoint.ui.viewmodel.UserUiState
import com.killingpart.killingpoint.ui.viewmodel.UserViewModel
import kotlinx.coroutines.launch

private const val KILLING_PART_WEBSITE_URL = "https://sites.google.com/view/killingpart/"

@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val repo = remember { AuthRepository(context) }
    val scope = rememberCoroutineScope()
    val userViewModel: UserViewModel = viewModel()
    val userState by userViewModel.state.collectAsState()
    var blockedCount by remember { mutableStateOf<Int?>(null) }
    var showWebsiteModal by remember { mutableStateOf(false) }
    var showFeedbackModal by remember { mutableStateOf(false) }
    var showUnregisterModal by remember { mutableStateOf(false) }
    var pushEnabled by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        userViewModel.loadUserInfo(context)
        repo.getBlockedUsers(page = 0, size = 5)
            .onSuccess { blockedCount = it.page.totalElements }
            .onFailure { blockedCount = 0 }
    }

    SettingsBackgroundBox {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SettingsBackground)
                .verticalScroll(rememberScrollState())
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

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "차단 관리",
                color = Color(0xFF8E8E93),
                fontFamily = PaperlogyFontFamily,
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 2.dp, bottom = 8.dp)
            )

            SettingsListCard {
                SettingsTab(
                    tabTitle = "차단 목록",
                    detailText = "${blockedCount ?: 0}명",
                    onClick = { navController.navigate("settings/blocks") }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "알림",
                color = Color(0xFF8E8E93),
                fontFamily = PaperlogyFontFamily,
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 2.dp, bottom = 8.dp)
            )

            SettingsListCard {
                SettingsSwitchRow(
                    tabTitle = "알림",
                    checked = pushEnabled,
                    onCheckedChange = { pushEnabled = it }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "앱 정보",
                color = Color(0xFF8E8E93),
                fontFamily = PaperlogyFontFamily,
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 2.dp, bottom = 8.dp)
            )

            SettingsListCard {
                SettingsValueRow(
                    tabTitle = "앱 버전",
                    value = "v${BuildConfig.VERSION_NAME}"
                )
                SettingsDivider()
                SettingsTab(
                    tabTitle = "이용약관",
                    onClick = { navController.navigate("settings/terms") }
                )
                SettingsDivider()
                SettingsTab(
                    tabTitle = "개인정보처리방침",
                    onClick = { navController.navigate("settings/privacy") }
                )
                SettingsDivider()
                SettingsTab(
                    tabTitle = "웹사이트",
                    onClick = { showWebsiteModal = true }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "계정",
                color = Color(0xFF8E8E93),
                fontFamily = PaperlogyFontFamily,
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 2.dp, bottom = 8.dp)
            )

            SettingsListCard {
                SettingsValueRow(
                    tabTitle = "로그인 정보",
                    value = "Kakao"
                )
            }

            Spacer(modifier = Modifier.height(25.dp))

            Image(
                painter = painterResource(id = R.drawable.feedback),
                contentDescription = "문의 및 피드백",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showFeedbackModal = true }
            )

            Spacer(modifier = Modifier.height(25.dp))

            SettingsAccountActionCard(
                onLogoutClick = {
                    scope.launch {
                        repo.logout()
                        navController.navigate("home") {
                            popUpTo(0) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                },
                onUnregisterClick = {
                    showUnregisterModal = true
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        if (showWebsiteModal) {
            WebsiteMoveModal(
                onDismiss = { showWebsiteModal = false },
                onMove = {
                    showWebsiteModal = false
                    context.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(KILLING_PART_WEBSITE_URL)
                        )
                    )
                }
            )
        }

        if (showFeedbackModal) {
            FeedbackModal(
                onDismiss = { showFeedbackModal = false },
                onSubmit = { content, onResult ->
                    scope.launch {
                        repo.submitSurvey(content)
                            .onSuccess {
                                onResult(null)
                                showFeedbackModal = false
                            }
                            .onFailure { error ->
                                onResult(parseSettingsApiError(error.message) ?: "문의 및 피드백 전송에 실패했습니다.")
                            }
                    }
                }
            )
        }

        if (showUnregisterModal) {
            UnregisterConfirmModal(
                onDismiss = { showUnregisterModal = false },
                onConfirm = { onResult ->
                    scope.launch {
                        repo.unregister()
                            .onSuccess {
                                onResult(null)
                                showUnregisterModal = false
                                navController.navigate("home") {
                                    popUpTo(0) { inclusive = false }
                                    launchSingleTop = true
                                }
                            }
                            .onFailure { error ->
                                onResult(parseSettingsApiError(error.message) ?: "회원 탈퇴에 실패했습니다.")
                            }
                    }
                }
            )
        }
    }
}

@Composable
private fun SettingsAccountActionCard(
    onLogoutClick: () -> Unit,
    onUnregisterClick: () -> Unit
) {
    SettingsListCard {
        Text(
            text = "로그아웃",
            color = Color(0xFFFF5A5A),
            fontFamily = PaperlogyFontFamily,
            fontSize = 12.sp,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onLogoutClick() }
                .padding(vertical = 16.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        SettingsDivider()
        Text(
            text = "회원 탈퇴",
            color = Color(0xFF6A6A6A),
            fontFamily = PaperlogyFontFamily,
            fontSize = 12.sp,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onUnregisterClick() }
                .padding(vertical = 16.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeedbackModal(
    onDismiss: () -> Unit,
    onSubmit: (String, (String?) -> Unit) -> Unit
) {
    var content by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SettingsBackground,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "킬링파트 팀에게 문의사항이나 개선할 점을\n알려주세요!",
                color = Color.White,
                fontFamily = PaperlogyFontFamily,
                fontWeight = FontWeight.W600,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(176.dp)
                    .background(Color(0xFF262626), androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                    .padding(14.dp)
            ) {
                BasicTextField(
                    value = content,
                    onValueChange = {
                        if (it.length <= 1000) {
                            content = it
                            errorMessage = null
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    textStyle = TextStyle(
                        color = Color.White,
                        fontFamily = PaperlogyFontFamily,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    ),
                    cursorBrush = SolidColor(mainGreen),
                    decorationBox = { innerTextField ->
                        if (content.isEmpty()) {
                            Text(
                                text = "문의사항 및 피드백..",
                                color = Color(0xFF8E8E93),
                                fontFamily = PaperlogyFontFamily,
                                fontSize = 12.sp
                            )
                        }
                        innerTextField()
                    }
                )
            }

            errorMessage?.let {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = it,
                    color = Color(0xFFFF5A5A),
                    fontFamily = PaperlogyFontFamily,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "돌아가기",
                    color = Color.Black,
                    fontFamily = PaperlogyFontFamily,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .background(Color.White, androidx.compose.foundation.shape.RoundedCornerShape(6.dp))
                        .clickable(enabled = !isSubmitting) { onDismiss() }
                        .padding(vertical = 14.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Text(
                    text = if (isSubmitting) "보내는 중..." else "보내기",
                    color = Color.Black,
                    fontFamily = PaperlogyFontFamily,
                    fontWeight = FontWeight.W600,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .background(mainGreen, androidx.compose.foundation.shape.RoundedCornerShape(6.dp))
                        .clickable(enabled = !isSubmitting) {
                            val trimmed = content.trim()
                            when {
                                trimmed.isEmpty() -> errorMessage = "빈 값은 입력이 불가능합니다."
                                trimmed.length !in 1..1000 -> errorMessage = "1자 이상 1000자 이하로 작성 가능합니다."
                                else -> {
                                    isSubmitting = true
                                    onSubmit(trimmed) { error ->
                                        isSubmitting = false
                                        errorMessage = error
                                    }
                                }
                            }
                        }
                        .padding(vertical = 14.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnregisterConfirmModal(
    onDismiss: () -> Unit,
    onConfirm: ((String?) -> Unit) -> Unit
) {
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF242424),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "!",
                color = Color(0xFFFF3B3B),
                fontFamily = PaperlogyFontFamily,
                fontWeight = FontWeight.W700,
                fontSize = 26.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "정말 탈퇴하시겠어요?",
                color = Color.White,
                fontFamily = PaperlogyFontFamily,
                fontWeight = FontWeight.W700,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "기존 데이터는 즉시 삭제되며 복구할 수 없습니다.",
                color = Color.White,
                fontFamily = PaperlogyFontFamily,
                fontWeight = FontWeight.W500,
                fontSize = 11.sp
            )

            errorMessage?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = it,
                    color = Color(0xFFFF5A5A),
                    fontFamily = PaperlogyFontFamily,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "← 돌아가기",
                    color = Color.Black,
                    fontFamily = PaperlogyFontFamily,
                    fontWeight = FontWeight.W600,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .background(Color.White, androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                        .clickable(enabled = !isSubmitting) { onDismiss() }
                        .padding(vertical = 14.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Text(
                    text = if (isSubmitting) "탈퇴 중..." else "탈퇴하기",
                    color = Color.White,
                    fontFamily = PaperlogyFontFamily,
                    fontWeight = FontWeight.W600,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .background(Color(0xFFFF333B), androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                        .clickable(enabled = !isSubmitting) {
                            isSubmitting = true
                            errorMessage = null
                            onConfirm { error ->
                                isSubmitting = false
                                errorMessage = error
                            }
                        }
                        .padding(vertical = 14.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WebsiteMoveModal(
    onDismiss: () -> Unit,
    onMove: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF242424),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.website),
                contentDescription = "웹사이트",
                modifier = Modifier.size(30.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "외부 웹사이트로 이동됩니다.",
                color = Color.White,
                fontFamily = PaperlogyFontFamily,
                fontWeight = FontWeight.W600,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "취소",
                    color = Color.White,
                    fontFamily = PaperlogyFontFamily,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .background(Color(0xFF333333), androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                        .clickable { onDismiss() }
                        .padding(vertical = 14.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Text(
                    text = "이동하기",
                    color = Color.Black,
                    fontFamily = PaperlogyFontFamily,
                    fontWeight = FontWeight.W600,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .background(mainGreen, androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                        .clickable { onMove() }
                        .padding(vertical = 14.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
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
                .verticalScroll(rememberScrollState())
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

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "차단 관리",
                color = Color(0xFF8E8E93),
                fontFamily = PaperlogyFontFamily,
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 2.dp, bottom = 8.dp)
            )

            SettingsListCard {
                SettingsTab(
                    tabTitle = "차단 목록",
                    detailText = "3명",
                    onClick = {}
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "알림",
                color = Color(0xFF8E8E93),
                fontFamily = PaperlogyFontFamily,
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 2.dp, bottom = 8.dp)
            )

            SettingsListCard {
                SettingsSwitchRow(
                    tabTitle = "알림",
                    checked = true,
                    onCheckedChange = {}
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "앱 정보",
                color = Color(0xFF8E8E93),
                fontFamily = PaperlogyFontFamily,
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 2.dp, bottom = 8.dp)
            )

            SettingsListCard {
                SettingsValueRow(
                    tabTitle = "앱 버전",
                    value = "v${BuildConfig.VERSION_NAME}"
                )
                SettingsDivider()
                SettingsTab(
                    tabTitle = "이용약관",
                    onClick = {}
                )
                SettingsDivider()
                SettingsTab(
                    tabTitle = "개인정보처리방침",
                    onClick = {}
                )
                SettingsDivider()
                SettingsTab(
                    tabTitle = "웹사이트",
                    onClick = {}
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "계정",
                color = Color(0xFF8E8E93),
                fontFamily = PaperlogyFontFamily,
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 2.dp, bottom = 8.dp)
            )

            SettingsListCard {
                SettingsValueRow(
                    tabTitle = "로그인 정보",
                    value = "Kakao"
                )
            }

            Spacer(modifier = Modifier.height(25.dp))

            Image(
                painter = painterResource(id = R.drawable.feedback),
                contentDescription = "문의 및 피드백",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(25.dp))

            SettingsAccountActionCard(
                onLogoutClick = {},
                onUnregisterClick = {}
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
