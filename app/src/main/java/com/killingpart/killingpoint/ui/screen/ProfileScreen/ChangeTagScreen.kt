package com.killingpart.killingpoint.ui.screen.ProfileScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.killingpart.killingpoint.data.repository.AuthRepository
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import com.killingpart.killingpoint.ui.viewmodel.UserUiState
import com.killingpart.killingpoint.ui.viewmodel.UserViewModel
import kotlinx.coroutines.launch
import java.util.regex.Pattern

@Composable
fun ChangeTagScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { AuthRepository(context) }
    val userViewModel: UserViewModel = viewModel()
    val userState by userViewModel.state.collectAsState()
    var tag by remember { mutableStateOf("") }
    var originalTag by remember { mutableStateOf("") }
    var validationMessage by remember { mutableStateOf<String?>(null) }
    var isUpdating by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        userViewModel.loadUserInfo(context)
    }

    LaunchedEffect(userState) {
        val userInfo = (userState as? UserUiState.Success)?.userInfo
        if (userInfo != null && originalTag.isBlank()) {
            originalTag = userInfo.tag
            tag = userInfo.tag
        }
    }

    fun validateTag(value: String): String? {
        if (value.length < 4 || value.length > 30) {
            return "tag는 4자 이상 30자 이하이어야 합니다."
        }
        val pattern = Pattern.compile("^[a-z0-9_.]+$")
        if (!pattern.matcher(value).matches()) {
            return "30자 이내의 영문과 숫자, 특수문자([.],[_])로 조합해주세요."
        }
        return null
    }

    fun updateTag() {
        if (isUpdating) return
        val cleanTag = tag.trim().removePrefix("@").lowercase()
        val validationError = validateTag(cleanTag)
        if (validationError != null) {
            validationMessage = validationError
            return
        }
        if (cleanTag == originalTag) {
            navController.popBackStack()
            return
        }

        isUpdating = true
        scope.launch {
            repo.updateTag(cleanTag)
                .onSuccess {
                    userViewModel.loadUserInfo(context)
                    navController.popBackStack()
                }
                .onFailure { e ->
                    validationMessage = parseSettingsApiError(e.message) ?: "태그 업데이트 실패"
                }
            isUpdating = false
        }
    }

    SettingsBackgroundBox {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SettingsBackground)
                .padding(horizontal = 18.dp)
        ) {
            EditTopBar(
                title = "태그 변경",
                actionText = if (isUpdating) "저장 중" else "완료",
                actionEnabled = !isUpdating && tag.isNotBlank() && tag.trim().removePrefix("@") != originalTag,
                onBack = { navController.popBackStack() },
                onAction = { updateTag() }
            )

            Spacer(modifier = Modifier.height(26.dp))

            SettingsLabel("태그")
            SettingsTextField(
                value = if (tag.startsWith("@")) tag else "@$tag",
                onValueChange = { newValue ->
                    tag = newValue.removePrefix("@").lowercase()
                    validationMessage = validateTag(tag)
                },
                keyboardType = KeyboardType.Text
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "영문 소문자 4이상, 30자 이내, 특수문자 일부([.],[_])",
                color = Color(0xFF8A8A8A),
                fontFamily = PaperlogyFontFamily,
                fontSize = 11.sp
            )
            validationMessage?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = it,
                    color = Color(0xFFFF5A5A),
                    fontFamily = PaperlogyFontFamily,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212, widthDp = 360, heightDp = 800)
@Composable
private fun ChangeTagScreenPreview() {
    SettingsBackgroundBox {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SettingsBackground)
                .padding(horizontal = 18.dp)
        ) {
            EditTopBar(
                title = "태그 변경",
                actionText = "완료",
                actionEnabled = false,
                onBack = {},
                onAction = {}
            )

            Spacer(modifier = Modifier.height(26.dp))

            SettingsLabel("태그")
            SettingsTextField(
                value = "@angrybadboy",
                onValueChange = {},
                keyboardType = KeyboardType.Text
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "영문 소문자 4이상, 30자 이내, 특수문자 일부([.],[_])",
                color = Color(0xFF8A8A8A),
                fontFamily = PaperlogyFontFamily,
                fontSize = 11.sp
            )
        }
    }
}
