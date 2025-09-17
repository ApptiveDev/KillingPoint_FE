package com.killingpart.killingpoint.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resumeWithException

object KakaoLoginClient {
    private fun Context.activity(): Activity? {
        var c = this
        while (c is ContextWrapper) {
            if (c is Activity) return c
            c = c.baseContext
        }
        return null
    }

    private suspend fun loginInternal(context: Context): String =
        suspendCancellableCoroutine { cont ->
            val act = context.activity() ?: run {
                cont.resumeWithException(IllegalStateException("No Activity context"))
                return@suspendCancellableCoroutine
            }
            val callback = { token: com.kakao.sdk.auth.model.OAuthToken?, err: Throwable? ->
                when {
                    err != null -> {
                        if (err is ClientError && err.reason == ClientErrorCause.Cancelled) cont.cancel()
                        else cont.resumeWithException(err)
                    }
                    token != null -> cont.resume(token.accessToken)
                    else -> cont.resumeWithException(IllegalStateException("Token null"))
                }
            }
            if (UserApiClient.instance.isKakaoTalkLoginAvailable(act)) {
                UserApiClient.instance.loginWithKakaoTalk(act, callback = callback)
            } else {
                UserApiClient.instance.loginWithKakaoAccount(act, callback = callback)
            }
        }

    suspend fun getAccessToken(context: Context): String = loginInternal(context)
}