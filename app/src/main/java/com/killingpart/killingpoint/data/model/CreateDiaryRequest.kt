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
    val totalDuration: Int, // YouTube 비디오 전체 길이 (초 단위)
    // 장르 추천 알고리즘용 (iTunes Search API 제공). 값이 없으면 null → 직렬화 시 생략
    val sourceType: String? = null,
    val trackId: Long? = null,
    val artistId: Long? = null,
    val primaryGenreName: String? = null
)


