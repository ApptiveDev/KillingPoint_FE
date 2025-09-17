// data/local/TokenStore.kt
package com.killingpart.killingpoint.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("auth_tokens")

class TokenStore(private val context: Context) {
    companion object {
        private val KEY_AT = stringPreferencesKey("access_token")
        private val KEY_RT = stringPreferencesKey("refresh_token")
    }

    suspend fun save(access: String, refresh: String) {
        context.dataStore.edit { pref ->
            pref[KEY_AT] = access
            pref[KEY_RT] = refresh
        }
    }

    suspend fun getAccessToken(): String? =
        context.dataStore.data.map { it[KEY_AT] }.first()

    suspend fun getRefreshToken(): String? =
        context.dataStore.data.map { it[KEY_RT] }.first()

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}
