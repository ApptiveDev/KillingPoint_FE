package com.killingpart.killingpoint.data.local

import android.content.Context

object AlarmReadStore {
    private const val PREF_NAME = "alarm_read_state"
    private const val KEY_READ_ALARM_IDS = "read_alarm_ids"
    private const val KEY_HAS_LOCAL_UNREAD = "has_local_unread"

    fun getReadAlarmIds(context: Context): Set<Long> {
        return context.applicationContext
            .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getStringSet(KEY_READ_ALARM_IDS, emptySet())
            .orEmpty()
            .mapNotNull { it.toLongOrNull() }
            .toSet()
    }

    fun markAlarmsRead(context: Context, alarmIds: Collection<Long>) {
        if (alarmIds.isEmpty()) return

        val appContext = context.applicationContext
        val preferences = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val updatedIds = preferences
            .getStringSet(KEY_READ_ALARM_IDS, emptySet())
            .orEmpty()
            .toMutableSet()
            .apply {
                addAll(alarmIds.map { it.toString() })
            }

        preferences.edit()
            .putStringSet(KEY_READ_ALARM_IDS, updatedIds)
            .putBoolean(KEY_HAS_LOCAL_UNREAD, false)
            .apply()
    }

    fun hasUnread(context: Context, alarmIds: Collection<Long>): Boolean {
        if (hasLocalUnread(context)) return true
        if (alarmIds.isEmpty()) return false

        val readIds = getReadAlarmIds(context)
        return alarmIds.any { it !in readIds }
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
