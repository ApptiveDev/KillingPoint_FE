package com.killingpart.killingpoint.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.killingpart.killingpoint.data.local.AlarmReadStore
import com.killingpart.killingpoint.data.model.AlarmItem
import com.killingpart.killingpoint.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface AlarmUiState {
    data object Loading : AlarmUiState
    data class Success(val alarms: List<AlarmUiItem>) : AlarmUiState
    data class Error(val message: String) : AlarmUiState
}

data class AlarmUiItem(
    val alarm: AlarmItem,
    val isRead: Boolean
)

class AlarmViewModel(
    private val repoFactory: (Context) -> AuthRepository = { ctx ->
        AuthRepository(ctx)
    }
) : ViewModel() {
    private val _state = MutableStateFlow<AlarmUiState>(AlarmUiState.Loading)
    val state: StateFlow<AlarmUiState> = _state

    private val _hasUnread = MutableStateFlow(false)
    val hasUnread: StateFlow<Boolean> = _hasUnread

    fun loadAlarms(context: Context, size: Int = 20) {
        _state.value = AlarmUiState.Loading
        val repo = repoFactory(context)
        viewModelScope.launch {
            loadAllAlarmPages(repo, size)
                .onSuccess { alarms ->
                    val readIds = AlarmReadStore.getReadAlarmIds(context)
                    val uiItems = alarms.map { alarm ->
                        AlarmUiItem(
                            alarm = alarm,
                            isRead = alarm.alarmId in readIds
                        )
                    }
                    _state.value = AlarmUiState.Success(uiItems)
                    // 목록에 보여진 알림은 "봤음"으로만 저장 -> 레드닷만 끄고 텍스트 색은 그대로 유지
                    AlarmReadStore.markAlarmsSeen(context, alarms.map { it.alarmId })
                    _hasUnread.value = false
                }
                .onFailure { e ->
                    _state.value = AlarmUiState.Error(e.message ?: "알림 목록 조회 실패")
                }
        }
    }

    /** 개별 알림을 탭했을 때 호출: 해당 알림만 읽음(회색) 처리하고 화면에 즉시 반영한다. */
    fun markAlarmRead(context: Context, alarmId: Long) {
        AlarmReadStore.markAlarmRead(context, alarmId)
        val current = _state.value
        if (current is AlarmUiState.Success) {
            _state.value = AlarmUiState.Success(
                current.alarms.map { item ->
                    if (item.alarm.alarmId == alarmId && !item.isRead) {
                        item.copy(isRead = true)
                    } else {
                        item
                    }
                }
            )
        }
    }

    fun refreshAlarmFlag(context: Context) {
        val repo = repoFactory(context)
        viewModelScope.launch {
            repo.getAlarms(page = 0, size = 20)
                .onSuccess { response ->
                    _hasUnread.value = AlarmReadStore.hasUnread(
                        context,
                        response.content.map { it.alarmId }
                    )
                }
                .onFailure {
                    _hasUnread.value = AlarmReadStore.hasLocalUnread(context)
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
