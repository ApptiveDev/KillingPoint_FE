package com.killingpart.killingpoint.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.killingpart.killingpoint.data.model.YouTubeVideo
import com.killingpart.killingpoint.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class YouTubeUiState(
    val videos: List<YouTubeVideo> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class YouTubeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(YouTubeUiState())
    val uiState: StateFlow<YouTubeUiState> = _uiState.asStateFlow()
    
    var artist: String = ""
        private set
    
    var title: String = ""
        private set
    
    fun updateArtist(artist: String) {
        this.artist = artist
    }
    
    fun updateTitle(title: String) {
        this.title = title
    }
    
    fun searchVideos() {
        if (artist.isBlank() || title.isBlank()) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val videos = RetrofitClient.api.searchVideos(
                    title = title,
                    artist = artist
                )
                _uiState.value = _uiState.value.copy(
                    videos = videos,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "알 수 없는 오류가 발생했습니다."
                )
            }
        }
    }
}
