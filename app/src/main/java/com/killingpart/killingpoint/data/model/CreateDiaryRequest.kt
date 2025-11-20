package com.killingpart.killingpoint.data.model

data class CreateDiaryRequest(
    val artist: String,
    val musicTitle: String,
    val albumImageUrl: String,
    val videoUrl: String,
    val scope: String,
    val content: String,
    val duration: String,
    val start: String,
    val end: String,
    val totalDuration: Int // YouTube 비디오 전체 길이 (초 단위)
)


