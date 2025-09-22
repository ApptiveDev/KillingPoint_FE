// data/local/TokenStore.kt
package com.killingpart.killingpoint.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.dataStore by preferencesDataStore("auth_tokens")

class TokenStore(private val _context: Context) {
    val context: Context get() = _context
    companion object {
        private val KEY_AT = stringPreferencesKey("access_token")
        private val KEY_RT = stringPreferencesKey("refresh_token")
    }

    suspend fun save(access: String, refresh: String) {
        _context.dataStore.edit { pref ->
            pref[KEY_AT] = access
            pref[KEY_RT] = refresh
        }
    }

    suspend fun getAccessToken(): String? =
        _context.dataStore.data.map { it[KEY_AT] }.first()

    suspend fun getRefreshToken(): String? =
        _context.dataStore.data.map { it[KEY_RT] }.first()

    suspend fun clear() {
        _context.dataStore.edit { it.clear() }
    }

    fun getAccessTokenSync(): String? = runBlocking { getAccessToken() }
    fun getRefreshTokenSync(): String? = runBlocking { getRefreshToken() }
}
