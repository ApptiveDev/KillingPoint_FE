package com.killingpart.killingpoint.data.repository

import com.killingpart.killingpoint.data.local.TokenStore
import com.killingpart.killingpoint.data.model.KakaoAuthRequest
import com.killingpart.killingpoint.data.model.KakaoAuthResponse
import com.killingpart.killingpoint.data.remote.RetrofitClient
import com.killingpart.killingpoint.data.remote.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class AuthRepository(
    private val api: ApiService = RetrofitClient.api,
    private val tokenStore: TokenStore
) {
    /**
     *  카카오 accessToken을 받아서:
     *   1) 우리 서버 /auth/kakao 로 교환
     *   2) 우리 서버 access/refresh 토큰 저장
     */
    suspend fun exchangeKakaoAccessToken(kakaoAccessToken: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val res: KakaoAuthResponse = api.loginWithKakao(KakaoAuthRequest(kakaoAccessToken))
                tokenStore.save(res.accessToken, res.refreshToken)
            }.recoverCatching { e ->
                if (e is HttpException) {
                    val code = e.code()
                    val msg = e.response()?.errorBody()?.string().orEmpty()
                    throw IllegalStateException("로그인 실패 ($code): $msg")
                } else {
                    throw e
                }
            }
        }

    suspend fun getAccessToken(): String? = tokenStore.getAccessToken()
    suspend fun getRefreshToken(): String? = tokenStore.getRefreshToken()
    suspend fun clearTokens() = tokenStore.clear()
}
