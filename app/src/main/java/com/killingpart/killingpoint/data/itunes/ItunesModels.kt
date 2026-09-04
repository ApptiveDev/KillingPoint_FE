package com.killingpart.killingpoint.data.itunes

/** iTunes Search API (https://itunes.apple.com/search) 응답 */
data class ItunesSearchResponse(
    val resultCount: Int = 0,
    val results: List<ItunesTrackItem> = emptyList()
)

data class ItunesTrackItem(
    val trackId: Long? = null,
    val trackName: String? = null,
    val artistName: String? = null,
    val artistId: Long? = null,
    val collectionId: Long? = null,
    val collectionName: String? = null,
    val artworkUrl100: String? = null,
    val primaryGenreName: String? = null,
    val trackTimeMillis: Long? = null
)
