package com.killingpart.killingpoint.ui.screen.ProfileScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.killingpart.killingpoint.data.repository.AuthRepository
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.regex.Pattern
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.killingpart.killingpoint.R
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import com.killingpart.killingpoint.ui.theme.mainGreen
import com.killingpart.killingpoint.ui.viewmodel.UserUiState
import com.killingpart.killingpoint.ui.viewmodel.UserViewModel
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

@Composable
fun ProfileSettingsScreen(
    onDismiss: () -> Unit,
    topOffset: androidx.compose.ui.unit.Dp = 0.dp,
    maxHeight: androidx.compose.ui.unit.Dp = androidx.compose.ui.unit.Dp.Unspecified
) {
    val context = LocalContext.current
    val userViewModel: UserViewModel = viewModel()
    val userState by userViewModel.state.collectAsState()
    
    LaunchedEffect(Unit) {
        userViewModel.loadUserInfo(context)
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // 반투명 배경 (배경 클릭 시 닫기)
        Box(
            modifier = Modifier
                .fillMaxSize()
//                .background(Color.Black.copy(alpha = 0.5f))
                .clickable { onDismiss() }
        )
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (maxHeight != androidx.compose.ui.unit.Dp.Unspecified) {
                        Modifier.height(maxHeight)
                    } else {
                        Modifier.fillMaxHeight(0.85f)
                    }
                )
                .align(Alignment.TopCenter)
                .padding(horizontal = 16.dp)
                .offset(y = topOffset),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFDADADA))
        ) {
            ProfileSettingsContent(
                userState = userState,
                onDismiss = onDismiss,
                onTagUpdateSuccess = {
                    userViewModel.loadUserInfo(context)
                }
            )
        }
    }
}

