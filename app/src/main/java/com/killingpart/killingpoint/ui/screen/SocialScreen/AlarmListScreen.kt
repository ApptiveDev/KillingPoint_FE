package com.killingpart.killingpoint.ui.screen.SocialScreen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.killingpart.killingpoint.analytics.NotificationAnalytics
import com.killingpart.killingpoint.analytics.SubTabAnalytics
import com.killingpart.killingpoint.data.model.AlarmDeepLink
import com.killingpart.killingpoint.data.repository.AuthRepository
import com.killingpart.killingpoint.R
import com.killingpart.killingpoint.navigation.handleAlarmNavigation
import com.killingpart.killingpoint.ui.component.AppBackground
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import com.killingpart.killingpoint.ui.theme.mainGreen
import com.killingpart.killingpoint.ui.viewmodel.AlarmUiState
import com.killingpart.killingpoint.ui.viewmodel.AlarmViewModel
import kotlinx.coroutines.launch

@Composable
fun AlarmListScreen(navController: NavController, entrySource: String = "") {
    val alarmViewModel: AlarmViewModel = viewModel()
    val alarmState by alarmViewModel.state.collectAsState()
    val isSelectionMode by alarmViewModel.isSelectionMode.collectAsState()
    val selectedIds by alarmViewModel.selectedIds.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val repo = remember { AuthRepository(context) }
    val coroutineScope = rememberCoroutineScope()
    var opening by remember { mutableStateOf(false) }
    var hasTrackedListViewed by remember { mutableStateOf(false) }
    val notificationEntryPoint = when (entrySource) {
        "social_tab" -> NotificationAnalytics.EntryPoint.SOCIAL_TAB
        "push" -> NotificationAnalytics.EntryPoint.PUSH
        else -> NotificationAnalytics.EntryPoint.UNKNOWN
    }

    LaunchedEffect(Unit) {
        if (entrySource == "social_tab") {
            SubTabAnalytics.selectSubTab(SubTabAnalytics.Tab.SOCIAL, SubTabAnalytics.SubTab.NOTIFICATION)
        } else {
            SubTabAnalytics.enterTab(SubTabAnalytics.Tab.SOCIAL, SubTabAnalytics.SubTab.NOTIFICATION)
        }
        alarmViewModel.loadAlarms(context)
    }

    LaunchedEffect(alarmState) {
        val state = alarmState as? AlarmUiState.Success ?: return@LaunchedEffect
        if (hasTrackedListViewed) return@LaunchedEffect
        hasTrackedListViewed = true
        NotificationAnalytics.notificationListViewed(
            entryPoint = notificationEntryPoint,
            unreadCount = state.alarms.count { !it.isRead }
        )
    }

    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                alarmViewModel.loadAlarms(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            SubTabAnalytics.onScreenDisappeared(SubTabAnalytics.Tab.SOCIAL)
        }
    }

    AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(horizontal = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp)
            ) {
                IconButton(
                    onClick = {
                        SubTabAnalytics.leaveNotificationBackToSocial()
                        navController.popBackStack()
                    },
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "뒤로가기",
                        tint = Color.White
                    )
                }

                Row(
                    modifier = Modifier.align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_bell),
                        contentDescription = "알림",
                        tint = mainGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "알림 목록",
                        color = mainGreen,
                        fontFamily = PaperlogyFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 18.sp
                    )
                }

                Text(
                    text = if (isSelectionMode) "취소" else "선택",
                    color = Color.White,
                    fontFamily = PaperlogyFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { alarmViewModel.setSelectionMode(!isSelectionMode) }
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                )
            }

            if (isSelectionMode) {
                val currentAlarms = (alarmState as? AlarmUiState.Success)?.alarms.orEmpty()
                val allSelected = currentAlarms.isNotEmpty() && selectedIds.size == currentAlarms.size
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SelectionChip(
                        text = if (allSelected) "전체 해제" else "전체 선택",
                        onClick = { alarmViewModel.toggleSelectAll() }
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    SelectionChip(
                        text = "선택 삭제",
                        onClick = { alarmViewModel.deleteSelected(context) },
                        enabled = selectedIds.isNotEmpty(),
                        highlighted = true
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    SelectionChip(
                        text = "모두 삭제",
                        onClick = { alarmViewModel.deleteAll(context) },
                        enabled = currentAlarms.isNotEmpty()
                    )
                }
            }

            when (val state = alarmState) {
                is AlarmUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = mainGreen)
                    }
                }
                is AlarmUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = state.message,
                            color = Color.White,
                            fontFamily = PaperlogyFontFamily
                        )
                    }
                }
                is AlarmUiState.Success -> {
                    if (state.alarms.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "알림 목록이 비어있습니다",
                                color = Color.White,
                                fontFamily = PaperlogyFontFamily
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = if (isSelectionMode) 16.dp else 36.dp),
                            verticalArrangement = Arrangement.spacedBy(0.dp)
                        ) {
                            itemsIndexed(state.alarms, key = { _, item -> item.alarm.alarmId }) { index, item ->
                                val alarm = item.alarm
                                val isSelected = alarm.alarmId in selectedIds
                                val textColor = if (item.isRead) Color(0xFFA4A4A6) else Color.White
                                val isNavigable = remember(alarm.alarmId, alarm.type, alarm.deepLink) {
                                    AlarmDeepLink.isNavigable(alarm.type, alarm.deepLink)
                                }
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable(enabled = isSelectionMode || !opening) {
                                                if (isSelectionMode) {
                                                    alarmViewModel.toggleSelection(alarm.alarmId)
                                                    return@clickable
                                                }
                                                NotificationAnalytics.notificationSelected(
                                                    notificationType = NotificationAnalytics.NotificationType.fromAlarmType(alarm.type),
                                                    notificationId = alarm.alarmId.toString(),
                                                    listPosition = index
                                                )
                                                // 탭하면 이동 가능 여부와 무관하게 항상 읽음(회색) 처리
                                                alarmViewModel.markAlarmRead(context, alarm.alarmId)
                                                // 이동은 딥링크가 유효한 알림에서만
                                                if (isNavigable) {
                                                    opening = true
                                                    coroutineScope.launch {
                                                        try {
                                                            handleAlarmNavigation(
                                                                navController = navController,
                                                                type = alarm.type,
                                                                deepLink = alarm.deepLink,
                                                                repo = repo,
                                                                entryPoint = NotificationAnalytics.EntryPoint.NOTIFICATION_LIST,
                                                                onError = { msg ->
                                                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                                                }
                                                            )
                                                        } finally {
                                                            opening = false
                                                        }
                                                    }
                                                }
                                            }
                                            .padding(vertical = 16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (isSelectionMode) {
                                            SelectionRadio(isSelected = isSelected)
                                        }
                                        Text(
                                            text = alarm.content,
                                            color = textColor,
                                            fontFamily = PaperlogyFontFamily,
                                            fontWeight = FontWeight.Normal,
                                            fontSize = 13.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = formatAlarmDate(alarm.createDate),
                                            color = Color(0xFFA4A4A6),
                                            fontFamily = PaperlogyFontFamily,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 10.sp
                                        )
                                    }

                                    if (index != state.alarms.lastIndex) {
                                        HorizontalDivider(
                                            thickness = 1.dp,
                                            color = Color(0xFF2A2A2C),
                                            modifier = Modifier.height(1.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectionRadio(isSelected: Boolean) {
    Box(
        modifier = Modifier
            .size(12.dp)
            .clip(CircleShape)
            .then(
                if (isSelected) {
                    Modifier.background(mainGreen)
                } else {
                    Modifier.border(1.dp, Color(0xFF6B6B6D), CircleShape)
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

@Composable
private fun SelectionChip(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    highlighted: Boolean = false
) {
    val backgroundColor = if (highlighted) {
        mainGreen.copy(alpha = if (enabled) 1f else 0.35f)
    } else {
        Color.White.copy(alpha = 0.12f)
    }
    val textColor = if (highlighted) Color.Black else Color.White.copy(alpha = 0.9f)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(backgroundColor)
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontFamily = PaperlogyFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp
        )
    }
}

private fun formatAlarmDate(raw: String?): String {
    if (raw.isNullOrBlank()) return ""
    return when {
        raw.length >= 10 && raw[4] == '-' && raw[7] == '-' -> {
            val month = raw.substring(5, 7)
            val day = raw.substring(8, 10)
            "$month.$day"
        }
        else -> raw
    }
}
