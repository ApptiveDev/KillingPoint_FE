package com.killingpart.killingpoint.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.killingpart.killingpoint.auth.KakaoLoginClient
import com.killingpart.killingpoint.data.local.TokenStore
import com.killingpart.killingpoint.data.remote.RetrofitClient
import com.killingpart.killingpoint.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface LoginUiState {
    data object Idle : LoginUiState
    data object Loading : LoginUiState
    data object Success : LoginUiState
    data class Error(val message: String) : LoginUiState
}

class LoginViewModel(
    private val repoFactory: (Context) -> AuthRepository = { ctx ->
        AuthRepository(
            api = RetrofitClient.api,
            tokenStore = TokenStore(ctx.applicationContext)
        )
    }
) : ViewModel() {

    private val _state = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val state: StateFlow<LoginUiState> = _state

    fun loginWithKakao(context: Context, onSuccess: (String) -> Unit) {
        _state.value = LoginUiState.Loading
        viewModelScope.launch {
            try {
                val kakaoAccessToken = KakaoLoginClient.getAccessToken(context)
                onSuccess(kakaoAccessToken)
            } catch (e: Exception) {
                _state.value = LoginUiState.Error(e.message ?: "카카오 로그인 실패")
            }
        }
    }

    fun loginWithServer(context: Context, kakaoAccessToken: String) {
        _state.value = LoginUiState.Loading
        val repo = repoFactory(context)
        viewModelScope.launch {
            repo.exchangeKakaoAccessToken(kakaoAccessToken)
                .onSuccess { _state.value = LoginUiState.Success }
                .onFailure { _state.value = LoginUiState.Error(it.message ?: "로그인 실패") }
        }
    }
}
