package com.killingpart.killingpoint.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.killingpart.killingpoint.data.model.AlarmItem
import com.killingpart.killingpoint.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface AlarmUiState {
    data object Loading : AlarmUiState
    data class Success(val alarms: List<AlarmItem>) : AlarmUiState
    data class Error(val message: String) : AlarmUiState
}

class AlarmViewModel(
    private val repoFactory: (Context) -> AuthRepository = { ctx ->
        AuthRepository(ctx)
    }
) : ViewModel() {
    private val _state = MutableStateFlow<AlarmUiState>(AlarmUiState.Loading)
    val state: StateFlow<AlarmUiState> = _state

    private val _hasAlarm = MutableStateFlow(false)
    val hasAlarm: StateFlow<Boolean> = _hasAlarm

    fun loadAlarms(context: Context, size: Int = 20) {
        _state.value = AlarmUiState.Loading
        val repo = repoFactory(context)
        viewModelScope.launch {
            repo.getAlarms(page = 0, size = size)
                .onSuccess { response ->
                    _hasAlarm.value = response.content.isNotEmpty()
                    _state.value = AlarmUiState.Success(response.content)
                }
                .onFailure { e ->
                    _state.value = AlarmUiState.Error(e.message ?: "알림 목록 조회 실패")
                }
        }
    }

    fun refreshAlarmFlag(context: Context) {
        val repo = repoFactory(context)
        viewModelScope.launch {
            repo.getAlarms(page = 0, size = 1)
                .onSuccess { response ->
                    _hasAlarm.value = response.page.totalElements > 0
                }
                .onFailure {
                    _hasAlarm.value = false
                }
        }
    }
}
