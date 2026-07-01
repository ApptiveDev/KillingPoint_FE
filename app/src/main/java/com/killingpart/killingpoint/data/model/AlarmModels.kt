package com.killingpart.killingpoint.data.model

data class FcmTokenRequest(
    val token: String
)

data class AlarmEnabledRequest(
    val alarmEnabled: Boolean
)

data class AlarmEnabledResponse(
    val alarmEnabled: Boolean
)

data class AlarmItem(
    val alarmId: Long,
    val title: String,
    val content: String,
    val deepLink: String,
    val type: String,
    val createDate: String? = null
)

data class AlarmPage(
    val size: Int,
    val number: Int,
    val totalElements: Int,
    val totalPages: Int
)

data class AlarmResponse(
    val content: List<AlarmItem>,
    val page: AlarmPage
)
