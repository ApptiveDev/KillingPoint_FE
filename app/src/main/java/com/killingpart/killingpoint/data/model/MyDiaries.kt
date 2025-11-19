package com.killingpart.killingpoint.data.model

import com.google.gson.annotations.SerializedName

enum class Scope {
    PUBLIC,
    PRIVATE,
    KILLING_PART
}

data class Diary(
    @SerializedName("diaryId")
    val id: Long? = null,
    val artist: String,
    @SerializedName("musicTitle")
    val musicTitle: String,
    @SerializedName("albumImageUrl")
    val albumImageUrl: String,
    val content: String,
    @SerializedName("videoUrl")
    val videoUrl: String,
    val scope: Scope,
    var duration: String,
    var start: String,
    var end: String,
    @SerializedName("createDate")
    val createDate: String,
    @SerializedName("updateDate")
    val updateDate: String,

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
