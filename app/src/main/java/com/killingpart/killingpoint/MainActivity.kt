package com.killingpart.killingpoint

import android.Manifest
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Color as AndroidColor
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.kakao.sdk.common.KakaoSdk
import com.killingpart.killingpoint.BuildConfig
import com.killingpart.killingpoint.analytics.EngagementAnalytics
import com.killingpart.killingpoint.analytics.OnboardingAnalytics
import com.killingpart.killingpoint.data.repository.AuthRepository
import com.killingpart.killingpoint.navigation.NavGraph
import com.killingpart.killingpoint.navigation.OnboardingProgressStore
import com.killingpart.killingpoint.navigation.handleAlarmNavigation
import com.killingpart.killingpoint.notification.FcmTokenSync
import com.killingpart.killingpoint.ui.component.VideoSplashScreen
import com.killingpart.killingpoint.ui.viewmodel.LoginViewModel
import com.killingpart.killingpoint.ui.viewmodel.LoginUiState

class MainActivity : ComponentActivity() {

    enum class LaunchState {
        SPLASH,
        MAIN
    }

    private val _pendingAlarmType = mutableStateOf("")
    private val _pendingDeepLink = mutableStateOf("")

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val type = intent.getStringExtra("type").orEmpty()
        val deepLink = intent.getStringExtra("deepLink").orEmpty()
        if (type.isNotBlank() && deepLink.isNotBlank()) {
            _pendingAlarmType.value = type
            _pendingDeepLink.value = deepLink
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        KakaoSdk.init(this, getString(R.string.kakao_native_app_key))
        OnboardingAnalytics.appOpened()
        requestNotificationPermissionIfNeeded()
        if (savedInstanceState == null) {
            val type = intent.getStringExtra("type").orEmpty()
            val deepLink = intent.getStringExtra("deepLink").orEmpty()
            if (type.isNotBlank() && deepLink.isNotBlank()) {
                _pendingAlarmType.value = type
                _pendingDeepLink.value = deepLink
            }
        }
        enableEdgeToEdge()
        window.statusBarColor = AndroidColor.BLACK
        window.navigationBarColor = AndroidColor.BLACK
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        setContent {
            val context = LocalContext.current
            val loginViewModel: LoginViewModel = viewModel()
            val loginState by loginViewModel.state.collectAsState()

            val pendingAlarmType = _pendingAlarmType.value
            val pendingDeepLink = _pendingDeepLink.value

            var launchState by remember {
                mutableStateOf(LaunchState.SPLASH)
            }

            var canFinishSplash by remember {
                mutableStateOf(false)
            }
            var resolvedStartDestination by rememberSaveable {
                mutableStateOf<String?>(null)
            }

            var previousLoginState by remember {
                mutableStateOf<LoginUiState?>(null)
            }

            LaunchedEffect(Unit) {
                loginViewModel.tryAutoLogin(context)
            }

            LaunchedEffect(loginState) {
                when (val state = loginState) {
                    is LoginUiState.AutoLoginSuccess,
                    is LoginUiState.Idle,
                    is LoginUiState.Error,
                    is LoginUiState.Success -> {
                        canFinishSplash = true
                    }
                    is LoginUiState.Loading -> {
                        canFinishSplash = false
                    }
                }
            }

            LaunchedEffect(loginState, context) {
                when (val s = loginState) {
                    is LoginUiState.AutoLoginSuccess -> {
                        if (previousLoginState !is LoginUiState.AutoLoginSuccess) {
                            OnboardingAnalytics.authCompleted(
                                provider = s.provider,
                                isNewUser = s.isNew
                            )
                        }
                        FcmTokenSync.syncCurrentToken(context)
                        val repo = AuthRepository(context)
                        val start = repo.getUserInitSettings()
                            .getOrNull()
                            ?.let { init ->
                                when {
                                    init.needsPolicyAgreement -> "onboarding_policy"
                                    init.needsTagSetup -> "onboarding_name"
                                    OnboardingProgressStore.isTutorialInProgress(context) -> "onboarding_kp_intro"
                                    else -> "main"
                                }
                            } ?: "home"
                        resolvedStartDestination = start
                    }

                    is LoginUiState.Idle, is LoginUiState.Error -> {
                        resolvedStartDestination = "home"
                    }

                    is LoginUiState.Success -> {
                        FcmTokenSync.syncCurrentToken(context)
                        resolvedStartDestination = "home"
                    }

                    is LoginUiState.Loading -> {
                        resolvedStartDestination = null
                    }
                }
                previousLoginState = loginState
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                when (launchState) {
                    LaunchState.SPLASH -> {
                        VideoSplashScreen(
                            onFinish = {
                                launchState = LaunchState.MAIN
                            },
                            canFinish = canFinishSplash
                        )
                    }

                    LaunchState.MAIN -> {
                        val navController = rememberNavController()

                        val startDestination = resolvedStartDestination ?: "home"

                        LaunchedEffect(startDestination, resolvedStartDestination) {
                            if (resolvedStartDestination != null && startDestination != "home") {
                                if (startDestination == "main" || startDestination.startsWith("main?")) {
                                    EngagementAnalytics.markAppOpenedOnMyTab()
                                }
                                navController.navigate(startDestination) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        }

                        LaunchedEffect(pendingAlarmType, pendingDeepLink, resolvedStartDestination) {
                            if (pendingAlarmType.isBlank() || pendingDeepLink.isBlank()) return@LaunchedEffect
                            val dest = resolvedStartDestination ?: return@LaunchedEffect
                            if (!dest.startsWith("main")) return@LaunchedEffect
                            val repo = AuthRepository(context)
                            handleAlarmNavigation(
                                navController = navController,
                                type = pendingAlarmType,
                                deepLink = pendingDeepLink,
                                repo = repo
                            )
                            // 네트워크 콜(suspension point) 이전에 지우면 코루틴이 취소되므로 반드시 이후에 지운다
                            _pendingAlarmType.value = ""
                            _pendingDeepLink.value = ""
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .navigationBarsPadding()
                        ) {
                            NavGraph(
                                navController = navController,
                                startDestination = startDestination,
                                loginViewModel = loginViewModel
                            )
                            if (BuildConfig.DEBUG && BuildConfig.SHOW_DEV_MENU) {
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .statusBarsPadding()
                                        .padding(top = 8.dp, end = 8.dp),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    TextButton(
                                        onClick = { navController.navigate("onboarding_policy") }
                                    ) { Text("약관 화면") }
                                    TextButton(
                                        onClick = { navController.navigate("onboarding_name") }
                                    ) { Text("이름·태그") }
                                    TextButton(
                                        onClick = { navController.navigate("onboarding_kp_intro") }
                                    ) { Text("튜토리얼 시작") }
                                    TextButton(
                                        onClick = { navController.navigate("add_music?tutorial=true") }
                                    ) { Text("곡 검색 튜토리얼") }
                                    TextButton(
                                        onClick = { navController.navigate("onboarding_home_preview") }
                                    ) { Text("홈 프리뷰 튜토리얼") }
                                    TextButton(
                                        onClick = { navController.navigate("onboarding_finish") }
                                    ) { Text("마지막 화면") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            1001
        )
    }
}
