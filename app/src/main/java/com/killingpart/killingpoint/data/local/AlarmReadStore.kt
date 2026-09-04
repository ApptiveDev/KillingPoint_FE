package com.killingpart.killingpoint.data.local

import android.content.Context

object AlarmReadStore {
    private const val PREF_NAME = "alarm_read_state"
    // 텍스트 색상(흰색->회색)용: 사용자가 "개별로 탭한" 알림 ID만 저장
    private const val KEY_READ_ALARM_IDS = "read_alarm_ids"
    // 레드닷용: 알림 목록에 진입해 "화면에서 본" 알림 ID 저장 (탭 여부와 무관)
    private const val KEY_SEEN_ALARM_IDS = "seen_alarm_ids"
    // 로컬 삭제용: 서버 삭제 API가 없어 로컬에서 숨길 알림 ID 저장
    private const val KEY_DELETED_ALARM_IDS = "deleted_alarm_ids"
    private const val KEY_HAS_LOCAL_UNREAD = "has_local_unread"

    // ---------- 텍스트 색상(개별 읽음) ----------

    fun getReadAlarmIds(context: Context): Set<Long> {
        return context.applicationContext
            .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getStringSet(KEY_READ_ALARM_IDS, emptySet())
            .orEmpty()
            .mapNotNull { it.toLongOrNull() }
            .toSet()
    }

    /** 개별 알림 하나를 읽음(회색) 처리한다. 알림을 탭했을 때만 호출한다. */
    fun markAlarmRead(context: Context, alarmId: Long) {
        val preferences = context.applicationContext
            .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val updatedIds = preferences
            .getStringSet(KEY_READ_ALARM_IDS, emptySet())
            .orEmpty()
            .toMutableSet()
            .apply { add(alarmId.toString()) }

        preferences.edit()
            .putStringSet(KEY_READ_ALARM_IDS, updatedIds)
            .apply()
    }

    // ---------- 레드닷(봤음) ----------

    fun getSeenAlarmIds(context: Context): Set<Long> {
        return context.applicationContext
            .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getStringSet(KEY_SEEN_ALARM_IDS, emptySet())
            .orEmpty()
            .mapNotNull { it.toLongOrNull() }
            .toSet()
    }

    /**
     * 알림 목록에 진입해 화면에 보여진 알림들을 "봤음"으로 저장한다.
     * 레드닷만 끄고, 텍스트 색상(개별 읽음)에는 영향을 주지 않는다.
     */
    fun markAlarmsSeen(context: Context, alarmIds: Collection<Long>) {
        val appContext = context.applicationContext
        val preferences = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val updatedIds = preferences
            .getStringSet(KEY_SEEN_ALARM_IDS, emptySet())
            .orEmpty()
            .toMutableSet()
            .apply { addAll(alarmIds.map { it.toString() }) }

        preferences.edit()
            .putStringSet(KEY_SEEN_ALARM_IDS, updatedIds)
            .putBoolean(KEY_HAS_LOCAL_UNREAD, false)
            .apply()
    }

    fun hasUnread(context: Context, alarmIds: Collection<Long>): Boolean {
        if (hasLocalUnread(context)) return true
        if (alarmIds.isEmpty()) return false

        val seenIds = getSeenAlarmIds(context)
        return alarmIds.any { it !in seenIds }
    }

    // ---------- 로컬 삭제 ----------

    fun getDeletedAlarmIds(context: Context): Set<Long> {
        return context.applicationContext
            .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getStringSet(KEY_DELETED_ALARM_IDS, emptySet())
            .orEmpty()
            .mapNotNull { it.toLongOrNull() }
            .toSet()
    }

    /**
     * 알림들을 로컬에서 삭제(숨김) 처리한다.
     * 삭제된 알림은 목록에서 제외되고, 다시 나타나 레드닷을 켜지 않도록 seen 에도 포함시킨다.
     */
    fun markAlarmsDeleted(context: Context, alarmIds: Collection<Long>) {
        if (alarmIds.isEmpty()) return

        val preferences = context.applicationContext
            .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val idStrings = alarmIds.map { it.toString() }

        val updatedDeleted = preferences
            .getStringSet(KEY_DELETED_ALARM_IDS, emptySet())
            .orEmpty()
            .toMutableSet()
            .apply { addAll(idStrings) }
        val updatedSeen = preferences
            .getStringSet(KEY_SEEN_ALARM_IDS, emptySet())
            .orEmpty()
            .toMutableSet()
            .apply { addAll(idStrings) }

        preferences.edit()
            .putStringSet(KEY_DELETED_ALARM_IDS, updatedDeleted)
            .putStringSet(KEY_SEEN_ALARM_IDS, updatedSeen)
            .apply()
    }

    fun markLocalUnread(context: Context) {
        context.applicationContext
            .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_HAS_LOCAL_UNREAD, true)
            .apply()
    }

    fun hasLocalUnread(context: Context): Boolean {
        return context.applicationContext
            .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_HAS_LOCAL_UNREAD, false)
    }
}
