package com.killingpart.killingpoint.data.remote

import com.killingpart.killingpoint.data.local.TokenStore
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class AuthInterceptor(
    private val tokenStore: TokenStore
) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val accessToken = tokenStore.getAccessTokenSync()

        val requestWithAuth = if (accessToken != null) {
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $accessToken")
                .build()
        } else {
            originalRequest
        }

        val response = chain.proceed(requestWithAuth)

        if (response.code == 401 && accessToken != null) {
            response.close()
            
            // 토큰이 만료된 경우 즉시 토큰 삭제 (무한 루프 방지)
            android.util.Log.w("AuthInterceptor", "401 오류 발생 - 토큰 삭제")
            tokenStore.clearSync()
        }

        return response
    }

}
