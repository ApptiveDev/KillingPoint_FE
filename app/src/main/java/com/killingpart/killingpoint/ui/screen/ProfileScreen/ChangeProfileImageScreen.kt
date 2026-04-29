package com.killingpart.killingpoint.ui.screen.ProfileScreen

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.killingpart.killingpoint.R
import com.killingpart.killingpoint.data.repository.AuthRepository
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import com.killingpart.killingpoint.ui.theme.mainGreen
import com.killingpart.killingpoint.ui.viewmodel.UserUiState
import com.killingpart.killingpoint.ui.viewmodel.UserViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

@Composable
fun ChangeProfileImageScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { AuthRepository(context) }
    val userViewModel: UserViewModel = viewModel()
    val userState by userViewModel.state.collectAsState()
    var isUploading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun uriToFile(uri: Uri): File? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val file = File(context.cacheDir, "profile_image_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            inputStream?.use { input ->
                outputStream.use { output -> input.copyTo(output) }
            }
            file
        } catch (e: Exception) {
            Log.e("SettingsScreen", "파일 변환 실패: ${e.message}", e)
            null
        }
    }

    fun removeQueryParams(url: String): String = url.split("?").first()

    fun refreshAndBack() {
        userViewModel.loadUserInfo(context)
        navController.popBackStack()
    }

    fun updateProfileImageWithUrl(imageUrl: String, temporalFileId: Long) {
        scope.launch {
            repo.updateProfileImage(temporalFileId, imageUrl)
                .onSuccess { refreshAndBack() }
                .onFailure { errorMessage = it.message ?: "프로필 이미지 변경 실패" }
            isUploading = false
        }
    }

    fun resetToDefaultImage() {
        if (isUploading) return
        isUploading = true
        errorMessage = null
        scope.launch {
            repo.deleteProfileImage()
                .onSuccess { refreshAndBack() }
                .onFailure { errorMessage = it.message ?: "기본 이미지로 변경 실패" }
            isUploading = false
        }
    }

    fun uploadProfileImage(imageUri: Uri) {
        if (isUploading) return
        isUploading = true
        errorMessage = null
        scope.launch {
            val imageFile = uriToFile(imageUri)
            if (imageFile == null) {
                errorMessage = "이미지 파일을 읽을 수 없습니다."
                isUploading = false
                return@launch
            }

            repo.getPresignedUrl()
                .onSuccess { presigned ->
                    repo.uploadImageToS3(presigned.presignedUrl, imageFile)
                        .onSuccess {
                            updateProfileImageWithUrl(
                                imageUrl = removeQueryParams(presigned.presignedUrl),
                                temporalFileId = presigned.id
                            )
                        }
                        .onFailure {
                            errorMessage = it.message ?: "이미지 업로드 실패"
                            isUploading = false
                        }
                }
                .onFailure {
                    errorMessage = it.message ?: "PresignedUrl 발급 실패"
                    isUploading = false
                }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { uploadProfileImage(it) }
    }

    LaunchedEffect(Unit) {
        userViewModel.loadUserInfo(context)
    }

    SettingsBackgroundBox {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SettingsBackground)
                .padding(horizontal = 18.dp)
        ) {
            SettingsTopBar(
                title = "프로필 이미지",
                onBack = { navController.popBackStack() }
            )

            Spacer(modifier = Modifier.height(28.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(116.dp),
                contentAlignment = Alignment.Center
            ) {
                val profileImageUrl = (userState as? UserUiState.Success)?.userInfo?.profileImageUrl
                AsyncImage(
                    model = profileImageUrl ?: R.drawable.default_profile,
                    contentDescription = "프로필 이미지",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(112.dp)
                        .clip(CircleShape),
                    placeholder = painterResource(id = R.drawable.default_profile),
                    error = painterResource(id = R.drawable.default_profile)
                )
                if (isUploading) {
                    CircularProgressIndicator(color = mainGreen)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            SettingsListCard {
                SettingsTab(
                    tabTitle = "갤러리에서 선택",
                    onClick = {
                        imagePickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                )
                SettingsDivider()
                SettingsTab(
                    tabTitle = "기본 이미지로 변경",
                    onClick = { resetToDefaultImage() }
                )
            }

            errorMessage?.let {
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
private fun ChangeProfileImageScreenPreview() {
    SettingsBackgroundBox {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SettingsBackground)
                .padding(horizontal = 18.dp)
        ) {
            SettingsTopBar(
                title = "프로필 이미지",
                onBack = {}
            )

            Spacer(modifier = Modifier.height(28.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(116.dp),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = R.drawable.default_profile,
                    contentDescription = "프로필 이미지",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(112.dp)
                        .clip(CircleShape),
                    placeholder = painterResource(id = R.drawable.default_profile),
                    error = painterResource(id = R.drawable.default_profile)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            SettingsListCard {
                SettingsTab(
                    tabTitle = "갤러리에서 선택",
                    onClick = {}
                )
                SettingsDivider()
                SettingsTab(
                    tabTitle = "기본 이미지로 변경",
                    onClick = {}
                )
            }
        }
    }
}
