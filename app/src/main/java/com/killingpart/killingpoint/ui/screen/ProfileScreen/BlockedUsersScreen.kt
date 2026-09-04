package com.killingpart.killingpoint.ui.screen.ProfileScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.killingpart.killingpoint.R
import com.killingpart.killingpoint.data.model.SubscribeUser
import com.killingpart.killingpoint.data.repository.AuthRepository
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import kotlinx.coroutines.launch

@Composable
fun BlockedUsersScreen(navController: NavController) {
    val context = LocalContext.current
    val repo = remember { AuthRepository(context) }
    val scope = rememberCoroutineScope()
    var blockedUsers by remember { mutableStateOf<List<SubscribeUser>>(emptyList()) }
    var totalBlockedCount by remember { mutableStateOf(0) }
    var searchText by remember { mutableStateOf("") }
    var selectedUser by remember { mutableStateOf<SubscribeUser?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isUnblocking by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun loadBlockedUsers() {
        scope.launch {
            isLoading = true
            errorMessage = null
            repo.getBlockedUsers(page = 0, size = 50)
                .onSuccess {
                    blockedUsers = it.content
                    totalBlockedCount = it.page.totalElements
                }
                .onFailure {
                    errorMessage = it.message ?: "차단 목록을 불러오지 못했습니다."
                }
            isLoading = false
        }
    }

    fun unblockSelectedUser() {
        val target = selectedUser ?: return
        if (isUnblocking) return
        scope.launch {
            isUnblocking = true
            repo.unblockUser(target.userId)
                .onSuccess {
                    selectedUser = null
                    loadBlockedUsers()
                }
                .onFailure {
                    errorMessage = it.message ?: "차단 해제에 실패했습니다."
                }
            isUnblocking = false
        }
    }

    LaunchedEffect(Unit) {
        loadBlockedUsers()
    }

    val filteredUsers = remember(blockedUsers, searchText) {
        val query = searchText.trim()
        if (query.isEmpty()) {
            blockedUsers
        } else {
            blockedUsers.filter {
                it.username.contains(query, ignoreCase = true) ||
                    it.tag.contains(query, ignoreCase = true)
            }
        }
    }

    SettingsBackgroundBox {
        BlockedUsersContent(
            totalBlockedCount = totalBlockedCount,
            searchText = searchText,
            onSearchTextChange = { searchText = it },
            users = filteredUsers,
            isLoading = isLoading,
            errorMessage = errorMessage,
            onBack = { navController.popBackStack() },
            onUnblockClick = { selectedUser = it }
        )

        selectedUser?.let { user ->
            UnblockUserModal(
                username = user.username,
                isLoading = isUnblocking,
                onDismiss = { selectedUser = null },
                onConfirm = { unblockSelectedUser() }
            )
        }
    }
}

@Composable
private fun BlockedUsersContent(
    totalBlockedCount: Int,
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    users: List<SubscribeUser>,
    isLoading: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onUnblockClick: (SubscribeUser) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SettingsBackground)
            .padding(horizontal = 16.dp)
    ) {
        SettingsTopBar(
            title = "차단 목록",
            onBack = onBack
        )

        Text(
            text = "총 ${totalBlockedCount}명",
            color = Color(0xFF8E8E93),
            fontFamily = PaperlogyFontFamily,
            fontSize = 11.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(12.dp))

        BlockedUserSearchBar(
            value = searchText,
            onValueChange = onSearchTextChange
        )

        Spacer(modifier = Modifier.height(14.dp))

        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }

            errorMessage != null -> {
                Text(
                    text = errorMessage,
                    color = Color(0xFFFF5A5A),
                    fontFamily = PaperlogyFontFamily,
                    fontSize = 12.sp
                )
            }

            users.isEmpty() -> {
                Text(
                    text = "차단한 사용자가 없습니다.",
                    color = Color(0xFF8E8E93),
                    fontFamily = PaperlogyFontFamily,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 20.dp, start = 20.dp)
                )
            }

            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(users, key = { it.userId }) { user ->
                        BlockedUserItem(
                            user = user,
                            onUnblockClick = { onUnblockClick(user) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "차단된 사용자는 회원님의 킬링파트 및 컬렉션을 확인할 수 없습니다.",
                            color = Color(0xFF8E8E93),
                            fontFamily = PaperlogyFontFamily,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BlockedUserSearchBar(
    value: String,
    onValueChange: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(38.dp)
            .background(Color(0xFF121212), RoundedCornerShape(18.dp))
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.search),
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(
                    color = Color.White,
                    fontFamily = PaperlogyFontFamily,
                    fontSize = 12.sp
                ),
                singleLine = true,
                cursorBrush = SolidColor(Color.White),
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(
                            text = "검색",
                            color = Color(0xFF6A6A6A),
                            fontFamily = PaperlogyFontFamily,
                            fontSize = 12.sp
                        )
                    }
                    innerTextField()
                }
            )
        }
    }
}

