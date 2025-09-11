package com.killingpart.killingpoint.data.remote

interface ApiService {

    @GET("youtube")
    suspend fun getYoutube(
        @Body
    )
}