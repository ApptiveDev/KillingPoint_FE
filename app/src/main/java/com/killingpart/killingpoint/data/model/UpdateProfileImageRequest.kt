package com.killingpart.killingpoint.data.model

data class UpdateProfileImageRequest(
    val id: Long,
    val presignedUrl: String
)