@Composable
fun BlockedUserItem(
    user: SubscribeUser,
    onUnblockClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp)
            .background(Color(0xFF121212), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = user.profileImageUrl,
            contentDescription = "프로필 이미지",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape),
            placeholder = painterResource(id = R.drawable.default_profile),
            error = painterResource(id = R.drawable.default_profile)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 10.dp)
        ) {
            Text(
                text = user.username,
                color = Color.White,
                fontFamily = PaperlogyFontFamily,
                fontWeight = FontWeight.W600,
                fontSize = 12.sp
            )
            Text(
                text = "@${user.tag}",
                color = Color(0xFF8E8E93),
                fontFamily = PaperlogyFontFamily,
                fontSize = 10.sp
            )
        }

        Text(
            text = "차단 해제",
            color = Color(0xFFFF5A5A),
            fontFamily = PaperlogyFontFamily,
            fontSize = 10.sp,
            modifier = Modifier
                .background(color = Color(0xFF292929))
                .border(1.dp, Color(0xFFFF5A5A), RoundedCornerShape(18.dp))
                .clip(RoundedCornerShape(18.dp))
                .clickable { onUnblockClick() }
                .padding(horizontal = 14.dp, vertical = 6.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnblockUserModal(
    username: String,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF242424),
        shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "!",
                color = Color(0xFFFF5A5A),
                fontFamily = PaperlogyFontFamily,
                fontWeight = FontWeight.W700,
                fontSize = 28.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "${username}님을 차단 해제 할까요?",
                color = Color.White,
                fontFamily = PaperlogyFontFamily,
                fontWeight = FontWeight.W600,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "이제 상대방이 검색할 수 있습니다.\n상대방에게 알림이 가지 않습니다.",
                color = Color(0xFF8E8E93),
                fontFamily = PaperlogyFontFamily,
                fontSize = 12.sp,
                lineHeight = 14.sp
            )
            Spacer(modifier = Modifier.height(22.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "취소",
                    color = Color.White,
                    fontFamily = PaperlogyFontFamily,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .background(Color(0xFF333333), RoundedCornerShape(12.dp))
                        .clickable(enabled = !isLoading) { onDismiss() }
                        .padding(vertical = 14.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Text(
                    text = if (isLoading) "처리 중..." else "해제",
                    color = Color.White,
                    fontFamily = PaperlogyFontFamily,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .background(Color(0xFFFF646A), RoundedCornerShape(12.dp))
                        .clickable(enabled = !isLoading) { onConfirm() }
                        .padding(vertical = 14.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212, widthDp = 360, heightDp = 800)
@Composable
private fun BlockedUsersScreenPreview() {
    val users = listOf(
        SubscribeUser(
            userId = 1,
            username = "김기영",
            tag = "angry",
            identifier = "KAKAO-1",
            profileImageUrl = "",
            userRoleType = "USER",
            socialType = "KAKAO"
        ),
        SubscribeUser(
            userId = 2,
            username = "이진원",
            tag = "thegreatjinwon",
            identifier = "KAKAO-2",
            profileImageUrl = "",
            userRoleType = "USER",
            socialType = "KAKAO"
        ),
        SubscribeUser(
            userId = 3,
            username = "박민수",
            tag = "minsu_01",
            identifier = "KAKAO-3",
            profileImageUrl = "",
            userRoleType = "USER",
            socialType = "KAKAO"
        )
    )

    SettingsBackgroundBox {
        BlockedUsersContent(
            totalBlockedCount = users.size,
            searchText = "",
            onSearchTextChange = {},
            users = users,
            isLoading = false,
            errorMessage = null,
            onBack = {},
            onUnblockClick = {}
        )
    }
}
