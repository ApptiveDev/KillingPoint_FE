package com.killingpart.killingpoint.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.killingpart.killingpoint.data.repository.ItunesRepository
import com.killingpart.killingpoint.data.spotify.SimpleTrack
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface SpotifyUiState {
    data object Idle : SpotifyUiState
    data object Loading : SpotifyUiState
    data class Success(val tracks: List<SimpleTrack>) : SpotifyUiState
    data class Error(val message: String) : SpotifyUiState
}

class SpotifyViewModel(
    // 곡 검색 소스: Spotify → Apple iTunes Search API 로 교체
    private val repo: ItunesRepository = ItunesRepository.create()
) : ViewModel() {

    private val _state = MutableStateFlow<SpotifyUiState>(SpotifyUiState.Idle)
    val state: StateFlow<SpotifyUiState> = _state

    fun search(query: String) {
        if (query.isBlank()) return
        _state.value = SpotifyUiState.Loading
        viewModelScope.launch {
            runCatching {
                repo.searchTracks(query)
            }.onSuccess { tracks ->
                _state.value = SpotifyUiState.Success(tracks)
            }.onFailure { e ->
                _state.value = SpotifyUiState.Error(e.message ?: "검색 실패")
            }
        }
    }
}


