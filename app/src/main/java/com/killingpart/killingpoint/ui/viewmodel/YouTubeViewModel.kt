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

