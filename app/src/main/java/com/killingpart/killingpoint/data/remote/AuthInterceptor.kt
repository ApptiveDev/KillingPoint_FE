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
        val request = chain.request()
        val url = request.url.toString()

        if(url.contains("/oauth2/kakao")
            || url.contains("/oauth2/test")
            || url.contains("/jwt/exchange"))
        {
            return chain.proceed(request)
        }

        val accessToken = tokenStore.getAccessTokenSync()
        // DEBUG: log token presence (masked) for investigation
        try {
            if (accessToken != null) {
                val masked = if (accessToken.length > 10) accessToken.substring(0, 6) + "..." + accessToken.takeLast(4) else accessToken
                android.util.Log.d("AuthInterceptor", "add Authorization header, token(masked)=$masked, url=$url")
            } else {
                android.util.Log.d("AuthInterceptor", "no access token available for url=$url")
            }
        } catch (e: Exception) {
            // ignore logging errors
        }
        val requestWithAuth = if (accessToken != null) {
            request.newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .build()
        } else {
            request
        }

        val response = chain.proceed(requestWithAuth)

        return response
    }

}
