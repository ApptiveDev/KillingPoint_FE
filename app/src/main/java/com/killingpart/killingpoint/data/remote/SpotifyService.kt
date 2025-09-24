package com.killingpart.killingpoint.data.remote

import com.killingpart.killingpoint.data.spotify.SpotifySearchResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface SpotifyService {

    @FormUrlEncoded
    @POST("api/token")
    suspend fun getAccessToken(
        @Header("Authorization") authorization: String,
        @Field("grant_type") grantType: String = "client_credentials"
    ): SpotifyTokenResponse

    @GET("v1/search")
    suspend fun searchTracks(
        @Header("Authorization") bearerToken: String,
        @Header("Accept-Language") acceptLanguage: String = "ko-KR",
        @Query("q") query: String,
        @Query("type") type: String = "track",
        @Query("market") market: String = "KR",
        @Query("limit") limit: Int = 5
    ): SpotifySearchResponse
}

data class SpotifyTokenResponse(
    val access_token: String,
    val token_type: String,
    val expires_in: Int
)


