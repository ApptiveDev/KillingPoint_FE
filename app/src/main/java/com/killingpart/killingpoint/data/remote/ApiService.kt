package com.killingpart.killingpoint.data.remote

import com.killingpart.killingpoint.data.model.YouTubeVideo
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("youtube")
    suspend fun getYoutube(
        @Query("title") title: String,
        @Query("artist") artist: String
    ): List<YouTubeVideo>
}