@Composable
private fun ProfileSettingsContent(
    userState: UserUiState,
    onDismiss: () -> Unit,
    onTagUpdateSuccess: () -> Unit
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    val repo = remember { AuthRepository(context) }
    val userViewModel: UserViewModel = viewModel()
    
    var isEditingTag by remember { mutableStateOf(false) }
    var tagText by remember { mutableStateOf("") }
    var validationMessage by remember { mutableStateOf<String?>(null) }
    var isUpdating by remember { mutableStateOf(false) }
    
    // 프로필 이미지 업로드 상태
    var isUploadingImage by remember { mutableStateOf(false) }
    var imageUploadError by remember { mutableStateOf<String?>(null) }
    
    // Uri를 File로 변환하는 헬퍼 함수
    fun uriToFile(uri: Uri): File? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val file = File(context.cacheDir, "profile_image_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            file
        } catch (e: Exception) {
            android.util.Log.e("ProfileSettings", "파일 변환 실패: ${e.message}")
            null
        }
    }
    
    // presignedUrl에서 쿼리파라미터 제거
    fun removeQueryParams(url: String): String {
        return url.split("?").first()
    }
    
    // 이미지 업로드 플로우
    fun uploadProfileImage(imageUri: Uri) {
        if (isUploadingImage) return
        
        isUploadingImage = true
        imageUploadError = null
        
        scope.launch {
            try {
                // 1. Uri를 File로 변환
                val imageFile = uriToFile(imageUri)
                if (imageFile == null) {
                    imageUploadError = "이미지 파일을 읽을 수 없습니다."
                    isUploadingImage = false
                    return@launch
                }
                
                // 2. PresignedUrl 발급
                val presignedUrlResult = repo.getPresignedUrl()
                val presignedUrlResponse = presignedUrlResult.getOrElse {
                    imageUploadError = it.message ?: "PresignedUrl 발급 실패"
                    isUploadingImage = false
                    return@launch
                }
                
                // 3. S3에 이미지 업로드
                val uploadResult = repo.uploadImageToS3(
                    presignedUrlResponse.presignedUrl,
                    imageFile
                )
                uploadResult.getOrElse {
                    imageUploadError = it.message ?: "이미지 업로드 실패"
                    isUploadingImage = false
                    return@launch
                }
                
                // 4. 쿼리파라미터 제거한 presignedUrl
                val cleanUrl = removeQueryParams(presignedUrlResponse.presignedUrl)
                
                // 5. 프로필 이미지 변경
                val updateResult = repo.updateProfileImage(
                    presignedUrlResponse.id,
                    cleanUrl
                )
                updateResult.getOrElse {
                    imageUploadError = it.message ?: "프로필 이미지 변경 실패"
                    isUploadingImage = false
                    return@launch
                }
                
                // 6. 성공 - 사용자 정보 새로고침
                userViewModel.loadUserInfo(context)
                onTagUpdateSuccess()
                imageUploadError = null
                
            } catch (e: Exception) {
                imageUploadError = e.message ?: "프로필 이미지 업로드 실패"
                android.util.Log.e("ProfileSettings", "이미지 업로드 에러: ${e.message}", e)
            } finally {
                isUploadingImage = false
            }
        }
    }
    
    // 이미지 선택 launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            uploadProfileImage(it)
        }
    }
    
    // 이미지 선택 실행 함수
    fun pickImage() {
        imagePickerLauncher.launch(
            PickVisualMediaRequest(
                ActivityResultContracts.PickVisualMedia.ImageOnly
            )
        )
    }
    
    // 태그 유효성 검사
    fun validateTag(tag: String): String? {
        if (tag.isEmpty()) {
            return null
        }
        
        if (tag.length < 4 || tag.length > 30) {
            return "tag는 4자 이상 30자 이하이어야 합니다."
        }
        
        val pattern = Pattern.compile("^[a-z0-9_.]+$")
        if (!pattern.matcher(tag).matches()) {
            return "30자 이내의 영문과 숫자,\n 특수문자([.],[_])로 조합해주세요."
        }
        
        return null
    }
    
    // 태그 편집 시작
    fun startEditingTag(currentTag: String) {
        tagText = currentTag
        isEditingTag = true
        validationMessage = null
    }
    
    // 태그 편집 취소
    fun cancelEditingTag() {
        isEditingTag = false
        tagText = ""
        validationMessage = null
    }
    
    // 태그 업데이트
    fun updateTag(originalTag: String) {
        if (isUpdating) return
        
        val validationError = validateTag(tagText)
        if (validationError != null) {
            validationMessage = validationError
            return
        }
        
        if (tagText == originalTag) {
            cancelEditingTag()
            return
        }
        
        isUpdating = true
        scope.launch {
            try {
                repo.updateTag(tagText)
                    .onSuccess {
                        userViewModel.loadUserInfo(context)
                        onTagUpdateSuccess()
                        cancelEditingTag()
                    }
                    .onFailure { e ->
                        val errorMessage = e.message ?: "태그 업데이트 실패"
                        
                        // 에러 메시지에서 JSON 추출 시도
                        try {
                            // "태그 업데이트 실패 (400): {JSON}" 형식에서 JSON 추출
                            val jsonStart = errorMessage.indexOf("{")
                            if (jsonStart != -1) {
                                val jsonStr = errorMessage.substring(jsonStart)
                                val json = JSONObject(jsonStr)
                                
                                // 1. message 필드 확인 (중복 검사 등)
                                if (json.has("message")) {
                                    validationMessage = json.getString("message")
                                    isUpdating = false
                                    return@launch
                                }
                                
                                // 2. fieldErrors 확인 (유효성 검사 에러)
                                if (json.has("fieldErrors")) {
                                    val fieldErrors = json.getJSONArray("fieldErrors")
                                    if (fieldErrors.length() > 0) {
                                        // 모든 fieldErrors의 tag 메시지를 합침
                                        val errorMessages = mutableListOf<String>()
                                        for (i in 0 until fieldErrors.length()) {
                                            val errorObj = fieldErrors.getJSONObject(i)
                                            if (errorObj.has("tag")) {
                                                errorMessages.add(errorObj.getString("tag"))
                                            }
                                        }
                                        if (errorMessages.isNotEmpty()) {
                                            validationMessage = errorMessages.joinToString("\n")
                                            isUpdating = false
                                            return@launch
                                        }
                                    }
                                }
                            }
                        } catch (parseException: Exception) {
                            android.util.Log.e("ProfileSettings", "JSON 파싱 실패: ${parseException.message}")
                            // JSON 파싱 실패 시 원본 메시지 사용
                        }
                        
                        // JSON 파싱 실패하거나 형식이 다른 경우 원본 에러 메시지 사용
                        validationMessage = errorMessage
                    }
                isUpdating = false
            } catch (e: Exception) {
                validationMessage = e.message ?: "태그 업데이트 실패"
                isUpdating = false
            }
        }
    }
    
    // 실시간 검증
    LaunchedEffect(tagText) {
        if (isEditingTag && tagText.isNotEmpty() && userState is UserUiState.Success) {
            val currentTag = (userState as UserUiState.Success).userInfo.tag
            if (tagText != currentTag) {
                validationMessage = validateTag(tagText)
            } else {
                validationMessage = null
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        // 헤더
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "프로필 설정",
                color = mainGreen,
                fontFamily = PaperlogyFontFamily,
                fontWeight = FontWeight.W500,
                fontSize = 16.sp,


                )
            
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "닫기",
                    tint = mainGreen
                )
            }
        }
        
        Spacer(modifier = Modifier.height(15.dp))
        
        // 프로필 정보 영역
        when (val state = userState) {
            is UserUiState.Success -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
//                    verticalAlignment = Alignment.Start
                ) {
                    // 프로필 이미지 (클릭 가능)
                    Box {
                        AsyncImage(
                            model = state.userInfo.profileImageUrl,
                            contentDescription = "프로필 사진",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(50))
                                .border(3.dp, mainGreen, RoundedCornerShape(50))
                                .clickable(enabled = !isUploadingImage) {
                                    pickImage()
                                },
                            placeholder = painterResource(id = R.drawable.default_profile),
                            error = painterResource(id = R.drawable.default_profile)
                        )
                        
                        // 업로드 중 로딩 표시
                        if (isUploadingImage) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(Color.Black.copy(alpha = 0.6f)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(40.dp),
                                    color = mainGreen,
                                    strokeWidth = 3.dp
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(20.dp))
                    
                    // username과 tag
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = state.userInfo.username,
                            color = mainGreen,
                            fontFamily = PaperlogyFontFamily,
                            fontWeight = FontWeight.W400,
                            fontSize = 18.sp
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // 태그 영역 (인라인 편집)
                        Column {
                            // 입력 필드 또는 표시 영역
                            if (isEditingTag) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            color = Color(0xFF1A1A1A),
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "@ ",
                                        color = mainGreen,
                                        fontFamily = PaperlogyFontFamily,
                                        fontSize = 14.sp
                                    )
                                    
                                    androidx.compose.foundation.text.BasicTextField(

                                        value = tagText,
                                        onValueChange = { newValue ->
                                            tagText = newValue.lowercase()
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 4.dp),
                                        textStyle = androidx.compose.ui.text.TextStyle(
                                            color = mainGreen,
                                            fontFamily = PaperlogyFontFamily,
                                            fontSize = 14.sp
                                        ),
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Text,
                                            imeAction = ImeAction.Done
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onDone = {
                                                keyboardController?.hide()
                                                updateTag(state.userInfo.tag)
                                            }
                                        ),
                                        cursorBrush = androidx.compose.ui.graphics.SolidColor(mainGreen),

                                    )
                                }
                            } else {
                                Row(
                                    modifier = Modifier
                                        .clickable { startEditingTag(state.userInfo.tag) }
                                        .fillMaxWidth()
                                        .background(
                                            color = Color(0xFF1A1A1A),
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "@",
                                            color = mainGreen,
                                            fontFamily = PaperlogyFontFamily,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = state.userInfo.tag,
                                            color = mainGreen,
                                            fontFamily = PaperlogyFontFamily,
                                            fontSize = 14.sp
                                        )
                                    }
                                    
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "태그 수정",
                                        tint = mainGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            
                            // 하단 버튼 영역 (항상 공간 확보)
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(32.dp), // 고정 높이로 레이아웃 시프트 방지
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isEditingTag) {
                                    if (isUpdating) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = mainGreen,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        IconButton(
                                            onClick = { cancelEditingTag() },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "취소",
                                                tint = mainGreen,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        
                                        Spacer(modifier = Modifier.width(8.dp))
                                        
                                        IconButton(
                                            onClick = { updateTag(state.userInfo.tag) },
                                            enabled = tagText.isNotEmpty() && 
                                                     tagText != state.userInfo.tag && 
                                                     validateTag(tagText) == null,
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "저장",
                                                tint = if (tagText.isNotEmpty() && 
                                                          tagText != state.userInfo.tag && 
                                                          validateTag(tagText) == null) {
                                                    mainGreen
                                                } else {
                                                    Color.Gray
                                                },
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            
                            // 검증 메시지 또는 성공 메시지 (항상 공간 확보)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp) // 여러 줄 텍스트를 위한 높이 증가
                                    .padding(start = 12.dp, top = 4.dp),
                                contentAlignment = Alignment.TopStart
                            ) {
                                if (isEditingTag) {
                                    if (validationMessage != null) {
                                        Text(
                                            text = validationMessage!!,
                                            color = if (validationMessage!!.contains("사용 가능") || validationMessage!!.contains("성공")) {
                                                Color(0xFF4FDD79)
                                            } else {
                                                Color(0xFFFF6B6B)
                                            },
                                            fontFamily = PaperlogyFontFamily,
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp,
                                            maxLines = 2
                                        )
                                    } else if (tagText.isNotEmpty() && tagText != state.userInfo.tag && validateTag(tagText) == null) {
                                        Text(
                                            text = "사용 가능한 회원태그입니다!",
                                            color = Color(0xFF4FDD79),
                                            fontFamily = PaperlogyFontFamily,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 프로필 사진 라벨 및 에러 메시지
                Column(
                    modifier = Modifier
                        .padding(start = 10.dp, top = 4.dp)
                        .offset(y = (-40).dp)
                ) {
                    Text(
                        text = "프로필 사진",
                        color = mainGreen,
                        fontFamily = PaperlogyFontFamily,
                        fontSize = 12.sp
                    )
                    
                    // 이미지 업로드 에러 메시지
                    if (imageUploadError != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = imageUploadError!!,
                            color = Color(0xFFFF6B6B),
                            fontFamily = PaperlogyFontFamily,
                            fontSize = 11.sp,
                            lineHeight = 14.sp,
                            maxLines = 2
                        )
                    }
                }
            }
            
            is UserUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "로딩 중...",
                        color = mainGreen,
                        fontFamily = PaperlogyFontFamily
                    )
                }
            }
            
            is UserUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "오류: ${state.message}",
                        color = Color.Red,
                        fontFamily = PaperlogyFontFamily
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // 로그아웃 / 회원탈퇴
        Text(
            text = "로그아웃 / 회원탈퇴",
            color = Color.White,
            fontFamily = PaperlogyFontFamily,
            fontSize = 14.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

