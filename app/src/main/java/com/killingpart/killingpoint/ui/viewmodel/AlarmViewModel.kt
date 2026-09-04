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

    // 선택(삭제) 모드
    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode

    private val _selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedIds: StateFlow<Set<Long>> = _selectedIds

    fun loadAlarms(context: Context, size: Int = 20) {
        _state.value = AlarmUiState.Loading
        val repo = repoFactory(context)
        viewModelScope.launch {
            loadAllAlarmPages(repo, size)
                .onSuccess { alarms ->
                    val deletedIds = AlarmReadStore.getDeletedAlarmIds(context)
                    val visibleAlarms = alarms.filterNot { it.alarmId in deletedIds }
                    val readIds = AlarmReadStore.getReadAlarmIds(context)
                    val uiItems = visibleAlarms.map { alarm ->
                        AlarmUiItem(
                            alarm = alarm,
                            isRead = alarm.alarmId in readIds
                        )
                    }
                    _state.value = AlarmUiState.Success(uiItems)
                    // 목록에 보여진 알림은 "봤음"으로만 저장 -> 레드닷만 끄고 텍스트 색은 그대로 유지
                    AlarmReadStore.markAlarmsSeen(context, visibleAlarms.map { it.alarmId })
                    _hasUnread.value = false
                }
                .onFailure { e ->
                    _state.value = AlarmUiState.Error(e.message ?: "알림 목록 조회 실패")
                }
        }
    }

    fun setSelectionMode(enabled: Boolean) {
        _isSelectionMode.value = enabled
        if (!enabled) _selectedIds.value = emptySet()
    }

    fun toggleSelection(alarmId: Long) {
        val current = _selectedIds.value
        _selectedIds.value = if (alarmId in current) current - alarmId else current + alarmId
    }

    /** 전체 선택 <-> 전체 해제 토글 */
    fun toggleSelectAll() {
        val allIds = (_state.value as? AlarmUiState.Success)
            ?.alarms
            ?.map { it.alarm.alarmId }
            ?.toSet()
            .orEmpty()
        _selectedIds.value = if (allIds.isNotEmpty() && _selectedIds.value.size == allIds.size) {
            emptySet()
        } else {
            allIds
        }
    }

    fun deleteSelected(context: Context) {
        val ids = _selectedIds.value
        if (ids.isEmpty()) return
        AlarmReadStore.markAlarmsDeleted(context, ids)
        val current = _state.value
        if (current is AlarmUiState.Success) {
            _state.value = AlarmUiState.Success(
                current.alarms.filterNot { it.alarm.alarmId in ids }
            )
        }
        setSelectionMode(false)
    }

    fun deleteAll(context: Context) {
        val current = _state.value as? AlarmUiState.Success ?: return
        val allIds = current.alarms.map { it.alarm.alarmId }.toSet()
        if (allIds.isEmpty()) return
        AlarmReadStore.markAlarmsDeleted(context, allIds)
        _state.value = AlarmUiState.Success(emptyList())
        _hasUnread.value = false
        setSelectionMode(false)
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
