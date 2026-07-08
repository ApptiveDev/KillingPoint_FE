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
    // 추천 기능용 음악 메타데이터 (iTunes). 값 없으면 null → 직렬화 시 생략(수정 시 기존값 보존)
    val musicMetadata: MusicMetadata? = null
)

data class MusicMetadata(
    val sourceType: String = "ITUNES",   // 현재 ITUNES 외 sourceType은 백엔드에서 건너뜀
    val trackId: String? = null,         // 음원 id
    val artistId: String? = null,        // 아티스트 id
    val primaryGenreName: String? = null // iTunes 검색 결과 대표 장르
)


