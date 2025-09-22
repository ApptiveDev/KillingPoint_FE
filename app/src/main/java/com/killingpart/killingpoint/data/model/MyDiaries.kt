package com.killingpart.killingpoint.data.model

data class Diary(
    val artist: String,
    val musicTitle: String,
    val albumImageUrl: String,
    val content: String,
    val videoUrl: String
)

data class DiaryPage(
    val size: Int,
    val number: Int,
    val totalElements: Int,
    val totalPages: Int
)

data class MyDiaries(
    val content: List<Diary>,
    val page: DiaryPage
)
