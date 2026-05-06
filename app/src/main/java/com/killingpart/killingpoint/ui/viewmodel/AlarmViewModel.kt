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
            loadAllAlarmPages(repo, size)
                .onSuccess { alarms ->
                    _hasAlarm.value = alarms.isNotEmpty()
                    _state.value = AlarmUiState.Success(alarms)
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

    private suspend fun loadAllAlarmPages(
        repo: AuthRepository,
        size: Int
    ): Result<List<AlarmItem>> {
        val firstPageResult = repo.getAlarms(page = 0, size = size)
        val firstPage = firstPageResult.getOrElse { return Result.failure(it) }

        val totalPages = firstPage.page.totalPages.coerceAtLeast(1)
        val merged = firstPage.content.toMutableList()

        for (page in 1 until totalPages) {
            val pageResult = repo.getAlarms(page = page, size = size)
            val pageResponse = pageResult.getOrElse { return Result.failure(it) }
            merged += pageResponse.content
        }

        return Result.success(merged)
    }
}
