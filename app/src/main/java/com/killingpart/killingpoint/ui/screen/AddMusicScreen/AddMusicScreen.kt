package com.killingpart.killingpoint.ui.screen.AddMusicScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ColorMatrixColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.killingpart.killingpoint.R
import com.killingpart.killingpoint.ui.component.BottomBar
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextOverflow
import com.killingpart.killingpoint.data.spotify.SimpleTrack
import androidx.compose.ui.platform.LocalContext
import com.killingpart.killingpoint.ui.viewmodel.SpotifyViewModel
import com.killingpart.killingpoint.ui.viewmodel.SpotifyUiState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalInspectionMode

@Composable
fun AddMusicScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1D1E20))
    ) {
            // Center background logo (subtle)
            Image(
                painter = painterResource(id = R.drawable.killingpart_logo_dark),
                contentDescription = "앱 배경 로고",
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (-140).dp)
                    .size(280.dp)
            )

            val context = LocalContext.current
            val viewModel = remember { SpotifyViewModel() }
            val state by viewModel.state.collectAsState()

            var query by remember { mutableStateOf("") }

            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(60.dp))
                SearchField(
                    modifier = Modifier.fillMaxWidth(0.85f),
                    value = query,
                    onValueChange = { query = it },
                    onSearchClick = {
                        if (query.isNotBlank()) {
                            viewModel.search(query)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))

                when (state) {
                    is SpotifyUiState.Success -> {
                        val tracks = (state as SpotifyUiState.Success).tracks
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .weight(1f),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(tracks) { track ->
                                TrackRow(track, onClick = {
                                    val encodedTitle = java.net.URLEncoder.encode(track.title, "UTF-8")
                                    val encodedArtist = java.net.URLEncoder.encode(track.artist, "UTF-8")
                                    val encodedImage = java.net.URLEncoder.encode(track.albumImageUrl ?: "", "UTF-8")
                                    navController.navigate("select_duration?title=$encodedTitle&artist=$encodedArtist&image=$encodedImage")
                                })
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        }
                    }
                    is SpotifyUiState.Error -> {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    SpotifyUiState.Loading, SpotifyUiState.Idle -> {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                BottomBar(navController = navController)
            }
    }
}

@Composable
private fun TrackRow(track: SimpleTrack, onClick: () -> Unit = {}) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF232427)),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            if (LocalInspectionMode.current) {
                Image(
                    painter = painterResource(id = R.drawable.example_video),
                    contentDescription = "album",
                    modifier = Modifier.size(56.dp)
                )
            } else {
                AsyncImage(
                    model = track.albumImageUrl ?: R.drawable.example_video,
                    contentDescription = "album",
                    modifier = Modifier.size(56.dp),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = track.title, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = track.artist, color = Color(0xFFA4A4A6), maxLines = 1)
            }
        }
    }
}
@Preview
@Composable
fun AddMusicScreenPreview() {
    AddMusicScreen(navController = rememberNavController())
}

@Preview
@Composable
fun AddMusicScreenSuccessPreview() {
    val mockTracks = listOf(
        SimpleTrack("Effie - CAN I SIP 담배", "Effie", "https://i.scdn.co/image/ab67616d00001e02c6b31f5f1ce2958380fdb9b0"),
        SimpleTrack("A Night At The Symphony", "Laufey", null),
        SimpleTrack("Love Story", "Taylor Swift", null),
        SimpleTrack("Dynamite", "BTS", null),
        SimpleTrack("HypeBoy", "NewJeans", null)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1D1E20))
    ) {
        Image(
            painter = painterResource(id = R.drawable.killingpart_logo_dark),
            contentDescription = "앱 배경 로고",
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-140).dp)
                .size(280.dp)
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            SearchField(
                modifier = Modifier.fillMaxWidth(0.85f),
                value = "A Night At The Symphony",
                onValueChange = {},
                onSearchClick = {}
            )
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .weight(1f),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(mockTracks) { track ->
                    TrackRow(track)
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
            BottomBar(navController = rememberNavController())
        }
    }
}
