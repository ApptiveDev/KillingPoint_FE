package com.killingpart.killingpoint.ui.screen.HomeScreen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.killingpart.killingpoint.ui.viewmodel.YouTubeViewModel

@Composable
fun YoutubeBox() {
    val viewModel: YouTubeViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = viewModel.artist,
            onValueChange = viewModel::updateArtist,
            label = { Text("아티스트") },
            modifier = Modifier.fillMaxWidth()
        )
        
        OutlinedTextField(
            value = viewModel.title,
            onValueChange = viewModel::updateTitle,
            label = { Text("노래 제목") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { viewModel.searchVideos() },
            enabled = viewModel.artist.isNotBlank() && viewModel.title.isNotBlank() && !uiState.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("검색")
        }

        if (uiState.isLoading) {
            Text("로딩 중...")
        } else if (uiState.error != null) {
            Text("오류: ${uiState.error}")
        } else if (uiState.videos.isNotEmpty()) {
            Text("검색 결과: ${uiState.videos.size}개")
            uiState.videos.forEach { video ->
                Text("- ${video.title}")
            }
        }
    }
}