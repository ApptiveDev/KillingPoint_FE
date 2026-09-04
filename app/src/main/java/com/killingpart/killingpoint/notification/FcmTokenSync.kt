package com.killingpart.killingpoint.notification

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.killingpart.killingpoint.data.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object FcmTokenSync {
    fun syncCurrentToken(context: Context) {
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                if (token.isNullOrBlank()) return@addOnSuccessListener
                CoroutineScope(Dispatchers.IO).launch {
                    AuthRepository(context.applicationContext)
                        .addDeviceToken(token)
                        .onFailure { e ->
                            Log.e("FcmTokenSync", "토큰 등록 실패: ${e.message}")
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("FcmTokenSync", "토큰 발급 실패: ${e.message}")
            }
    }
}
