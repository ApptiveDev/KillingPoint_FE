package com.killingpart.killingpoint.data.repository

import com.killingpart.killingpoint.data.itunes.ItunesSearchResponse
import com.killingpart.killingpoint.data.remote.ItunesService
import com.killingpart.killingpoint.data.spotify.SimpleTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * 곡 정보 검색 = Apple iTunes Search API.
 * 장르 추천 알고리즘용 필드(trackId / artistId / primaryGenreName)와 sourceType(ITUNES)을 함께 제공한다.
 */
class ItunesRepository(
    private val service: ItunesService
) {

    suspend fun searchTracks(query: String, limit: Int = 10): List<SimpleTrack> =
        withContext(Dispatchers.IO) {
            if (query.isBlank()) return@withContext emptyList()
            val res: ItunesSearchResponse = service.searchTracks(term = query, limit = limit)
            res.results.mapNotNull { item ->
                val title = item.trackName ?: return@mapNotNull null
                SimpleTrack(
                    id = item.trackId?.toString() ?: return@mapNotNull null,
                    title = title,
                    artist = item.artistName.orEmpty(),
                    // 100x100 썸네일 URL을 고해상도로 치환
                    albumImageUrl = item.artworkUrl100?.replace("100x100bb", "600x600bb"),
                    albumId = item.collectionId?.toString().orEmpty(),
                    trackId = item.trackId?.toString(),
                    artistId = item.artistId?.toString(),
                    primaryGenreName = item.primaryGenreName,
                    sourceType = SOURCE_TYPE
                )
            }
        }

    companion object {
        const val SOURCE_TYPE = "ITUNES"

        fun create(): ItunesRepository {
            val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()
            val retrofit = Retrofit.Builder()
                .baseUrl("https://itunes.apple.com/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            return ItunesRepository(retrofit.create(ItunesService::class.java))
        }
    }
}
