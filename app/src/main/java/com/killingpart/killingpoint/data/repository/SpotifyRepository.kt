package com.killingpart.killingpoint.data.repository

import android.content.Context
import com.killingpart.killingpoint.data.remote.SpotifyService
import com.killingpart.killingpoint.data.remote.SpotifyTokenResponse
import com.killingpart.killingpoint.data.spotify.SimpleTrack
import com.killingpart.killingpoint.data.spotify.SpotifySearchResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SpotifyRepository(
    private val service: SpotifyService
) {

    private var cachedToken: String? = null
    private var tokenExpiresAtMs: Long = 0L

    suspend fun searchTracks(query: String, market: String = "KR", limit: Int = 5): List<SimpleTrack> =
        withContext(Dispatchers.IO) {
            val token = getValidToken()
            val res: SpotifySearchResponse = service.searchTracks(
                bearerToken = "Bearer $token",
                acceptLanguage = "ko-KR",
                query = query,
                market = market,
                limit = limit
            )
            res.tracks.items.map { item ->
                SimpleTrack(
                    title = item.name,
                    artist = item.artists.joinToString(", ") { it.name },
                    albumImageUrl = item.album.images.firstOrNull()?.url
                )
            }
        }

    private suspend fun getValidToken(): String = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        if (cachedToken != null && now < tokenExpiresAtMs) return@withContext cachedToken!!

        val tokenRes: SpotifyTokenResponse = service.getAccessToken(
            authorization = SPOTIFY_BASIC_AUTH
        )
        cachedToken = tokenRes.access_token
        // 60초 여유
        tokenExpiresAtMs = now + (tokenRes.expires_in - 60) * 1000L
        cachedToken!!
    }

    companion object {
        // 사용자 제공값
        private const val SPOTIFY_BASIC_AUTH = "Basic YzBhMjM1Yzk0MDFhNGI2ZGFjZGZmYjRlNjk0ODc0YTU6MGJmNzc2MTBmN2E4NDM5MmEzMDZmM2JlODYxNDI3NWY="

        fun create(): SpotifyRepository {
            val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl("https://accounts.spotify.com/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val tokenService = retrofit.create(SpotifyService::class.java)

            // 검색은 다른 베이스 URL이라 별도 인스턴스 사용
            val searchRetrofit = Retrofit.Builder()
                .baseUrl("https://api.spotify.com/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val searchService = searchRetrofit.create(SpotifyService::class.java)

            // 토큰/검색 모두 같은 인터페이스라 검색용으로 반환하되 토큰 호출도 가능
            return SpotifyRepository(object : SpotifyService by searchService {
                override suspend fun getAccessToken(authorization: String, grantType: String): SpotifyTokenResponse {
                    return tokenService.getAccessToken(authorization, grantType)
                }
            })
        }
    }
}


