package com.killingpart.killingpoint.data.repository

import android.content.Context
import com.killingpart.killingpoint.data.local.TokenStore
import com.killingpart.killingpoint.data.model.KakaoAuthRequest
import com.killingpart.killingpoint.data.model.KakaoAuthResponse
import com.killingpart.killingpoint.data.model.MyDiaries
import com.killingpart.killingpoint.data.model.UserInfo
import com.killingpart.killingpoint.data.model.YouTubeVideo
import com.killingpart.killingpoint.data.model.CreateDiaryRequest
import com.killingpart.killingpoint.data.model.Diary
import com.killingpart.killingpoint.data.remote.RetrofitClient
import com.killingpart.killingpoint.data.remote.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class AuthRepository(
    private val context: Context,
    private val api: ApiService = RetrofitClient.getApi(context),
    private val tokenStore: TokenStore = TokenStore(context.applicationContext)
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

    suspend fun getUserInfo(): Result<UserInfo> =
        withContext(Dispatchers.IO) {
            runCatching {
                val accessToken = getAccessToken() 
                    ?: throw IllegalStateException("액세스 토큰이 없습니다")
                api.getUserInfo("Bearer $accessToken")
            }.recoverCatching { e ->
                if (e is HttpException) {
                    val code = e.code()
                    val msg = e.response()?.errorBody()?.string().orEmpty()
                    throw IllegalStateException("사용자 정보 조회 실패 ($code): $msg")
                } else {
                    throw e
                }
            }
        }

    suspend fun refreshAccessToken(): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val refreshToken = getRefreshToken() 
                    ?: throw IllegalStateException("리프레시 토큰이 없습니다")
                val response = api.refreshAccessToken(refreshToken)
                tokenStore.save(response.accessToken, response.refreshToken)
            }.recoverCatching { e ->
                if (e is HttpException) {
                    val code = e.code()
                    val msg = e.response()?.errorBody()?.string().orEmpty()
                    // 토큰 갱신 실패 시 토큰 삭제
                    clearTokens()
                    throw IllegalStateException("토큰 갱신 실패 ($code): $msg")
                } else {
                    throw e
                }
            }
        }

    suspend fun searchVideos(artist: String, title: String): List<YouTubeVideo> =
        withContext(Dispatchers.IO) {
            try {
                val accessToken = getAccessToken()
                    ?: throw IllegalStateException("액세스 토큰이 없습니다")
                api.searchVideos("Bearer $accessToken", artist, title)
            } catch (e: HttpException) {
                val code = e.code()
                val msg = e.response()?.errorBody()?.string().orEmpty()
                throw IllegalStateException("비디오 검색 실패 ($code): $msg")
            }
        }


    suspend fun getMyDiaries(page: Int = 0, size: Int = 10): MyDiaries =
        withContext(Dispatchers.IO) {
            try {
                val accessToken = getAccessToken() 
                    ?: throw IllegalStateException("액세스 토큰이 없습니다")
                api.getMyDiaries("Bearer $accessToken", page, size)
            } catch (e: HttpException) {
                val code = e.code()
                val msg = e.response()?.errorBody()?.string().orEmpty()
                throw IllegalStateException("다이어리 조회 실패 ($code): $msg")
            }
        }

    suspend fun createDiary(body: CreateDiaryRequest) = withContext(Dispatchers.IO) {
        try {
            val accessToken = getAccessToken() ?: throw IllegalStateException("액세스 토큰이 없습니다")
            val response = api.createDiary("Bearer $accessToken", body)
            if (!response.isSuccessful) {
                throw IllegalStateException("일기 작성 실패 (${response.code()}): ${response.message()}")
            }
        } catch (e: HttpException) {
            val code = e.code()
            val msg = e.response()?.errorBody()?.string().orEmpty()
            throw IllegalStateException("일기 작성 실패 ($code): $msg")
        }
    }
}
