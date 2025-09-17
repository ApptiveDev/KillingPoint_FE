package com.killingpart.killingpoint.data.remote

import com.killingpart.killingpoint.data.model.KakaoAuthRequest
import com.killingpart.killingpoint.data.model.KakaoAuthResponse
import com.killingpart.killingpoint.data.model.YouTubeVideo
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @GET("youtube/search")
    suspend fun searchVideos(
        @Query("artist") artist: String,
        @Query("title") title: String
    ): List<YouTubeVideo>

    @POST("auth/kakao")
    suspend fun loginWithKakao(@Body body: KakaoAuthRequest): KakaoAuthResponse

}