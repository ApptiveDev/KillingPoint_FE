package com.killingpart.killingpoint.data.remote

import com.killingpart.killingpoint.data.itunes.ItunesSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

/** iTunes Search API. baseUrl = https://itunes.apple.com/ */
interface ItunesService {

    @GET("search")
    suspend fun searchTracks(
        @Query("term") term: String,
        @Query("media") media: String = "music",
//        @Query("country") country: String = "KR",
        @Query("lang") lang: String = "ko_kr",
        @Query("limit") limit: Int = 10
    ): ItunesSearchResponse
}
