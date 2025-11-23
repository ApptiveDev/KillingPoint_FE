package com.killingpart.killingpoint.data.spotify

data class SpotifySearchResponse(
    val tracks: Tracks
)

data class Tracks(
    val items: List<TrackItem>
)

data class TrackItem(
    val id: String,
    val name: String, // 제목
    val artists: List<Artist>,
    val album: Album
)

data class Artist(
    val name: String
)

data class Album(
    val id: String, // Spotify 앨범 ID
    val images: List<Image>
)

data class Image(
    val url: String,
    val width: Int?,
    val height: Int?
)

data class SimpleTrack(
    val id: String,
    val title: String,
    val artist: String,
    val albumImageUrl: String?,
    val albumId: String // Spotify 앨범 ID
)

