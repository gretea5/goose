package com.ssafy.firstproject.base

import android.util.Log
import com.ssafy.firstproject.base.ApplicationClass.Companion.sharedPreferencesUtil
import com.ssafy.firstproject.base.ApplicationClass.Companion.userRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

private const val TAG = "TokenAuthenticator"

class TokenAuthenticator : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        return runBlocking {
            if (response.code != 401) return@runBlocking null

            val refreshToken = sharedPreferencesUtil.getRefreshToken() ?: return@runBlocking null

            val newRequest =
                kotlin.runCatching {
                    userRepository.refreshToken(refreshToken)
                }.mapCatching { newTokenResponse ->
                    if (newTokenResponse.isSuccessful) {
                        newTokenResponse.body()?.let {
                            if (it.success) {
                                val newAccessToken = it.accessToken
                                val newRefreshToken = it.refreshToken

                                // 새 토큰 저장
                                sharedPreferencesUtil.addAccessToken(newAccessToken)
                                sharedPreferencesUtil.addRefreshToken(newRefreshToken)

                                // 새로운 Access Token으로 요청 재시도
                                response.request.newBuilder()
                                    .header("Authorization", "Bearer $newAccessToken")
                                    .build()
                            } else {
                                // RefreshToken이 만료된 경우
                                sharedPreferencesUtil.removeAccessToken()
                                sharedPreferencesUtil.removeRefreshToken()

                                // AcceessToken 없이 요청 재시도
                                response.request.newBuilder()
                                    .build()
                            }
                        }
                    } else {
                        Log.d(TAG, "authenticate fail: ${newTokenResponse.code()}")
                        null
                    }
                }.getOrElse {
                    Log.e(TAG, "authenticate: ${it.message}", it)
                    null
                }

            return@runBlocking newRequest
        }
    }
}
