package com.killingpart.killingpoint.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.killingpart.killingpoint.data.model.YouTubeVideo
import com.killingpart.killingpoint.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface YouTubeUiState {
    data object Loading : YouTubeUiState
    data class Success(val video: YouTubeVideo) : YouTubeUiState
    data class Error(val message: String) : YouTubeUiState
}

class YouTubeViewModel(
    private val repoFactory: (Context) -> AuthRepository = { ctx ->
        AuthRepository(ctx)
    }
) : ViewModel() {

    private val _state = MutableStateFlow<YouTubeUiState>(YouTubeUiState.Loading)
    val state: StateFlow<YouTubeUiState> = _state

    fun searchVideos(context: Context, artist: String, title: String) {
        _state.value = YouTubeUiState.Loading
        val repo = repoFactory(context)
        viewModelScope.launch {
            try {
                val videos = repo.searchVideos(artist, title)
                if (videos.isNotEmpty()) {
                    _state.value = YouTubeUiState.Success(videos.first())
                } else {
                    _state.value = YouTubeUiState.Error("검색 결과가 없습니다")
                }
            } catch (e: Exception) {
                _state.value = YouTubeUiState.Error(e.message ?: "비디오 검색 실패")
            }
        }
    }
}