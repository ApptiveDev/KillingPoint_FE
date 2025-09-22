package com.killingpart.killingpoint.data.model

data class UserInfo(
    val username: String,
    val identifier: String,
    val email: String,
    val profileImageUrl: String,
    val userRoleType: String,
    val socialType: String
)
