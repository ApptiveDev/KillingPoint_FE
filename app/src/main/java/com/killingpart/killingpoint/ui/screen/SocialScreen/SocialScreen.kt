package com.killingpart.killingpoint.ui.screen.SocialScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.runtime.saveable.rememberSaveable
import com.killingpart.killingpoint.R
import com.killingpart.killingpoint.ui.component.AppBackground
import com.killingpart.killingpoint.ui.component.BottomBar
import com.killingpart.killingpoint.ui.screen.MainScreen.TopPillTabs
import com.killingpart.killingpoint.ui.viewmodel.AlarmViewModel

enum class SocialTab {
    FEED, FRIEND
}

@Composable
fun SocialScreen(
    navController: NavController,
    initialTab: String = "feed",
    initialFriendListTab: String = "picks"
) {
    val alarmViewModel: AlarmViewModel = viewModel()
    val hasUnread by alarmViewModel.hasUnread.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val initialFriendTabEnum = when (initialFriendListTab.lowercase()) {
        "fans", "fandom" -> FriendTab.FANS
        else -> FriendTab.PICKS
    }

    var selectedTab by rememberSaveable(initialTab) {
        mutableStateOf(
            when (initialTab) {
                "friend" -> SocialTab.FRIEND
                else -> SocialTab.FEED
            }
        )
    }

    LaunchedEffect(Unit) {
        alarmViewModel.refreshAlarmFlag(context)
    }

    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                alarmViewModel.refreshAlarmFlag(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    AppBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            // 친구 탭일 때만 단색 배경 적용
            if (selectedTab == SocialTab.FRIEND) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF1D1E20))
                )
            }
            
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(35.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(horizontal = 16.dp)
                        .height(45.dp)
                        .clip(RoundedCornerShape(34.dp))
                        .background(Color(0xFF101010)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TopPillTabs(
                        options = listOf("피드", "친구"),
                        selectedIndex = when (selectedTab) {
                            SocialTab.FEED -> 0
                            SocialTab.FRIEND -> 1
                        },
                        onSelected = { idx ->
                            selectedTab = when (idx) {
                                0 -> SocialTab.FEED
                                else -> SocialTab.FRIEND
                            }
                        },
                        modifier = Modifier.weight(1f),
                        height = 54.dp,
                        containerColor = Color.Transparent,
                        indicatorColor = Color(0xFFEEEFF3),
                        selectedTextColor = Color.Black,
                        unselectedTextColor = Color(0xFF7B7B7B),
                        cornerRadius = 30.dp
                    )
                    Box(
                        modifier = Modifier
                            .width(54.dp)
                            .fillMaxHeight()
                            .clickable { navController.navigate("alarm_list") },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(
                                id = if (hasUnread) {
                                    R.drawable.ic_noti_true_without_bg
                                } else {
                                    R.drawable.ic_bell
                                }
                            ),
                            contentDescription = "알림 목록 진입",
                            modifier = Modifier.size(if (hasUnread) 24.dp else 18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    when (selectedTab) {
                        SocialTab.FEED -> FeedScreen(navController)
                        SocialTab.FRIEND -> FriendScreen(
                            navController = navController,
                            initialListTab = initialFriendTabEnum
                        )
                    }
                }

                BottomBar(navController = navController)
            }
        }
    }
}
