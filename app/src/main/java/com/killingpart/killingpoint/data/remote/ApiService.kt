package com.killingpart.killingpoint.data.remote

import com.killingpart.killingpoint.data.model.YouTubeVideo
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("youtube/search")
    suspend fun searchVideos(
        @Query("artist") artist: String,
        @Query("title") title: String
    ): List<YouTubeVideo>
}