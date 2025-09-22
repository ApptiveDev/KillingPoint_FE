package com.killingpart.killingpoint.data.remote

import com.killingpart.killingpoint.data.model.KakaoAuthRequest
import com.killingpart.killingpoint.data.model.KakaoAuthResponse
import com.killingpart.killingpoint.data.model.MyDiaries
import com.killingpart.killingpoint.data.model.UserInfo
import com.killingpart.killingpoint.data.model.YouTubeVideo
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @GET("youtube")
    suspend fun searchVideos(
        @Query("artist") artist: String,
        @Query("title") title: String
    ): List<YouTubeVideo>

    @POST("oauth2/kakao")
    suspend fun loginWithKakao(@Body body: KakaoAuthRequest): KakaoAuthResponse

    @GET("users/my")
    suspend fun getUserInfo(@Header("Authorization") accessToken: String): UserInfo

    @POST("jwt/exchange")
    suspend fun refreshAccessToken(@Header("X-Refresh-Token") refreshToken: String): KakaoAuthResponse

    @GET("diaries/my")
    suspend fun getMyDiaries(
        @Header("Authorization") accessToken: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 5
    ): MyDiaries
}