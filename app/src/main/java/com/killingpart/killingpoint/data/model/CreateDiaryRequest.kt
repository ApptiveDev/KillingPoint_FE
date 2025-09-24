package com.killingpart.killingpoint.data.model

data class CreateDiaryRequest(
    val artist: String,
    val musicTitle: String,
    val albumImageUrl: String,
    val videoUrl: String,
    val scope: String,
    val content: String
)


