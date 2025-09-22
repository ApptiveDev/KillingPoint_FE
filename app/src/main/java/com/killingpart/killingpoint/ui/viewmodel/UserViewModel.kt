package com.killingpart.killingpoint.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.killingpart.killingpoint.data.model.UserInfo
import com.killingpart.killingpoint.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface UserUiState {
    data object Loading : UserUiState
    data class Success(val userInfo: UserInfo) : UserUiState
    data class Error(val message: String) : UserUiState
}

class UserViewModel(
    private val repoFactory: (Context) -> AuthRepository = { ctx ->
        AuthRepository(ctx)
    }
) : ViewModel() {

    private val _state = MutableStateFlow<UserUiState>(UserUiState.Loading)
    val state: StateFlow<UserUiState> = _state

    fun loadUserInfo(context: Context) {
        _state.value = UserUiState.Loading
        val repo = repoFactory(context)
        viewModelScope.launch {
            repo.getUserInfo()
                .onSuccess { userInfo ->
                    _state.value = UserUiState.Success(userInfo)
                }
                .onFailure { 
                    _state.value = UserUiState.Error(it.message ?: "사용자 정보 로드 실패")
                }
        }
    }
}
