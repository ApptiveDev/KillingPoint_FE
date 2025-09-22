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
            
            val refreshToken = tokenStore.getRefreshTokenSync()
            if (refreshToken != null) {
                try {
                    val newAccessToken = refreshAccessToken(refreshToken)
                    if (newAccessToken != null) {
                        // 새로운 토큰으로 재요청
                        val newRequest = originalRequest.newBuilder()
                            .addHeader("Authorization", "Bearer $newAccessToken")
                            .build()
                        return chain.proceed(newRequest)
                    }
                } catch (e: Exception) {
                }
            }
        }

        return response
    }

    private fun refreshAccessToken(refreshToken: String): String? {
        return try {
            runBlocking {
                val response = RetrofitClient.getApi(tokenStore.context).refreshAccessToken(refreshToken)
                tokenStore.save(response.accessToken, response.refreshToken)
                response.accessToken
            }
        } catch (e: Exception) {
            null
        }
    }
}
