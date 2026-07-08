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
    val albumId: String, // 앨범 ID (iTunes collectionId)
    // 장르 추천 알고리즘용 (iTunes Search API 제공 필드)
    val trackId: Long? = null,
    val artistId: Long? = null,
    val primaryGenreName: String? = null,
    val sourceType: String = "ITUNES"
)

