package com.ssafy.firstproject.data.source.remote

import com.ssafy.firstproject.base.ApplicationClass.Companion.SERVER_URL
import com.ssafy.firstproject.base.ApplicationClass.Companion.gson
import com.ssafy.firstproject.base.ApplicationClass.Companion.nullOnEmptyConverterFactory
import com.ssafy.firstproject.base.AuthInterceptor
import com.ssafy.firstproject.base.TokenAuthenticator
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitCllient {

    fun getRetrofitClient(readTimeout: Long): Retrofit {
        val okHttpClient = OkHttpClient.Builder()
            .readTimeout(readTimeout, TimeUnit.MILLISECONDS)
            .connectTimeout(5000, TimeUnit.MILLISECONDS)
            .addInterceptor(AuthInterceptor())
            .authenticator(TokenAuthenticator())
            // 로그캣에 okhttp.OkHttpClient로 검색하면 http 통신 내용을 보여줍니다.
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build()

        // 앱이 처음 생성되는 순간, retrofit 인스턴스를 생성
        return Retrofit.Builder()
            .baseUrl(SERVER_URL)
            .addConverterFactory(nullOnEmptyConverterFactory)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttpClient)
            .build()
    }
}