package com.ssafy.firstproject.base

import com.ssafy.firstproject.base.ApplicationClass.Companion.sharedPreferencesUtil
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {

        val request = chain.request()
        val accessToken = sharedPreferencesUtil.getAccessToken()

        return if (accessToken != null) {
            val newRequest = request.newBuilder()
                .addHeader("Authorization", "Bearer $accessToken")
                .build()
            chain.proceed(newRequest)
        } else {
            chain.proceed(request) // 비로그인 유저는 그대로 요청
        }
    }
}