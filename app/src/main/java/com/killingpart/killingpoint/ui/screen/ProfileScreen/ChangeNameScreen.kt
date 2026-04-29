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
fun ChangeNameScreen(navController: NavController) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { AuthRepository(context) }
    val userViewModel: UserViewModel = viewModel()
    val userState by userViewModel.state.collectAsState()
    var name by remember { mutableStateOf("") }
    var originalName by remember { mutableStateOf("") }
    var validationMessage by remember { mutableStateOf<String?>(null) }
    var isUpdating by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        userViewModel.loadUserInfo(context)
    }

    LaunchedEffect(userState) {
        val userInfo = (userState as? UserUiState.Success)?.userInfo
        if (userInfo != null && originalName.isBlank()) {
            originalName = userInfo.username
            name = userInfo.username
        }
    }

    fun validateName(value: String): String? {
        val trimmed = value.trim()
        if (trimmed.length !in 1..20) {
            return "이름은 1자 이상 20자 이하여야 합니다."
        }
        val pattern = Pattern.compile("^[A-Za-z0-9가-힣 ]+$")
        if (!pattern.matcher(trimmed).matches()) {
            return "이름은 영어, 한글, 숫자, 공백만 사용할 수 있습니다."
        }
        return null
    }

    fun updateName() {
        if (isUpdating) return
        val trimmed = name.trim()
        val validationError = validateName(trimmed)
        if (validationError != null) {
            validationMessage = validationError
            return
        }
        if (trimmed == originalName) {
            navController.popBackStack()
            return
        }

        isUpdating = true
        scope.launch {
            repo.updateUsername(trimmed)
                .onSuccess {
                    userViewModel.loadUserInfo(context)
                    navController.popBackStack()
                }
                .onFailure { e ->
                    validationMessage = parseSettingsApiError(e.message) ?: "이름 업데이트 실패"
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
                title = "이름 변경",
                actionText = if (isUpdating) "저장 중" else "완료",
                actionEnabled = !isUpdating && name.trim().isNotBlank() && name.trim() != originalName,
                onBack = { navController.popBackStack() },
                onAction = { updateName() }
            )

            Spacer(modifier = Modifier.height(26.dp))

            SettingsLabel("이름")
            SettingsTextField(
                value = name,
                onValueChange = {
                    name = it
                    validationMessage = validateName(it)
                },
                keyboardType = KeyboardType.Text
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "2~16자, 한글/영문 숫자 사용 가능",
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
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212, widthDp = 360, heightDp = 800)
@Composable
private fun ChangeNameScreenPreview() {
    SettingsBackgroundBox {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SettingsBackground)
                .padding(horizontal = 20.dp)
        ) {
            EditTopBar(
                title = "이름 변경",
                actionText = "완료",
                actionEnabled = true,
                onBack = {},
                onAction = {}
            )

            Spacer(modifier = Modifier.height(26.dp))

            SettingsLabel("이름")
            SettingsTextField(
                value = "김기영",
                onValueChange = {},
                keyboardType = KeyboardType.Text
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "1~20자, 영어/한글/숫자/공백 사용 가능",
                color = Color(0xFF8A8A8A),
                fontFamily = PaperlogyFontFamily,
                fontSize = 11.sp
            )
        }
    }
}
